package com.example.hotel.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.auth.AuthService;
import com.example.hotel.auth.dto.LoginRequest;
import com.example.hotel.auth.dto.RegisterRequest;
import com.example.hotel.auth.entity.AuthToken;
import com.example.hotel.auth.entity.User;
import com.example.hotel.auth.mapper.AuthTokenMapper;
import com.example.hotel.auth.mapper.UserMapper;
import com.example.hotel.common.context.LoginUser;
import com.example.hotel.common.context.UserContext;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import com.example.hotel.order.dto.CheckInRequest;
import com.example.hotel.order.dto.CreateOrderRequest;
import com.example.hotel.order.dto.OrderInfo;
import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.entity.OrderStatus;
import com.example.hotel.order.mapper.OrderMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对应并发测试用例：TC-G04（并发分配同一房间仅一笔成功）、TC-G05（并发入住同一订单仅一笔生效）、
 * TC-G06（并发取消 + 入住同一订单终态自洽）。断言以数据库终态为准。
 * 入住校验要求订单在入住窗口内（入住日 ≤ 今天 < 离店日），用当天日期（预创建窗口内，无懒建行，
 * 2~3 线程不超过默认连接池 30，无需放大）。
 */
@SpringBootTest
class AdminConcurrencyTest {

    private static final Long TYPE_ID = 1L;
    private static final String ID_CARD = "110101199001011234";
    private static final String ROOM_A101 = "A101";

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthTokenMapper authTokenMapper;
    @Autowired
    private InventoryMapper inventoryMapper;

    private JdbcTemplate jdbc;

    @Autowired
    void initJdbc(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private final List<Long> createdUserIds = new CopyOnWriteArrayList<>();

    @AfterEach
    void cleanup() {
        if (!createdUserIds.isEmpty()) {
            orderMapper.delete(new QueryWrapper<HotelOrder>().in("user_id", createdUserIds));
            authTokenMapper.delete(new QueryWrapper<AuthToken>().in("user_id", createdUserIds));
            userMapper.delete(new QueryWrapper<User>().in("id", createdUserIds));
            createdUserIds.clear();
        }
        jdbc.update("UPDATE daily_inventory di SET di.occupied_count = ("
                        + "SELECT COUNT(*) FROM hotel_order o "
                        + "WHERE o.room_type_id = di.room_type_id AND o.status IN ('CONFIRMED', 'CHECKED_IN') "
                        + "AND o.checkin_date <= di.stay_date AND o.checkout_date > di.stay_date) "
                        + "WHERE di.stay_date >= ? AND di.stay_date <= ?",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(10));
    }

    @Test
    void checkIn_concurrent_sameRoom_onlyOneSucceeds() throws InterruptedException {
        // TC-G04：两笔订单同时办理入住且都选 A101，恰一笔成功，另一笔提示"房间刚被分配，请刷新重选"
        LocalDate today = LocalDate.now();
        String first = createOrder(newGuest(), today, today.plusDays(2));
        String second = createOrder(newGuest(), today, today.plusDays(2));
        AtomicInteger success = new AtomicInteger();
        AtomicInteger roomTaken = new AtomicInteger();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();

        runConcurrently(2, i -> {
            try {
                orderService.checkIn(i == 0 ? first : second, buildCheckInRequest(ROOM_A101));
                success.incrementAndGet();
            } catch (BizException e) {
                assertEquals("房间刚被分配，请刷新重选", e.getMessage());
                roomTaken.incrementAndGet();
            }
        }, unexpected);

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertEquals(1, success.get());
        assertEquals(1, roomTaken.get());
        // 数据库终态：A101 的在住订单恒为 1（生成列唯一索引兜底）
        Long occupiedCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hotel_order o JOIN room r ON o.room_id = r.id "
                        + "WHERE r.room_no = ? AND o.status = 'CHECKED_IN'", Long.class, ROOM_A101);
        assertEquals(1L, occupiedCount);
    }

    @Test
    void checkIn_concurrent_sameOrder_onlyOneTakesEffect() throws InterruptedException {
        // TC-G05：两个前台会话同时对同一订单办理入住（选不同房间），仅一笔生效，另一笔提示状态已变更
        LocalDate today = LocalDate.now();
        String orderNo = createOrder(newGuest(), today, today.plusDays(2));
        AtomicInteger success = new AtomicInteger();
        AtomicInteger statusChanged = new AtomicInteger();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();

        runConcurrently(2, i -> {
            try {
                orderService.checkIn(orderNo, buildCheckInRequest(i == 0 ? ROOM_A101 : "A102"));
                success.incrementAndGet();
            } catch (BizException e) {
                assertEquals("订单状态已变更，请刷新查看", e.getMessage());
                statusChanged.incrementAndGet();
            }
        }, unexpected);

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertEquals(1, success.get());
        assertEquals(1, statusChanged.get());
        // O1 只分配 1 个房间
        HotelOrder stored = findByOrderNo(orderNo);
        assertEquals(OrderStatus.CHECKED_IN, stored.getStatus());
        Long checkedInRooms = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hotel_order WHERE status = 'CHECKED_IN' AND room_id IN "
                        + "(SELECT id FROM room WHERE room_no IN ('A101', 'A102'))", Long.class);
        assertEquals(1L, checkedInRooms);
    }

    @Test
    void cancelAndCheckIn_concurrent_consistentFinalState() throws InterruptedException {
        // TC-G06：住客取消与前台办理入住并发，仅一个操作成功，终态要么已取消要么已入住
        LocalDate today = LocalDate.now();
        LoginUser owner = newGuest();
        String orderNo = createOrder(owner, today, today.plusDays(2));
        AtomicInteger cancelSuccess = new AtomicInteger();
        AtomicInteger checkInSuccess = new AtomicInteger();
        List<BizException> bizFailures = new CopyOnWriteArrayList<>();
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();

        runConcurrently(2, i -> {
            try {
                if (i == 0) {
                    UserContext.set(owner);
                    try {
                        orderService.cancelMine(orderNo);
                    } finally {
                        UserContext.clear();
                    }
                    cancelSuccess.incrementAndGet();
                } else {
                    orderService.checkIn(orderNo, buildCheckInRequest(ROOM_A101));
                    checkInSuccess.incrementAndGet();
                }
            } catch (BizException e) {
                bizFailures.add(e);
            }
        }, unexpected);

        assertTrue(unexpected.isEmpty(), () -> "未知异常: " + unexpected);
        assertEquals(1, cancelSuccess.get() + checkInSuccess.get());
        assertEquals(1, bizFailures.size());
        // 失败方提示状态不可操作（已变更 / 不可办理入住）
        assertTrue(List.of("订单状态已变更，请刷新查看", "该订单当前状态不可办理入住")
                .contains(bizFailures.get(0).getMessage()));
        // 终态自洽：要么已取消要么已入住，不存在中间错乱
        HotelOrder stored = findByOrderNo(orderNo);
        assertTrue(List.of(OrderStatus.CANCELLED, OrderStatus.CHECKED_IN).contains(stored.getStatus()));
        if (stored.getStatus() == OrderStatus.CHECKED_IN) {
            assertEquals(ID_CARD, stored.getIdCard());
        } else {
            assertNull(stored.getRoomId());
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

    private String createOrder(LoginUser user, LocalDate checkin, LocalDate checkout) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setRoomTypeId(TYPE_ID);
        request.setCheckinDate(checkin);
        request.setCheckoutDate(checkout);
        request.setGuestName("张三");
        request.setGuestPhone("13800001111");
        UserContext.set(user);
        try {
            OrderInfo info = orderService.create(request);
            return info.getOrderNo();
        } finally {
            UserContext.clear();
        }
    }

    private CheckInRequest buildCheckInRequest(String roomNo) {
        CheckInRequest request = new CheckInRequest();
        request.setIdCard(ID_CARD);
        request.setRoomNo(roomNo);
        return request;
    }

    private HotelOrder findByOrderNo(String orderNo) {
        return orderMapper.selectOne(new QueryWrapper<HotelOrder>().eq("order_no", orderNo));
    }
}
