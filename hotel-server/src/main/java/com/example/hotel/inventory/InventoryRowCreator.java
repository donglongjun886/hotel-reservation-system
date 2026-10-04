package com.example.hotel.inventory;

import com.example.hotel.inventory.mapper.InventoryMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 懒建兜底：预创建窗口（InventoryPreCreator）之外日期的库存行创建器。
 * 建行必须在独立小事务中完成并立即提交：
 * 若在调用方事务内 INSERT 撞唯一索引，InnoDB 会为重复键检查持有该记录的 S 锁直到事务结束，
 * 随后并发事务再请求 X 锁（条件更新）会形成 S→X 升级死锁。
 */
@Component
public class InventoryRowCreator {

    private final InventoryMapper inventoryMapper;

    public InventoryRowCreator(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    /** 建行（已存在则忽略），total_count 取当前房间数；独立事务立即提交，空行（occupied=0）无害 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureRow(Long typeId, LocalDate stayDate) {
        inventoryMapper.insertIgnore(typeId, stayDate, inventoryMapper.countRooms(typeId));
    }

    /**
     * UC-09 维护同步前置：在独立事务中补建未来预创建窗口缺失的库存行。
     * 与懒建同理：批量 INSERT IGNORE 若放在维护事务内，会对已存在行持有 S 锁直到事务结束，
     * 随后 syncFutureTotal 请求 X 锁形成 S→X 升级死锁；移出后维护事务内只剩对已存在行的 UPDATE。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureWindowRows(Long typeId, int totalRooms) {
        LocalDate today = LocalDate.now();
        inventoryMapper.preCreate(typeId, today, today.plusDays(InventoryPreCreator.PRE_CREATE_DAYS), totalRooms);
    }
}
