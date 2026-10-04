package com.example.hotel.inventory;

import com.example.hotel.inventory.mapper.InventoryMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 库存行预创建（决策 13）：应用启动时为全部房型批量创建未来窗口的逐日库存行。
 * 预创建保证下单热路径上库存行总是存在，占用只做"对已存在行的原子条件更新"（纯记录锁），
 * 从而避免下单事务内 INSERT 引发的并发死锁（间隙锁 / 撞唯一索引的 S 锁升级）。
 * 窗口长度覆盖演示期，无需定时任务滚动延期；窗口外日期由下单时懒建兜底（InventoryRowCreator）。
 */
@Component
public class InventoryPreCreator implements ApplicationRunner {

    static final int PRE_CREATE_DAYS = 730;

    private final InventoryMapper inventoryMapper;

    public InventoryPreCreator(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        preCreateWindow();
    }

    /** 为全部房型预创建 [今天, 今天 + PRE_CREATE_DAYS) 的库存行，INSERT IGNORE 幂等可重复执行 */
    public void preCreateWindow() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(PRE_CREATE_DAYS);
        for (Long typeId : inventoryMapper.selectRoomTypeIds()) {
            inventoryMapper.preCreate(typeId, start, end, inventoryMapper.countRooms(typeId));
        }
    }
}
