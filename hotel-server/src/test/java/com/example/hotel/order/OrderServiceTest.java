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
import com.example.hotel.order.dto.OrderInfo;
import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.entity.OrderStatus;
import com.example.hotel.order.mapper.OrderMapper;
import com.example.hotel.room.entity.RoomType;
import com.example.hotel.room.mapper.RoomTypeMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对应测试用例：TC-B03~B13（创建与回滚）、TC-C01~C05（取消）、TC-A06（数据隔离）、TC-H03（归属按 token）。
 * 大床房（room_type_id=1）预置 3 间房、单价 288.50 元。日期统一取 2099 年，避开库存预创建窗口。
 */
@SpringBootTest
class OrderServiceTest {

    private static final Long TYPE_ID = 1L;
    private static final BigDecimal TYPE_PRICE = new BigDecimal("288.50");
    private static final LocalDate FAR_FUTURE = LocalDate.of(2099, 1, 1);
    private static final Pattern ORDER_NO = Pattern.compile("HR\\d{8}-\\d{4,}");

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
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private RoomTypeMapper roomTypeMapper;

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
    void create_success_mainFlow() {
        // TC-B05/B08/B10：订单号格式、状态已确认、晚数与金额（分单位）、逐日占用（离店日不占）
        LoginUser user = newGuest();
        LocalDate checkin = LocalDate.of(2099, 2, 10);
        LocalDate checkout = LocalDate.of(2099, 2, 13);

        OrderInfo info = createOrder(user, checkin, checkout);

        assertTrue(ORDER_NO.matcher(info.getOrderNo()).matches(), () -> "订单号格式: " + info.getOrderNo());
        assertEquals(OrderStatus.CONFIRMED, info.getStatus());
        assertEquals(3, info.getNights());
        assertEquals(86550L, info.getAmount()); // 288.50 × 3 晚 = 865.50 元 = 86550 分
        assertEquals("张三", info.getGuestName());
        assertEquals("大床房", info.getRoomTypeName());
        assertEquals(List.of(1, 1, 1), occupiedCounts(checkin, checkout));
        // 离店日当天不占库存
        assertEquals(0, occupiedCounts(checkout, checkout.plusDays(1)).stream().mapToInt(i -> i).sum());
        // DB 落库核对：归属为下单账号、金额固化为 BigDecimal（元）
        HotelOrder stored = findByOrderNo(info.getOrderNo());
        assertEquals(user.id(), stored.getUserId());
        assertEquals(0, new BigDecimal("865.50").compareTo(stored.getAmount()));
    }

    @Test
    void create_bookingForOther_guestInfoDiffersFromAccount() {
        // TC-B06：允许代订，订单上存住客姓名/手机号而非账号信息
        LoginUser user = newGuest();
        CreateOrderRequest request = buildRequest(LocalDate.of(2099, 2, 20), LocalDate.of(2099, 2, 21));
        request.setGuestName("李四");
        request.setGuestPhone("13700002222");

        OrderInfo info = createOrder(user, request);

        assertNotEquals(user.loginName(), info.getGuestPhone());
        HotelOrder stored = findByOrderNo(info.getOrderNo());
        assertEquals("李四", stored.getGuestName());
        assertEquals("13700002222", stored.getGuestPhone());
        assertEquals(user.id(), stored.getUserId());
    }

    @Test
    void create_invalidDateRange_rejected() {
        // TC-B03：离店 = 入住、离店早于入住，均拒绝且不生成订单
        LoginUser user = newGuest();
        LocalDate day = LocalDate.of(2099, 3, 1);
        BizException e1 = assertThrows(BizException.class,
                () -> createOrder(user, day, day));
        BizException e2 = assertThrows(BizException.class,
                () -> createOrder(user, day, day.minusDays(1)));
        assertEquals("离店日期必须晚于入住日期", e1.getMessage());
        assertEquals("离店日期必须晚于入住日期", e2.getMessage());
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), e1.getCode());
    }

    @Test
    void create_checkinBeforeToday_rejected() {
        // TC-B04：入住日期早于当天，拒绝且不生成订单
        LoginUser user = newGuest();
        BizException e = assertThrows(BizException.class,
                () -> createOrder(user, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), e.getCode());
        assertEquals("入住日期不能早于今天", e.getMessage());
    }

    @Test
    void create_invalidRequests_occupyNoInventory() {
        // TC-B13：业务校验失败不占库存（先校验后占用）
        LoginUser user = newGuest();
        LocalDate checkin = LocalDate.of(2099, 3, 10);
        LocalDate checkout = LocalDate.of(2099, 3, 12);
        assertEquals(List.of(), occupiedCounts(checkin, checkout));

        CreateOrderRequest pastDate = buildRequest(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        CreateOrderRequest badRange = buildRequest(checkout, checkin);
        CreateOrderRequest badPhone = buildRequest(checkin, checkout);
        badPhone.setGuestPhone("12345");
        CreateOrderRequest blankName = buildRequest(checkin, checkout);
        blankName.setGuestName("  ");
        for (CreateOrderRequest request : List.of(pastDate, badRange, badPhone, blankName)) {
            assertThrows(BizException.class, () -> createOrder(user, request));
        }

        assertEquals(List.of(), occupiedCounts(checkin, checkout));
        assertEquals(0, orderMapper.selectCount(new QueryWrapper<HotelOrder>().eq("user_id", user.id())));
    }

    @Test
    void create_fullMidway_rollsBackEntirely() {
        // TC-B12：多晚订单中途订满，整单回滚无库存残留
        LoginUser user = newGuest();
        LocalDate checkin = LocalDate.of(2099, 3, 20);
        LocalDate checkout = LocalDate.of(2099, 3, 23);
        // 把第 3 晚（03-22）直接占满 3 间：前两晚可订、整区间不可订
        for (int i = 0; i < 3; i++) {
            inventoryService.tryOccupy(TYPE_ID, checkin.plusDays(2), checkin.plusDays(3));
        }

        BizException e = assertThrows(BizException.class, () -> createOrder(user, checkin, checkout));
        assertEquals(ErrorCode.SOLD_OUT.getCode(), e.getCode());
        assertEquals("该房型所选日期已订满", e.getMessage());

        // 不生成订单；前两晚已占用成功的库存随事务回滚（行可能由懒建兜底留下，但 occupied 必须为 0）
        assertEquals(0, orderMapper.selectCount(new QueryWrapper<HotelOrder>().eq("user_id", user.id())));
        assertEquals(List.of(0, 0, 3), occupiedCounts(checkin, checkout));
    }

    @Test
    void create_amountFixedAtCreation_priceChangeNotRetroactive() {
        // TC-B09/B10：金额下单时固化；房型调价后老订单金额不变、新订单按新价
        LoginUser user = newGuest();
        LocalDate checkin = LocalDate.of(2099, 4, 10);
        LocalDate checkout = LocalDate.of(2099, 4, 12);
        OrderInfo first = createOrder(user, checkin, checkout);
        assertEquals(57700L, first.getAmount()); // 288.50 × 2

        RoomType roomType = roomTypeMapper.selectById(TYPE_ID);
        BigDecimal originalPrice = roomType.getPrice();
        assertEquals(0, TYPE_PRICE.compareTo(originalPrice));
        roomType.setPrice(new BigDecimal("388.00"));
        roomTypeMapper.updateById(roomType);
        try {
            // 老订单金额不回溯
            OrderInfo firstAgain = detail(user, first.getOrderNo());
            assertEquals(57700L, firstAgain.getAmount());
            // 新订单按新价计算
            OrderInfo second = createOrder(user, checkin, checkout);
            assertEquals(77600L, second.getAmount()); // 388.00 × 2
        } finally {
            roomType.setPrice(originalPrice);
            roomTypeMapper.updateById(roomType);
        }
    }

    @Test
    void cancel_confirmedOrder_releasesInventory() {
        // TC-C01：已确认订单取消成功，状态变已取消，库存释放
        LoginUser user = newGuest();
        LocalDate checkin = LocalDate.of(2099, 5, 10);
        LocalDate checkout = LocalDate.of(2099, 5, 12);
        OrderInfo order = createOrder(user, checkin, checkout);
        assertEquals(List.of(1, 1), occupiedCounts(checkin, checkout));

        cancel(user, order.getOrderNo());

        assertEquals(OrderStatus.CANCELLED, findByOrderNo(order.getOrderNo()).getStatus());
        assertEquals(List.of(0, 0), occupiedCounts(checkin, checkout));
    }

    @Test
    void cancel_onCheckinDay_stillAllowed() {
        // TC-C02：入住日当天未入住仍可取消（BR-04 宽松规则）
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        OrderInfo order = createOrder(user, today, today.plusDays(1));

        cancel(user, order.getOrderNo());

        assertEquals(OrderStatus.CANCELLED, findByOrderNo(order.getOrderNo()).getStatus());
    }

    @Test
    void cancel_nonConfirmed_rejected() {
        // TC-C03②/C04：已入住/已完成/已取消订单取消被拒，状态与库存不变
        LoginUser user = newGuest();
        LocalDate checkin = LocalDate.of(2099, 5, 20);
        LocalDate checkout = LocalDate.of(2099, 5, 21);
        for (OrderStatus status : List.of(OrderStatus.CHECKED_IN, OrderStatus.COMPLETED, OrderStatus.CANCELLED)) {
            OrderInfo order = createOrder(user, checkin, checkout);
            forceStatus(order.getOrderNo(), status);

            BizException e = assertThrows(BizException.class, () -> cancel(user, order.getOrderNo()));
            assertEquals(ErrorCode.ORDER_STATUS_CHANGED.getCode(), e.getCode());
            assertEquals("订单状态已变更，请刷新查看", e.getMessage());
            assertEquals(status, findByOrderNo(order.getOrderNo()).getStatus());
        }
        // 三笔订单的库存均未释放（仍占 3 间）
        assertEquals(List.of(3), occupiedCounts(checkin, checkout));
    }

    @Test
    void cancel_othersOrder_rejected() {
        // TC-C05：越权取消他人订单被拒（条件更新含 user_id），订单状态不变
        LoginUser owner = newGuest();
        LoginUser other = newGuest();
        OrderInfo order = createOrder(owner, LocalDate.of(2099, 6, 10), LocalDate.of(2099, 6, 11));

        assertThrows(BizException.class, () -> cancel(other, order.getOrderNo()));

        assertEquals(OrderStatus.CONFIRMED, findByOrderNo(order.getOrderNo()).getStatus());
    }

    @Test
    void detailAndList_onlyOwnOrders() {
        // TC-A06/H03：列表与详情按 token 中 user_id 过滤，他人订单不可见
        LoginUser owner = newGuest();
        LoginUser other = newGuest();
        OrderInfo first = createOrder(owner, LocalDate.of(2099, 6, 20), LocalDate.of(2099, 6, 21));
        OrderInfo second = createOrder(owner, LocalDate.of(2099, 6, 22), LocalDate.of(2099, 6, 23));
        createOrder(other, LocalDate.of(2099, 6, 24), LocalDate.of(2099, 6, 25));

        List<OrderInfo> mine = listMine(owner);
        assertEquals(2, mine.size());
        assertTrue(mine.stream().allMatch(o -> findByOrderNo(o.getOrderNo()).getUserId().equals(owner.id())));

        BizException e = assertThrows(BizException.class, () -> detail(other, first.getOrderNo()));
        assertEquals(ErrorCode.NOT_FOUND.getCode(), e.getCode());
        assertEquals("订单不存在", e.getMessage());
        // 本人可正常查详情
        assertEquals(second.getOrderNo(), detail(owner, second.getOrderNo()).getOrderNo());
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

    private CreateOrderRequest buildRequest(LocalDate checkin, LocalDate checkout) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setRoomTypeId(TYPE_ID);
        request.setCheckinDate(checkin);
        request.setCheckoutDate(checkout);
        request.setGuestName("张三");
        request.setGuestPhone("13800001111");
        return request;
    }

    private OrderInfo createOrder(LoginUser user, LocalDate checkin, LocalDate checkout) {
        return createOrder(user, buildRequest(checkin, checkout));
    }

    private OrderInfo createOrder(LoginUser user, CreateOrderRequest request) {
        UserContext.set(user);
        try {
            return orderService.create(request);
        } finally {
            UserContext.clear();
        }
    }

    private List<OrderInfo> listMine(LoginUser user) {
        UserContext.set(user);
        try {
            return orderService.listMine();
        } finally {
            UserContext.clear();
        }
    }

    private OrderInfo detail(LoginUser user, String orderNo) {
        UserContext.set(user);
        try {
            return orderService.detailMine(orderNo);
        } finally {
            UserContext.clear();
        }
    }

    private void cancel(LoginUser user, String orderNo) {
        UserContext.set(user);
        try {
            orderService.cancelMine(orderNo);
        } finally {
            UserContext.clear();
        }
    }

    private void forceStatus(String orderNo, OrderStatus status) {
        HotelOrder order = findByOrderNo(orderNo);
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    private HotelOrder findByOrderNo(String orderNo) {
        return orderMapper.selectOne(new QueryWrapper<HotelOrder>().eq("order_no", orderNo));
    }

    /** 区间 [checkin, checkout) 逐日 occupied_count；无库存行的日期不计入（验证未建行场景用） */
    private List<Integer> occupiedCounts(LocalDate checkin, LocalDate checkout) {
        return inventoryMapper.selectList(new QueryWrapper<DailyInventory>()
                        .eq("room_type_id", TYPE_ID)
                        .ge("stay_date", checkin)
                        .lt("stay_date", checkout)
                        .orderByAsc("stay_date"))
                .stream()
                .map(DailyInventory::getOccupiedCount)
                .toList();
    }
}
