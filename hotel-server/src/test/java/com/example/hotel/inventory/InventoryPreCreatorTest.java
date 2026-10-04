package com.example.hotel.inventory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 库存行预创建（决策 13）：启动时窗口内行已存在、幂等、窗口内占用走热路径。
 * SpringBootTest 启动时会执行 ApplicationRunner，即预创建已生效。
 */
@SpringBootTest
class InventoryPreCreatorTest {

    private static final Long TYPE_ID = 1L; // 大床房 3 间
    private static final int TOTAL_ROOMS = 3;

    @Autowired
    private InventoryPreCreator preCreator;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryMapper inventoryMapper;

    @Test
    void windowRowsExistAfterStartup() {
        LocalDate today = LocalDate.now();
        DailyInventory row = inventoryMapper.selectOne(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", TYPE_ID)
                .eq("stay_date", today));
        assertNotNull(row);
        assertEquals(TOTAL_ROOMS, row.getTotalCount());
        // 窗口末端（今天 + 730 天 - 1）也已创建
        Long count = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", TYPE_ID)
                .ge("stay_date", today)
                .lt("stay_date", today.plusDays(InventoryPreCreator.PRE_CREATE_DAYS)));
        assertEquals(InventoryPreCreator.PRE_CREATE_DAYS, count);
    }

    @Test
    void preCreate_isIdempotent() {
        preCreator.preCreateWindow();
        preCreator.preCreateWindow();
        Long count = inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", TYPE_ID)
                .ge("stay_date", LocalDate.now()));
        assertEquals(InventoryPreCreator.PRE_CREATE_DAYS, count);
    }

    @Test
    void occupyWithinWindow_thenRelease() {
        // 窗口内日期：占用走"已存在行的条件更新"热路径，释放后恢复
        LocalDate checkin = LocalDate.now().plusDays(30);
        LocalDate checkout = checkin.plusDays(2);
        int before = inventoryService.queryAvailability(TYPE_ID, checkin, checkout);
        assertTrue(before <= TOTAL_ROOMS);
        try {
            inventoryService.tryOccupy(TYPE_ID, checkin, checkout);
            assertEquals(before - 1, inventoryService.queryAvailability(TYPE_ID, checkin, checkout));
        } finally {
            inventoryService.release(TYPE_ID, checkin, checkout);
        }
        assertEquals(before, inventoryService.queryAvailability(TYPE_ID, checkin, checkout));
    }
}
