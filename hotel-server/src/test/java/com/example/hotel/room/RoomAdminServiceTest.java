package com.example.hotel.room;

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
import com.example.hotel.inventory.InventoryService;
import com.example.hotel.inventory.entity.DailyInventory;
import com.example.hotel.inventory.mapper.InventoryMapper;
import com.example.hotel.order.OrderService;
import com.example.hotel.order.dto.CreateOrderRequest;
import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.mapper.OrderMapper;
import com.example.hotel.room.entity.Room;
import com.example.hotel.room.entity.RoomType;
import com.example.hotel.room.mapper.RoomMapper;
import com.example.hotel.room.mapper.RoomTypeMapper;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 对应测试用例：TC-F03~F08（房型/房间维护，UC-09）。
 * 大床房 room_type_id=1（3 间）、双床房 room_type_id=2（3 间）、行政套房 room_type_id=4（2 间）。
 * 改挂用例在 cleanup 中恢复预置数据（A103 挂回大床房并触发库存 total 再同步）。
 */
@SpringBootTest
class RoomAdminServiceTest {

    private static final Long TYPE_A = 1L; // 大床房，3 间
    private static final Long TYPE_B = 2L; // 双床房，3 间
    private static final Long TYPE_D = 4L; // 行政套房，2 间
    private static final LocalDate FAR_FUTURE = LocalDate.of(2099, 1, 1);

    @Autowired
    private RoomService roomService;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private RoomTypeMapper roomTypeMapper;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryMapper inventoryMapper;
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

    private JdbcTemplate jdbc;

    @Autowired
    void initJdbc(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private final List<Long> createdUserIds = new CopyOnWriteArrayList<>();
    private final List<Long> createdRoomIds = new CopyOnWriteArrayList<>();
    private final List<Long> createdRoomTypeIds = new CopyOnWriteArrayList<>();

    @AfterEach
    void cleanup() {
        if (!createdUserIds.isEmpty()) {
            orderMapper.delete(new QueryWrapper<HotelOrder>().in("user_id", createdUserIds));
            authTokenMapper.delete(new QueryWrapper<AuthToken>().in("user_id", createdUserIds));
            userMapper.delete(new QueryWrapper<User>().in("id", createdUserIds));
            createdUserIds.clear();
        }
        // 恢复预置数据：A103 若被改挂则挂回大床房（updateRoom 会触发双方库存 total 再同步）
        Room a103 = roomMapper.selectOne(new QueryWrapper<Room>().eq("room_no", "A103"));
        if (a103 != null && !TYPE_A.equals(a103.getRoomTypeId())) {
            roomService.updateRoom(a103.getId(), "A103", TYPE_A);
        }
        if (!createdRoomIds.isEmpty()) {
            roomMapper.delete(new QueryWrapper<Room>().in("id", createdRoomIds));
            createdRoomIds.clear();
        }
        if (!createdRoomTypeIds.isEmpty()) {
            roomTypeMapper.delete(new QueryWrapper<RoomType>().in("id", createdRoomTypeIds));
            inventoryMapper.delete(new QueryWrapper<DailyInventory>().in("room_type_id", createdRoomTypeIds));
            createdRoomTypeIds.clear();
        }
        inventoryMapper.delete(new QueryWrapper<DailyInventory>().ge("stay_date", FAR_FUTURE));
        // 修正当天窗口库存占用（订单已删除，occupied 归零）
        jdbc.update("UPDATE daily_inventory di SET di.occupied_count = ("
                        + "SELECT COUNT(*) FROM hotel_order o "
                        + "WHERE o.room_type_id = di.room_type_id AND o.status IN ('CONFIRMED', 'CHECKED_IN') "
                        + "AND o.checkin_date <= di.stay_date AND o.checkout_date > di.stay_date) "
                        + "WHERE di.stay_date >= ? AND di.stay_date <= ?",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(10));
    }

    @Test
    void createRoomTypeAndRooms_syncsToGuestSide() {
        // TC-F03：新增房型并加 2 个房间，住客侧可见、剩余可订数量 = 2（库存行随维护预创建）
        RoomType suite = roomService.createRoomType("套房 S", new BigDecimal("588.00"), "豪华套房");
        createdRoomTypeIds.add(suite.getId());
        createdRoomIds.add(roomService.createRoom("S101", suite.getId()).getId());
        createdRoomIds.add(roomService.createRoom("S102", suite.getId()).getId());

        assertTrue(roomService.listRoomTypes().stream().anyMatch(t -> t.getName().equals("套房 S")));
        LocalDate today = LocalDate.now();
        assertEquals(2, inventoryService.queryAvailability(suite.getId(), today, today.plusDays(2)));
        // 库存行已按决策 13 预创建
        assertTrue(inventoryMapper.selectCount(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", suite.getId()).eq("stay_date", today)) > 0);
    }

    @Test
    void updateRoomType_syncsInfo() {
        // TC-F04：修改房型名称/单价/介绍后住客侧展示更新
        RoomType suite = roomService.createRoomType("临时房型", new BigDecimal("100.00"), "旧介绍");
        createdRoomTypeIds.add(suite.getId());

        roomService.updateRoomType(suite.getId(), "精品房型", new BigDecimal("288.50"), "新介绍");

        RoomType updated = roomService.getRoomType(suite.getId());
        assertEquals("精品房型", updated.getName());
        assertEquals(0, new BigDecimal("288.50").compareTo(updated.getPrice()));
        assertEquals("新介绍", updated.getDescription());
    }

    @Test
    void roomTypeForm_validation() {
        // TC-F05：名称/单价为空或单价非正数，新增与修改均拒绝，文案对齐原型 P-A4
        RoomType suite = roomService.createRoomType("待编辑房型", new BigDecimal("100.00"), "");
        createdRoomTypeIds.add(suite.getId());

        List<Runnable> invalidOps = List.of(
                () -> roomService.createRoomType(null, new BigDecimal("100.00"), ""),
                () -> roomService.createRoomType("  ", new BigDecimal("100.00"), ""),
                () -> roomService.createRoomType("X", null, ""),
                () -> roomService.createRoomType("X", BigDecimal.ZERO, ""),
                () -> roomService.createRoomType("X", new BigDecimal("-1.00"), ""),
                () -> roomService.updateRoomType(suite.getId(), null, new BigDecimal("100.00"), ""),
                () -> roomService.updateRoomType(suite.getId(), "X", BigDecimal.ZERO, ""));
        for (Runnable op : invalidOps) {
            BizException e = assertThrows(BizException.class, op::run);
            assertEquals("请填写完整的房型信息", e.getMessage());
        }
        // 校验失败后原数据未被修改
        assertEquals("待编辑房型", roomService.getRoomType(suite.getId()).getName());
    }

    @Test
    void roomNo_requiredAndUnique() {
        // TC-F06：房间号为空或重复，新增与编辑均拒绝，文案对齐原型 P-A5
        Room a102 = findRoom("A102");

        BizException blank = assertThrows(BizException.class, () -> roomService.createRoom("  ", TYPE_A));
        assertEquals("房间号不能为空且不可重复", blank.getMessage());
        BizException duplicated = assertThrows(BizException.class, () -> roomService.createRoom("A101", TYPE_A));
        assertEquals("房间号不能为空且不可重复", duplicated.getMessage());
        BizException editDuplicated = assertThrows(BizException.class,
                () -> roomService.updateRoom(a102.getId(), "A101", TYPE_A));
        assertEquals("房间号不能为空且不可重复", editDuplicated.getMessage());
        // 编辑自己房间号不变（排除自身）合法；不存在的房型拒绝
        roomService.updateRoom(a102.getId(), "A102", TYPE_A);
        BizException noType = assertThrows(BizException.class, () -> roomService.createRoom("T901", 999L));
        assertEquals("房型不存在", noType.getMessage());
    }

    @Test
    void reassignRoom_belowActiveOrders_rejected() {
        // TC-F07：大床房 3 间、有效订单 = 3，把 A103 改挂到行政套房（减到 2 间）被拒
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 3; i++) {
            createOrder(user, today, today.plusDays(2));
        }
        Room a103 = findRoom("A103");

        BizException e = assertThrows(BizException.class, () -> roomService.updateRoom(a103.getId(), "A103", TYPE_D));

        assertEquals("该房型存在有效订单，请先处理相关订单", e.getMessage());
        // 房间与库存均未变化
        assertEquals(TYPE_A, findRoom("A103").getRoomTypeId());
        assertEquals(3, totalCount(TYPE_A, today.plusDays(100)));
        assertEquals(2, totalCount(TYPE_D, today.plusDays(100)));
    }

    @Test
    void reassignRoom_bothSidesSynced() {
        // TC-F08：大床房有效订单 2、共 3 间，A103 改挂到双床房成功（源 2 ≥ 有效订单 2），
        // 双方未来库存行 total 同步：大床房 3→2、双床房 3→4
        LoginUser user = newGuest();
        LocalDate today = LocalDate.now();
        createOrder(user, today, today.plusDays(2));
        createOrder(user, today, today.plusDays(2));
        Room a103 = findRoom("A103");

        roomService.updateRoom(a103.getId(), "A103", TYPE_B);

        assertEquals(TYPE_B, findRoom("A103").getRoomTypeId());
        assertEquals(2, totalCount(TYPE_A, today.plusDays(100)));
        assertEquals(4, totalCount(TYPE_B, today.plusDays(100)));
        assertEquals(2, inventoryService.queryAvailability(TYPE_A, today.plusDays(100), today.plusDays(101)));
        assertEquals(4, inventoryService.queryAvailability(TYPE_B, today.plusDays(100), today.plusDays(101)));
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

    private void createOrder(LoginUser user, LocalDate checkin, LocalDate checkout) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setRequestNo(UUID.randomUUID().toString());
        request.setRoomTypeId(TYPE_A);
        request.setCheckinDate(checkin);
        request.setCheckoutDate(checkout);
        request.setGuestName("张三");
        request.setGuestPhone("13800001111");
        UserContext.set(user);
        try {
            orderService.create(request);
        } finally {
            UserContext.clear();
        }
    }

    private Room findRoom(String roomNo) {
        return roomMapper.selectOne(new QueryWrapper<Room>().eq("room_no", roomNo));
    }

    private int totalCount(Long typeId, LocalDate stayDate) {
        DailyInventory row = inventoryMapper.selectOne(new QueryWrapper<DailyInventory>()
                .eq("room_type_id", typeId).eq("stay_date", stayDate));
        return row == null ? -1 : row.getTotalCount();
    }
}
