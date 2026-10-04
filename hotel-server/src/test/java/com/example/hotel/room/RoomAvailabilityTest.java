package com.example.hotel.room;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.inventory.InventoryService;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import com.example.hotel.room.dto.RoomTypeAvailability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 房型剩余量公开查询（技术方案 §6.1，P-C1）。
 * 预置房型：大床房 3 间、双床房 3 间、家庭房 2 间、行政套房 2 间；用 2099 年日期避开预创建窗口。
 */
@SpringBootTest
class RoomAvailabilityTest {

    private static final LocalDate CHECKIN = LocalDate.of(2099, 3, 10);
    private static final LocalDate CHECKOUT = LocalDate.of(2099, 3, 12);

    @Autowired
    private RoomService roomService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryMapper inventoryMapper;

    @AfterEach
    void cleanup() {
        inventoryMapper.delete(new QueryWrapper<DailyInventory>()
                .ge("stay_date", CHECKIN)
                .lt("stay_date", CHECKOUT));
    }

    @Test
    void queryAvailability_returnsAllRoomTypes_andReflectsOccupancy() {
        Map<Long, RoomTypeAvailability> byType = query().stream()
                .collect(Collectors.toMap(RoomTypeAvailability::getRoomTypeId, Function.identity()));
        assertEquals(4, byType.size());
        assertEquals(3, byType.get(1L).getAvailable());
        assertEquals(3, byType.get(2L).getAvailable());
        assertEquals(2, byType.get(3L).getAvailable());
        assertEquals(2, byType.get(4L).getAvailable());

        // 大床房占用 1 间后剩余量减 1，其余房型不受影响
        inventoryService.tryOccupy(1L, CHECKIN, CHECKOUT);
        byType = query().stream()
                .collect(Collectors.toMap(RoomTypeAvailability::getRoomTypeId, Function.identity()));
        assertEquals(2, byType.get(1L).getAvailable());
        assertEquals(3, byType.get(2L).getAvailable());
    }

    @Test
    void queryAvailability_paramValidation() {
        BizException missing = assertThrows(BizException.class,
                () -> roomService.queryAvailability(null, CHECKOUT));
        assertEquals("请选择入住和离店日期", missing.getMessage());

        BizException missing2 = assertThrows(BizException.class,
                () -> roomService.queryAvailability(CHECKIN, null));
        assertEquals("请选择入住和离店日期", missing2.getMessage());

        BizException reversed = assertThrows(BizException.class,
                () -> roomService.queryAvailability(CHECKOUT, CHECKIN));
        assertEquals("离店日期必须晚于入住日期", reversed.getMessage());

        BizException sameDay = assertThrows(BizException.class,
                () -> roomService.queryAvailability(CHECKIN, CHECKIN));
        assertEquals("离店日期必须晚于入住日期", sameDay.getMessage());
    }

    private List<RoomTypeAvailability> query() {
        return roomService.queryAvailability(CHECKIN, CHECKOUT);
    }
}
