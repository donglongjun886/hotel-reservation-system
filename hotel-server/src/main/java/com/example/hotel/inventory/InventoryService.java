package com.example.hotel.inventory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryRowCreator rowCreator;

    public InventoryService(InventoryMapper inventoryMapper, InventoryRowCreator rowCreator) {
        this.inventoryMapper = inventoryMapper;
        this.rowCreator = rowCreator;
    }

    /** 区间 [checkin, checkout) 剩余可订量：逐日剩余的最小值；无库存行的日期按当前房间数计 */
    public int queryAvailability(Long typeId, LocalDate checkin, LocalDate checkout) {
        int totalRooms = inventoryMapper.countRooms(typeId);
        List<DailyInventory> rows = inventoryMapper.selectList(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", typeId)
                .ge("stay_date", checkin)
                .lt("stay_date", checkout));
        Map<LocalDate, DailyInventory> byDate = rows.stream()
                .collect(Collectors.toMap(DailyInventory::getStayDate, row -> row));
        int minRemaining = totalRooms;
        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {
            DailyInventory row = byDate.get(date);
            int remaining = row == null ? totalRooms : row.getTotalCount() - row.getOccupiedCount();
            minRemaining = Math.min(minRemaining, remaining);
        }
        return minRemaining;
    }

    /**
     * 占用区间 [checkin, checkout) 每日 1 间，按日期升序；任一日订满抛 SOLD_OUT。
     * 整单回滚由调用方事务保证（REQUIRED 传播，独立调用时自身成一个事务）。
     * 预创建窗口内库存行恒存在（决策 13），本事务只做"对已存在行的原子条件更新"（纯记录锁）；
     * 窗口外日期走懒建兜底（独立小事务建行），避免下单事务内 INSERT 引发的并发死锁。
     */
    @Transactional
    public void tryOccupy(Long typeId, LocalDate checkin, LocalDate checkout) {
        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {
            occupyOneDate(typeId, date);
        }
    }

    /** 释放区间 [checkin, checkout) 每日 1 间，带 occupied_count > 0 保护，按日期升序 */
    @Transactional
    public void release(Long typeId, LocalDate checkin, LocalDate checkout) {
        for (LocalDate date = checkin; date.isBefore(checkout); date = date.plusDays(1)) {
            inventoryMapper.releaseOne(typeId, date);
        }
    }

    /**
     * UC-09 房间数变更同步（决策 13）。第一步经独立小事务为所有涉及房型补建窗口缺失行
     * （必须先于任何库存行加锁：批量 INSERT IGNORE 的重复键检查要读已有行，
     *  若本事务已持有库存行锁——含范围锁 bleed 到相邻房型的首行——会把独立事务堵死）；
     * 第二步逐房型把 stay_date ≥ 今天 的库存行 total_count 刷成当前房间数，
     * 减容由"occupied_count ≤ 新房间数"条件更新 + 加锁复查超占行兜底：与并发下单在同一批
     * 行锁上串行，无论谁先拿到锁，"occupied ≤ total"的防超卖不变式都成立（违规即整事务回滚）。
     */
    @Transactional
    public void syncTotalForFutureDates(Long... typeIds) {
        for (Long typeId : typeIds) {
            rowCreator.ensureWindowRows(typeId, inventoryMapper.countRooms(typeId));
        }
        for (Long typeId : typeIds) {
            syncOneType(typeId);
        }
    }

    private void syncOneType(Long typeId) {
        int totalRooms = inventoryMapper.countRooms(typeId);
        LocalDate today = LocalDate.now();
        inventoryMapper.syncFutureTotal(typeId, today, totalRooms);
        if (inventoryMapper.countUnsyncedRows(typeId, today, totalRooms) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该房型存在有效订单，请先处理相关订单");
        }
    }

    private void occupyOneDate(Long typeId, LocalDate date) {
        // 一致性读判断行是否存在（不加锁，避免对不存在行的加锁 UPDATE 产生间隙锁）；
        // 预创建窗口内恒为存在，未命中即窗口外日期
        boolean exists = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", typeId)
                .eq("stay_date", date)) > 0;
        if (!exists) {
            // 超出预创建窗口：懒建兜底，独立小事务建行后再走条件更新
            rowCreator.ensureRow(typeId, date);
        }
        if (inventoryMapper.occupyOne(typeId, date) != 1) {
            throw new BizException(ErrorCode.SOLD_OUT);
        }
    }
}
