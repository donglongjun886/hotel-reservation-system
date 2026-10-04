package com.example.hotel.inventory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对应测试用例：TC-B01/B02（查询语义）、TC-G01 库存断言部分（并发防超卖）。
 * 大床房（room_type_id=1）预置 3 间房。
 */
@SpringBootTest
class InventoryServiceTest {

    private static final Long TYPE_ID = 1L;
    private static final int TOTAL_ROOMS = 3;
    private static final LocalDate CHECKIN = LocalDate.of(2099, 1, 10);
    private static final LocalDate CHECKOUT = LocalDate.of(2099, 1, 12);

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryMapper inventoryMapper;

    @AfterEach
    void cleanup() {
        inventoryMapper.delete(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", TYPE_ID)
                .ge("stay_date", CHECKIN)
                .lt("stay_date", CHECKOUT));
    }

    @Test
    void queryAvailability_noInventoryRow_countsAsCurrentRoomCount() {
        // TC-B01：无库存行时按当前房间数计
        assertEquals(TOTAL_ROOMS, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));
    }

    @Test
    void queryAvailability_dailyMinimum_andZeroWhenFull() {
        // TC-B01/B02：剩余量取逐日最小值；订满后为 0
        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKIN.plusDays(1));
        assertEquals(TOTAL_ROOMS - 1, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));

        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);
        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);
        assertEquals(0, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));
        // 第二日只被占 2 间，区间最小值仍为 0（第一日已满）
        assertThrows(BizException.class,
                () -> inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT));
    }

    @Test
    void release_returnsRoomToSellable() {
        inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);
        assertEquals(TOTAL_ROOMS - 1, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));

        inventoryService.release(TYPE_ID, CHECKIN, CHECKOUT);
        assertEquals(TOTAL_ROOMS, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));
        // 释放后占用数不为负
        List<DailyInventory> rows = selectRows();
        assertEquals(2, rows.size());
        rows.forEach(row -> assertEquals(0, row.getOccupiedCount()));
    }

    @Test
    void tryOccupy_concurrent_neverOversells() throws InterruptedException {
        // TC-G01 库存断言部分：10 线程并发占用同区间，成功数恰为房间数、occupied 永不超 total
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        List<Throwable> unexpected = new java.util.concurrent.CopyOnWriteArrayList<>();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    inventoryService.tryOccupy(TYPE_ID, CHECKIN, CHECKOUT);
                    success.incrementAndGet();
                } catch (BizException e) {
                    assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());
                    assertEquals("该房型所选日期已订满", e.getMessage());
                    soldOut.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    unexpected.add(e);
                }
            });
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertEquals(TOTAL_ROOMS, success.get());
        assertEquals(threads - TOTAL_ROOMS, soldOut.get());

        // 每日 occupied_count 恰为 3 且不超过 total_count，失败方无残留占用
        List<DailyInventory> rows = selectRows();
        assertEquals(2, rows.size());
        for (DailyInventory row : rows) {
            assertEquals(TOTAL_ROOMS, row.getTotalCount());
            assertEquals(TOTAL_ROOMS, row.getOccupiedCount());
            assertTrue(row.getOccupiedCount() <= row.getTotalCount());
        }
        assertEquals(0, inventoryService.queryAvailability(TYPE_ID, CHECKIN, CHECKOUT));
    }

    private List<DailyInventory> selectRows() {
        return inventoryMapper.selectList(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", TYPE_ID)
                .ge("stay_date", CHECKIN)
                .lt("stay_date", CHECKOUT)
                .orderByAsc("stay_date"));
    }
}
