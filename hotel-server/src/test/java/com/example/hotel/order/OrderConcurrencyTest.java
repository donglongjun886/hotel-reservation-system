package com.example.hotel.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.auth.AuthService;
import com.example.hotel.auth.dto.LoginRequest;
import com.example.hotel.auth.dto.RegisterRequest;
import com.example.hotel.auth.entity.AuthToken;
import com.example.hotel.auth.entity.User;
import com.example.hotel.auth.mapper.AuthTokenMapper;
import com.example.hotel.auth.mapper.UserMapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.context.LoginUser;
import com.example.hotel.common.context.UserContext;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.inventory.InventoryService;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import com.example.hotel.order.dto.CreateOrderRequest;
import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.entity.OrderStatus;
import com.example.hotel.order.mapper.OrderMapper;
import com.example.hotel.room.RoomService;
import com.example.hotel.room.entity.Room;
import com.example.hotel.room.mapper.RoomMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对应并发测试用例：TC-G01（并发预订防超卖）、TC-G07（订单号并发唯一）、TC-G02（并发抢最后一间）、TC-G03（取消与预订交叉）。
 * 并发用例统一使用 2099 年日期，避开库存预创建窗口（今天 +730 天），断言以数据库终态为准。
 * 连接池放大到 60：2099 年库存行未预创建，每线程下单事务内会以 REQUIRES_NEW 懒建行（额外借一个连接）。
 */
@SpringBootTest(properties = "spring.datasource.hikari.maximum-pool-size=60")
class OrderConcurrencyTest {

    private static final Long TYPE_ID = 1L;
    private static final Long TARGET_TYPE_ID = 2L; // 双床房，TC-G08 改挂目标
    private static final int TOTAL_ROOMS = 3;
    private static final LocalDate FAR_FUTURE = LocalDate.of(2099, 1, 1);
    private static final Pattern ORDER_NO = Pattern.compile("HR\\d{8}-\\d{4,}");

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthTokenMapper authTokenMapper;
    @Autowired
    private RoomService roomService;
    @Autowired
    private RoomMapper roomMapper;

    private final List<Long> createdUserIds = new CopyOnWriteArrayList<>();

    @AfterEach
    void cleanup() {
        if (!createdUserIds.isEmpty()) {
            orderMapper.delete(new QueryWrapper<HotelOrder>().in("user_id", createdUserIds));
            authTokenMapper.delete(new QueryWrapper<AuthToken>().in("user_id", createdUserIds));
            userMapper.delete(new QueryWrapper<User>().in("id", createdUserIds));
            createdUserIds.clear();
        }
        inventoryMapper.delete(new QueryWrapper<DailyInventory>().ge("stay_date", FAR_FUTURE));
    }

    @Test
    void create_concurrent_neverOversells_exactlyRoomCountSucceeds() throws InterruptedException {
        // TC-G01：大床房共 3 间，8 个不同住客同时预订同一区间（3 晚），
        // 恰 3 笔成功、其余全部提示已订满；终态不超卖、失败方无残留库存占用
        int threads = 8;
        LocalDate checkin = LocalDate.of(2099, 10, 10);
        LocalDate checkout = LocalDate.of(2099, 10, 13);
        List<LoginUser> users = new CopyOnWriteArrayList<>();
        for (int i = 0; i < threads; i++) {
            users.add(newGuest());
        }
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();

        runConcurrently(threads, i -> {
            UserContext.set(users.get(i));
            try {
                orderService.create(buildRequest(checkin, checkout));
                success.incrementAndGet();
            } catch (BizException e) {
                assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());
                assertEquals("该房型所选日期已订满", e.getMessage());
                soldOut.incrementAndGet();
            } finally {
                UserContext.clear();
            }
        }, unexpected);

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertEquals(TOTAL_ROOMS, success.get());
        assertEquals(threads - TOTAL_ROOMS, soldOut.get());
        // 数据库终态：该区间有效订单恰 3 笔，每日 occupied 恰为 3（无残留）且不超 total
        assertEquals(TOTAL_ROOMS, countActiveOrders(checkin, checkout));
        assertEquals(List.of(TOTAL_ROOMS, TOTAL_ROOMS, TOTAL_ROOMS), occupiedCounts(checkin, checkout));
        for (DailyInventory row : selectRows(checkin, checkout)) {
            assertTrue(row.getOccupiedCount() <= row.getTotalCount(),
                    () -> "occupied=" + row.getOccupiedCount() + " 超过 total=" + row.getTotalCount());
        }
    }

    @Test
    void create_concurrent_orderNosGloballyUnique() throws InterruptedException {
        // TC-G07：20 线程同时创建订单（各占 2099 年不同的一天，排除库存竞争），订单号全局唯一、格式正确
        int threads = 20;
        List<LoginUser> users = new CopyOnWriteArrayList<>();
        for (int i = 0; i < threads; i++) {
            users.add(newGuest());
        }
        Set<String> orderNos = ConcurrentHashMap.newKeySet();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();
        runConcurrently(threads, i -> {
            LocalDate day = LocalDate.of(2099, 6, 1).plusDays(i);
            UserContext.set(users.get(i));
            try {
                orderNos.add(orderService.create(buildRequest(day, day.plusDays(1))).getOrderNo());
            } finally {
                UserContext.clear();
            }
        }, unexpected);

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertEquals(threads, orderNos.size());
        orderNos.forEach(orderNo -> assertTrue(ORDER_NO.matcher(orderNo).matches(),
                () -> "订单号格式: " + orderNo));
    }

    @Test
    void create_concurrent_lastRoom_onlyOneSucceeds() throws InterruptedException {
        // TC-G02：区间仅剩 1 间，两人同时提交，恰一笔成功、一笔提示已订满
        LocalDate checkin = LocalDate.of(2099, 7, 10);
        LocalDate checkout = LocalDate.of(2099, 7, 12);
        for (int i = 0; i < TOTAL_ROOMS - 1; i++) {
            inventoryService.tryOccupy(TYPE_ID, checkin, checkout);
        }
        LoginUser first = newGuest();
        LoginUser second = newGuest();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();

        runConcurrently(2, i -> {
            LoginUser user = i == 0 ? first : second;
            UserContext.set(user);
            try {
                orderService.create(buildRequest(checkin, checkout));
                success.incrementAndGet();
            } catch (BizException e) {
                assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());
                assertEquals("该房型所选日期已订满", e.getMessage());
                soldOut.incrementAndGet();
            } finally {
                UserContext.clear();
            }
        }, unexpected);

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertEquals(1, success.get());
        assertEquals(1, soldOut.get());
        // 数据库终态：该区间有效订单恰 1 笔，每日 occupied 恰为 3 且不超 total
        assertEquals(1, countActiveOrders(checkin, checkout));
        assertEquals(List.of(TOTAL_ROOMS, TOTAL_ROOMS), occupiedCounts(checkin, checkout));
    }

    @Test
    void cancelAndCreate_concurrent_consistentFinalState() throws InterruptedException {
        // TC-G03：仅剩 1 间时，取消与两笔预订交叉；终态自洽：每日 occupied 恒等于
        // 有效订单覆盖数 + 无订单占位（本例 1 间），无负库存、不超卖
        LocalDate checkin = LocalDate.of(2099, 8, 10);
        LocalDate checkout = LocalDate.of(2099, 8, 12);
        LoginUser owner = newGuest();
        LoginUser second = newGuest();
        LoginUser third = newGuest();
        // 甲持有一笔已确认订单，另占 1 间（模拟其他渠道占用），剩余可订 = 1
        UserContext.set(owner);
        String orderNo;
        try {
            orderNo = orderService.create(buildRequest(checkin, checkout)).getOrderNo();
        } finally {
            UserContext.clear();
        }
        inventoryService.tryOccupy(TYPE_ID, checkin, checkout);

        AtomicInteger createSuccess = new AtomicInteger();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();
        String finalOrderNo = orderNo;
        runConcurrently(3, i -> {
            try {
                if (i == 0) {
                    UserContext.set(owner);
                    orderService.cancelMine(finalOrderNo);
                } else {
                    UserContext.set(i == 1 ? second : third);
                    orderService.create(buildRequest(checkin, checkout));
                    createSuccess.incrementAndGet();
                }
            } catch (BizException e) {
                assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());
            } finally {
                UserContext.clear();
            }
        }, unexpected);

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertTrue(createSuccess.get() <= 2, () -> "成功预订数: " + createSuccess.get());
        // 终态自洽：甲的订单已取消；每日 occupied == 有效订单覆盖数 + 1 间无订单占位
        assertEquals(OrderStatus.CANCELLED,
                orderMapper.selectOne(new QueryWrapper<HotelOrder>().eq("order_no", finalOrderNo)).getStatus());
        List<DailyInventory> rows = selectRows(checkin, checkout);
        assertEquals(2, rows.size());
        long activeOrders = countActiveOrders(checkin, checkout);
        assertEquals(createSuccess.get(), activeOrders);
        for (DailyInventory row : rows) {
            assertEquals(activeOrders + 1, (long) row.getOccupiedCount());
            assertTrue(row.getOccupiedCount() >= 0 && row.getOccupiedCount() <= row.getTotalCount());
        }
    }

    @Test
    void reassignRoom_concurrentWithBooking_neverOversells() throws InterruptedException {
        // TC-G08：减容维护（A103 改挂使大床房 3→2 间）与并发下单交叉——维护的条件更新
        // （occupied ≤ 新 total）与下单的条件更新在同一批行锁上串行，恰一方成功，occupied ≤ total 恒成立
        LocalDate checkin = LocalDate.of(2099, 9, 10);
        LocalDate checkout = LocalDate.of(2099, 9, 11);
        // 先造 2 笔有效订单（占 2/3），维护预检"新房间数 2 ≥ 有效订单 2"可通过
        for (int i = 0; i < TOTAL_ROOMS - 1; i++) {
            LoginUser user = newGuest();
            UserContext.set(user);
            try {
                orderService.create(buildRequest(checkin, checkout));
            } finally {
                UserContext.clear();
            }
        }
        Room a103 = roomMapper.selectOne(new QueryWrapper<Room>().eq("room_no", "A103"));
        LoginUser booker = newGuest();
        AtomicInteger maintenanceSuccess = new AtomicInteger();
        AtomicInteger bookingSuccess = new AtomicInteger();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();

        try {
            runConcurrently(2, i -> {
                if (i == 0) {
                    try {
                        roomService.updateRoom(a103.getId(), "A103", TARGET_TYPE_ID);
                        maintenanceSuccess.incrementAndGet();
                    } catch (BizException e) {
                        assertEquals("该房型存在有效订单，请先处理相关订单", e.getMessage());
                    }
                } else {
                    UserContext.set(booker);
                    try {
                        orderService.create(buildRequest(checkin, checkout));
                        bookingSuccess.incrementAndGet();
                    } catch (BizException e) {
                        assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());
                    } finally {
                        UserContext.clear();
                    }
                }
            }, unexpected);

            assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
            assertEquals(1, maintenanceSuccess.get() + bookingSuccess.get(),
                    () -> "维护成功 " + maintenanceSuccess.get() + " + 下单成功 " + bookingSuccess.get() + " 应恰为 1");
            DailyInventory row = selectRows(checkin, checkout).get(0);
            assertTrue(row.getOccupiedCount() <= row.getTotalCount(),
                    () -> "occupied=" + row.getOccupiedCount() + " 超过 total=" + row.getTotalCount());
        } finally {
            // 恢复预置数据：A103 若已改挂则挂回大床房（updateRoom 触发双方库存 total 再同步）
            Room after = roomMapper.selectOne(new QueryWrapper<Room>().eq("room_no", "A103"));
            if (!TYPE_ID.equals(after.getRoomTypeId())) {
                roomService.updateRoom(after.getId(), "A103", TYPE_ID);
            }
        }
    }

    // ---------- 测试辅助 ----------

    /** 起 threads 个线程同时放行执行任务（task 内自行处理业务异常，其余异常收集到 unexpected） */
    private void runConcurrently(int threads, IndexedTask task, List<Throwable> unexpected)
            throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < threads; i++) {
            int index = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.run(index);
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
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
    }

    @FunctionalInterface
    private interface IndexedTask {
        void run(int index) throws Exception;
    }

    private LoginUser newGuest() {
        String phone = "139" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
        RegisterRequest register = new RegisterRequest();
        register.setPhone(phone);
        register.setPassword("Test1234");
        authService.register(register);
        LoginRequest login = new LoginRequest();
        login.setLoginName(phone);
        login.setPassword("Test1234");
        LoginUser user = authService.resolveUser(authService.login(login).getToken());
        createdUserIds.add(user.id());
        return user;
    }

    private CreateOrderRequest buildRequest(LocalDate checkin, LocalDate checkout) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setRequestNo(UUID.randomUUID().toString());
        request.setRoomTypeId(TYPE_ID);
        request.setCheckinDate(checkin);
        request.setCheckoutDate(checkout);
        request.setGuestName("张三");
        request.setGuestPhone("13800001111");
        return request;
    }

    /** 区间 [checkin, checkout) 内有效订单（已确认 + 已入住）数 */
    private long countActiveOrders(LocalDate checkin, LocalDate checkout) {
        return orderMapper.selectCount(new QueryWrapper<HotelOrder>()
                .eq("room_type_id", TYPE_ID)
                .in("status", OrderStatus.CONFIRMED, OrderStatus.CHECKED_IN)
                .lt("checkin_date", checkout)
                .gt("checkout_date", checkin));
    }

    private List<DailyInventory> selectRows(LocalDate checkin, LocalDate checkout) {
        return inventoryMapper.selectList(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", TYPE_ID)
                .ge("stay_date", checkin)
                .lt("stay_date", checkout)
                .orderByAsc("stay_date"));
    }

    private List<Integer> occupiedCounts(LocalDate checkin, LocalDate checkout) {
        return selectRows(checkin, checkout).stream()
                .map(DailyInventory::getOccupiedCount)
                .toList();
    }
}
