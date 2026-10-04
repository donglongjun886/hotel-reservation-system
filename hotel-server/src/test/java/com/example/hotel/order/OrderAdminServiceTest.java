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
import com.example.hotel.common.api.PageResult;
import com.example.hotel.common.context.LoginUser;
import com.example.hotel.common.context.UserContext;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.inventory.InventoryService;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import com.example.hotel.order.dto.AssignableRoomInfo;
import com.example.hotel.order.dto.CheckInPrecheckInfo;
import com.example.hotel.order.dto.CheckInRequest;
import com.example.hotel.order.dto.CreateOrderRequest;
import com.example.hotel.order.dto.OrderInfo;
import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.entity.OrderStatus;
import com.example.hotel.order.mapper.OrderMapper;
import com.example.hotel.room.entity.Room;
import com.example.hotel.room.mapper.RoomMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对应测试用例：TC-D01~D09（前台查询与办理入住）、TC-E01~E05（退房与终态）。
 * 入住窗口相关用例必须用当天日期（BR-06 窗口为"入住日 ≤ 今天 < 离店日"），落在库存预创建窗口内；
 * 当天窗口的库存行无法删除，cleanup 用 SQL 按有效订单重算 occupied_count 保证用例间隔离。
 */
@SpringBootTest
class OrderAdminServiceTest {

    private static final Long TYPE_ID = 1L;
    private static final LocalDate FAR_FUTURE = LocalDate.of(2099, 1, 1);
    private static final String ID_CARD = "110101199001011234";
    private static final String ROOM_A101 = "A101";
    private static final String ROOM_A102 = "A102";
    private static final String ROOM_A103 = "A103";

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
    private InventoryService inventoryService;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private RoomMapper roomMapper;

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
        inventoryMapper.delete(new QueryWrapper<DailyInventory>().ge("stay_date", FAR_FUTURE));
        // 当天窗口的库存行在预创建窗口内无法删除，按现存有效订单重算 occupied，保证用例间隔离
        jdbc.update("UPDATE daily_inventory di SET di.occupied_count = ("
                        + "SELECT COUNT(*) FROM hotel_order o "
                        + "WHERE o.room_type_id = di.room_type_id AND o.status IN ('CONFIRMED', 'CHECKED_IN') "
                        + "AND o.checkin_date <= di.stay_date AND o.checkout_date > di.stay_date) "
                        + "WHERE di.stay_date >= ? AND di.stay_date <= ?",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(10));
    }

    // ---------- TC-D01~D09 查询与办理入住 ----------

    @Test
    void list_byOrderNoOrPhoneOrEmpty() {
        // TC-D01：按订单号、按订单上的住客手机号均能查到；空条件查全部
        LoginUser user = newGuest();
        OrderInfo order = createOrder(user, "李四", "13900000003",
                LocalDate.of(2099, 2, 10), LocalDate.of(2099, 2, 11));

        PageResult<OrderInfo> byOrderNo = orderService.listForAdmin(order.getOrderNo(), 1, 10);
        assertEquals(1, byOrderNo.getTotal());
        assertEquals(order.getOrderNo(), byOrderNo.getList().get(0).getOrderNo());

        PageResult<OrderInfo> byPhone = orderService.listForAdmin("13900000003", 1, 10);
        assertTrue(byPhone.getList().stream().anyMatch(o -> o.getOrderNo().equals(order.getOrderNo())));

        PageResult<OrderInfo> all = orderService.listForAdmin("  ", 1, 10);
        assertTrue(all.getList().stream().anyMatch(o -> o.getOrderNo().equals(order.getOrderNo())));

        assertTrue(orderService.listForAdmin("HR00000000-9999", 1, 10).getList().isEmpty());
    }

    @Test
    void checkIn_success() {
        // TC-D02：办理入住成功，状态变已入住，房间号与身份证号落库，房间从可分配列表消失
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(2));
        assertEquals(3, orderService.listAssignableRooms(order.getOrderNo()).size());

        OrderInfo checkedIn = checkIn(order.getOrderNo(), ID_CARD, ROOM_A101);

        assertEquals(OrderStatus.CHECKED_IN, checkedIn.getStatus());
        assertEquals(ROOM_A101, checkedIn.getRoomNo());
        assertEquals(ID_CARD, checkedIn.getIdCard());
        // DB 落库核对
        HotelOrder stored = findByOrderNo(order.getOrderNo());
        assertEquals(roomId(ROOM_A101), stored.getRoomId());
        assertEquals(ID_CARD, stored.getIdCard());
        // A101 不再出现在可分配列表
        List<String> assignable = assignableRoomNos(order.getOrderNo());
        assertEquals(2, assignable.size());
        assertTrue(assignable.containsAll(List.of(ROOM_A102, ROOM_A103)));
    }

    @Test
    void detailMine_excludesIdCard_adminDetailKeepsIt() {
        // P-C8 住客详情：已入住后追加房间号但不含身份证号；P-A3 前台详情可见身份证号
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(1));
        checkIn(order.getOrderNo(), ID_CARD, ROOM_A101);

        UserContext.set(user);
        try {
            OrderInfo mine = orderService.detailMine(order.getOrderNo());
            assertEquals(ROOM_A101, mine.getRoomNo());
            assertNull(mine.getIdCard());
        } finally {
            UserContext.clear();
        }
        assertEquals(ID_CARD, orderService.detailForAdmin(order.getOrderNo()).getIdCard());
    }

    @Test
    void checkIn_lateArrivalWithinWindow_success() {
        // TC-D03：窗口内晚到（入住日 = 昨天）仍可办理，订单区间与金额不变
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(3));
        long amount = order.getAmount();
        forceDates(order.getOrderNo(), today.minusDays(1), today.plusDays(3));

        OrderInfo checkedIn = checkIn(order.getOrderNo(), ID_CARD, ROOM_A101);

        assertEquals(OrderStatus.CHECKED_IN, checkedIn.getStatus());
        assertEquals(amount, checkedIn.getAmount());
        assertEquals(today.minusDays(1), checkedIn.getCheckinDate());
        assertEquals(today.plusDays(3), checkedIn.getCheckoutDate());
    }

    @Test
    void checkIn_beforeCheckinDate_rejected() {
        // TC-D04：未到入住日办理被拒，文案对齐原型
        LoginUser user = newGuest();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        OrderInfo order = createOrder(user, tomorrow, tomorrow.plusDays(2));

        BizException e = assertThrows(BizException.class, () -> checkIn(order.getOrderNo(), ID_CARD, ROOM_A101));

        assertEquals(ErrorCode.CHECKIN_NOT_ALLOWED.getCode(), e.getCode());
        assertEquals("未到入住日期（入住日：" + tomorrow + "）", e.getMessage());
        assertEquals(OrderStatus.CONFIRMED, findByOrderNo(order.getOrderNo()).getStatus());
    }

    @Test
    void checkIn_afterWindow_rejected_guestCanStillCancel() {
        // TC-D05：超过入住窗口（今天 = 离店日）办理被拒；状态保持已确认，住客仍可取消
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(1));
        forceDates(order.getOrderNo(), today.minusDays(3), today);

        BizException e = assertThrows(BizException.class, () -> checkIn(order.getOrderNo(), ID_CARD, ROOM_A101));

        assertEquals(ErrorCode.CHECKIN_NOT_ALLOWED.getCode(), e.getCode());
        assertEquals("已超过可入住时间（离店日：" + today + "），请引导客人取消重订", e.getMessage());
        assertEquals(OrderStatus.CONFIRMED, findByOrderNo(order.getOrderNo()).getStatus());

        cancel(user, order.getOrderNo());
        assertEquals(OrderStatus.CANCELLED, findByOrderNo(order.getOrderNo()).getStatus());
    }

    @Test
    void checkIn_nonConfirmed_rejected() {
        // TC-D06：已入住/已完成/已取消订单办理入住均被拒，文案对齐原型
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        for (OrderStatus status : List.of(OrderStatus.CHECKED_IN, OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
            OrderInfo order = createOrder(user, today, today.plusDays(1));
            forceStatus(order.getOrderNo(), status);

            BizException e = assertThrows(BizException.class, () -> checkIn(order.getOrderNo(), ID_CARD, ROOM_A101));
            assertEquals(ErrorCode.CHECKIN_NOT_ALLOWED.getCode(), e.getCode());
            assertEquals("该订单当前状态不可办理入住", e.getMessage());
            assertEquals(status, findByOrderNo(order.getOrderNo()).getStatus());
        }
    }

    @Test
    void checkIn_invalidIdCard_rejected() {
        // TC-D07：17 位 / 19 位 / 含字母（非末位 X）/ 留空，均拒且不入库、不分配房间
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        for (String badIdCard : new String[]{"11010119900101123", "1101011990010112345",
                "1101011990010112A4", "  ", null}) {
            OrderInfo order = createOrder(user, today, today.plusDays(1));

            BizException e = assertThrows(BizException.class, () -> checkIn(order.getOrderNo(), badIdCard, ROOM_A101));
            assertEquals(ErrorCode.PARAM_INVALID.getCode(), e.getCode());
            assertEquals("身份证号格式不正确", e.getMessage());

            HotelOrder stored = findByOrderNo(order.getOrderNo());
            assertEquals(OrderStatus.CONFIRMED, stored.getStatus());
            assertNull(stored.getRoomId());
            assertNull(stored.getIdCard());
            // 取消释放库存，避免同窗口订单累积超出房间数
            cancel(user, order.getOrderNo());
        }
    }

    @Test
    void assignableRooms_excludesOccupied() {
        // TC-D08：可分配房间列表不含在住房间
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo first = createOrder(user, today, today.plusDays(2));
        OrderInfo second = createOrder(user, today, today.plusDays(2));
        checkIn(first.getOrderNo(), ID_CARD, ROOM_A101);

        List<String> assignable = assignableRoomNos(second.getOrderNo());

        assertEquals(List.of(ROOM_A102, ROOM_A103), assignable);
    }

    @Test
    void checkIn_noFreeRoom_rejected() {
        // TC-D09：房型全部房间在住（超期未退房悬挂场景），办理入住被拒，订单状态不变
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        String[] rooms = {ROOM_A101, ROOM_A102, ROOM_A103};
        for (String roomNo : rooms) {
            // 构造"昨天入住、今天离店但仍未退房"的在住订单：占房不占今天的库存
            OrderInfo order = createOrder(user, today, today.plusDays(1));
            inventoryService.release(TYPE_ID, today, today.plusDays(1));
            forceCheckedIn(order.getOrderNo(), roomId(roomNo), today.minusDays(1), today);
        }
        // 3 间全部在住后，该房型今天仍有一笔已确认订单（窗口合法）
        OrderInfo pending = createOrder(user, today, today.plusDays(1));

        BizException e = assertThrows(BizException.class, () -> checkIn(pending.getOrderNo(), ID_CARD, ROOM_A101));

        assertEquals(ErrorCode.CHECKIN_NOT_ALLOWED.getCode(), e.getCode());
        assertEquals("该房型当前无空闲房间", e.getMessage());
        assertEquals(OrderStatus.CONFIRMED, findByOrderNo(pending.getOrderNo()).getStatus());
    }

    @Test
    void checkInPrecheck_withinWindow_pass() {
        // TC-D10：窗口内的已确认订单预检通过，无阻断原因，且不做任何状态变更
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(1));

        CheckInPrecheckInfo precheck = orderService.checkInPrecheck(order.getOrderNo());

        assertTrue(precheck.isPass());
        assertTrue(precheck.getReasons().isEmpty());
        assertEquals(OrderStatus.CONFIRMED, findByOrderNo(order.getOrderNo()).getStatus());
    }

    @Test
    void checkInPrecheck_notReady_reportsReasons() {
        // TC-D10：未到入住日 / 非已确认状态，预检不通过，原因文案与办理入住报错一致
        LoginUser user = newGuest();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        OrderInfo future = createOrder(user, tomorrow, tomorrow.plusDays(2));

        CheckInPrecheckInfo notYet = orderService.checkInPrecheck(future.getOrderNo());
        assertFalse(notYet.isPass());
        assertEquals(List.of("未到入住日期（入住日：" + tomorrow + "）"), notYet.getReasons());

        OrderInfo checkedIn = createOrder(user, LocalDate.now(), LocalDate.now().plusDays(1));
        forceStatus(checkedIn.getOrderNo(), OrderStatus.CHECKED_IN);
        CheckInPrecheckInfo wrongStatus = orderService.checkInPrecheck(checkedIn.getOrderNo());
        assertFalse(wrongStatus.isPass());
        assertEquals(List.of("该订单当前状态不可办理入住"), wrongStatus.getReasons());
    }

    // ---------- TC-E01~E05 退房与终态 ----------

    @Test
    void checkOut_success() {
        // TC-E01：退房成功，状态变已完成，房间重新出现在可分配列表
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(2));
        checkIn(order.getOrderNo(), ID_CARD, ROOM_A101);
        assertEquals(2, orderService.listAssignableRooms(order.getOrderNo()).size());

        orderService.checkOut(order.getOrderNo());

        assertEquals(OrderStatus.COMPLETED, findByOrderNo(order.getOrderNo()).getStatus());
        assertEquals(3, orderService.listAssignableRooms(order.getOrderNo()).size());
    }

    @Test
    void checkOut_earlyCheckOut_remainingNightsResellable() {
        // TC-E02：提前退房（离店日前退房）剩余晚数立即可再售，新订单可创建成功
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(3));
        checkIn(order.getOrderNo(), ID_CARD, ROOM_A101);
        // 把 today+1 ~ today+3 区间订满（本订单占 1 间 + 额外 2 间无订单占位）
        inventoryService.tryOccupy(TYPE_ID, today.plusDays(1), today.plusDays(3));
        inventoryService.tryOccupy(TYPE_ID, today.plusDays(1), today.plusDays(3));
        assertEquals(0, inventoryService.queryAvailability(TYPE_ID, today.plusDays(1), today.plusDays(2)));

        orderService.checkOut(order.getOrderNo());

        // 剩余晚数释放 1 间，可立即再售
        assertEquals(1, inventoryService.queryAvailability(TYPE_ID, today.plusDays(1), today.plusDays(2)));
        OrderInfo resold = createOrder(newGuest(), today.plusDays(1), today.plusDays(2));
        assertEquals(OrderStatus.CONFIRMED, resold.getStatus());
    }

    @Test
    void checkOut_earlyCheckOut_amountUnchanged() {
        // TC-E03：提前退房不退差价，金额保持原价（单价 × 3 晚）不变
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(3));
        checkIn(order.getOrderNo(), ID_CARD, ROOM_A101);

        orderService.checkOut(order.getOrderNo());

        OrderInfo detail = orderService.detailForAdmin(order.getOrderNo());
        assertEquals(OrderStatus.COMPLETED, detail.getStatus());
        assertEquals(86550L, detail.getAmount()); // 288.50 × 3 晚
        assertEquals(0, new BigDecimal("865.50").compareTo(findByOrderNo(order.getOrderNo()).getAmount()));
    }

    @Test
    void checkOut_nonCheckedIn_rejected() {
        // TC-E04：已确认/已完成/已取消订单退房被拒，状态不变
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        for (OrderStatus status : List.of(OrderStatus.CONFIRMED, OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
            OrderInfo order = createOrder(user, today, today.plusDays(1));
            forceStatus(order.getOrderNo(), status);

            BizException e = assertThrows(BizException.class, () -> orderService.checkOut(order.getOrderNo()));
            assertEquals(ErrorCode.ORDER_STATUS_CHANGED.getCode(), e.getCode());
            assertEquals("订单状态已变更，请刷新", e.getMessage());
            assertEquals(status, findByOrderNo(order.getOrderNo()).getStatus());
        }
    }

    @Test
    void terminalState_noTransitionOut() {
        // TC-E05：终态（已完成/已取消）无任何出口：取消、入住、退房全部拒绝
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        for (OrderStatus terminal : List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
            OrderInfo order = createOrder(user, today, today.plusDays(1));
            forceStatus(order.getOrderNo(), terminal);

            assertThrows(BizException.class, () -> cancel(user, order.getOrderNo()));
            assertThrows(BizException.class, () -> checkIn(order.getOrderNo(), ID_CARD, ROOM_A101));
            assertThrows(BizException.class, () -> orderService.checkOut(order.getOrderNo()));
            assertEquals(terminal, findByOrderNo(order.getOrderNo()).getStatus());
        }
    }

    @Test
    void detail_notFound_rejected() {
        // 兜底：订单不存在
        BizException e = assertThrows(BizException.class, () -> orderService.detailForAdmin("HR00000000-0001"));
        assertEquals(ErrorCode.NOT_FOUND.getCode(), e.getCode());
        assertEquals("订单不存在", e.getMessage());
    }

    // ---------- 测试辅助 ----------

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

    private OrderInfo createOrder(LoginUser user, LocalDate checkin, LocalDate checkout) {
        return createOrder(user, "张三", "13800001111", checkin, checkout);
    }

    private OrderInfo createOrder(LoginUser user, String guestName, String guestPhone,
                                  LocalDate checkin, LocalDate checkout) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setRequestNo(UUID.randomUUID().toString());
        request.setRoomTypeId(TYPE_ID);
        request.setCheckinDate(checkin);
        request.setCheckoutDate(checkout);
        request.setGuestName(guestName);
        request.setGuestPhone(guestPhone);
        UserContext.set(user);
        try {
            return orderService.create(request);
        } finally {
            UserContext.clear();
        }
    }

    private OrderInfo checkIn(String orderNo, String idCard, String roomNo) {
        CheckInRequest request = new CheckInRequest();
        request.setIdCard(idCard);
        request.setRoomNo(roomNo);
        return orderService.checkIn(orderNo, request);
    }

    private void cancel(LoginUser user, String orderNo) {
        UserContext.set(user);
        try {
            orderService.cancelMine(orderNo);
        } finally {
            UserContext.clear();
        }
    }

    private Long roomId(String roomNo) {
        Room room = roomMapper.selectOne(new QueryWrapper<Room>().eq("room_no", roomNo));
        assertNotNull(room, () -> "预置房间不存在: " + roomNo);
        return room.getId();
    }

    private List<String> assignableRoomNos(String orderNo) {
        return orderService.listAssignableRooms(orderNo).stream()
                .map(AssignableRoomInfo::getRoomNo)
                .toList();
    }

    private void forceStatus(String orderNo, OrderStatus status) {
        HotelOrder order = findByOrderNo(orderNo);
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    private void forceDates(String orderNo, LocalDate checkin, LocalDate checkout) {
        HotelOrder order = findByOrderNo(orderNo);
        order.setCheckinDate(checkin);
        order.setCheckoutDate(checkout);
        orderMapper.updateById(order);
    }

    /** 直接构造在住订单（绕过入住窗口校验，模拟超期未退房悬挂场景） */
    private void forceCheckedIn(String orderNo, Long roomId, LocalDate checkin, LocalDate checkout) {
        HotelOrder order = findByOrderNo(orderNo);
        order.setStatus(OrderStatus.CHECKED_IN);
        order.setRoomId(roomId);
        order.setIdCard(ID_CARD);
        order.setCheckinDate(checkin);
        order.setCheckoutDate(checkout);
        orderMapper.updateById(order);
    }

    private HotelOrder findByOrderNo(String orderNo) {
        return orderMapper.selectOne(new QueryWrapper<HotelOrder>().eq("order_no", orderNo));
    }
}
