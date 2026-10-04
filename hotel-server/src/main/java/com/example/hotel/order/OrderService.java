package com.example.hotel.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.context.LoginUser;
import com.example.hotel.common.context.UserContext;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.common.util.Validators;
import com.example.hotel.inventory.InventoryService;
import com.example.hotel.order.dto.AssignableRoomInfo;
import com.example.hotel.order.dto.CheckInPrecheckInfo;
import com.example.hotel.order.dto.CheckInRequest;
import com.example.hotel.order.dto.CreateOrderRequest;
import com.example.hotel.order.dto.OrderInfo;
import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.entity.OrderStatus;
import com.example.hotel.order.mapper.OrderMapper;
import com.example.hotel.room.RoomService;
import com.example.hotel.room.entity.Room;
import com.example.hotel.room.entity.RoomType;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderNoGenerator orderNoGenerator;
    private final InventoryService inventoryService;
    private final RoomService roomService;

    public OrderService(OrderMapper orderMapper, OrderNoGenerator orderNoGenerator,
                        InventoryService inventoryService, RoomService roomService) {
        this.orderMapper = orderMapper;
        this.orderNoGenerator = orderNoGenerator;
        this.inventoryService = inventoryService;
        this.roomService = roomService;
    }

    /**
     * 创建预订：BR-01 业务规则先校验后占用；金额下单时固化（单价 × 晚数，BR-03）；
     * 一个事务内生成订单号 → 逐日占用库存（中途订满整单回滚）→ 插单。
     * 幂等：同一 request_no 重复提交/重试返回首次创建的订单，不重复占库存；
     * 先查兜底并发，唯一索引 uk_order_request_no 兜底并发下的重复插入。
     */
    @Transactional
    public OrderInfo create(CreateOrderRequest request) {
        LoginUser user = UserContext.require();
        validateBookingRules(request);
        HotelOrder replay = orderMapper.selectOne(new QueryWrapper<HotelOrder>()
                .eq("request_no", request.getRequestNo())
                .eq("user_id", user.id()));
        if (replay != null) {
            return OrderInfo.from(replay, roomService.getRoomType(replay.getRoomTypeId()).getName());
        }
        RoomType roomType = roomService.getRoomType(request.getRoomTypeId());
        int nights = (int) ChronoUnit.DAYS.between(request.getCheckinDate(), request.getCheckoutDate());
        BigDecimal amount = roomType.getPrice().multiply(BigDecimal.valueOf(nights));

        String orderNo = orderNoGenerator.next();
        inventoryService.tryOccupy(request.getRoomTypeId(), request.getCheckinDate(), request.getCheckoutDate());

        HotelOrder order = new HotelOrder();
        order.setOrderNo(orderNo);
        order.setRequestNo(request.getRequestNo());
        order.setUserId(user.id());
        order.setGuestName(request.getGuestName().trim());
        order.setGuestPhone(request.getGuestPhone());
        order.setRoomTypeId(request.getRoomTypeId());
        order.setCheckinDate(request.getCheckinDate());
        order.setCheckoutDate(request.getCheckoutDate());
        order.setNights(nights);
        order.setAmount(amount);
        order.setStatus(OrderStatus.CONFIRMED);
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // 并发重复提交撞上唯一索引：回滚本事务已占的库存与序号，改查首次创建的订单返回
            HotelOrder first = rollbackAndFindByRequestNo(request.getRequestNo(), user.id());
            if (first == null) {
                throw e;
            }
            return OrderInfo.from(first, roomService.getRoomType(first.getRoomTypeId()).getName());
        }
        return OrderInfo.from(order, roomType.getName());
    }

    /** 我的订单：一律按 token 中的 user_id 过滤，不接受前端传 userId */
    public List<OrderInfo> listMine() {
        LoginUser user = UserContext.require();
        List<HotelOrder> orders = orderMapper.selectList(new QueryWrapper<HotelOrder>()
                .eq("user_id", user.id())
                .orderByDesc("id"));
        Map<Long, String> typeNames = roomService.listRoomTypes().stream()
                .collect(Collectors.toMap(RoomType::getId, RoomType::getName));
        return orders.stream()
                .map(order -> OrderInfo.from(order, typeNames.get(order.getRoomTypeId())))
                .toList();
    }

    /** 我的订单详情：仅本人订单可见，他人订单按不存在处理（不泄露） */
    public OrderInfo detailMine(String orderNo) {
        HotelOrder order = findMine(orderNo);
        return OrderInfo.from(order, roomService.getRoomType(order.getRoomTypeId()).getName(), resolveRoomNo(order));
    }

    /** 取消：带源状态 + user_id 条件更新，成功后逐日释放库存（BR-04 宽松取消） */
    @Transactional
    public void cancelMine(String orderNo) {
        LoginUser user = UserContext.require();
        HotelOrder order = findMine(orderNo);
        if (orderMapper.cancelByGuest(orderNo, user.id()) == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_CHANGED);
        }
        inventoryService.release(order.getRoomTypeId(), order.getCheckinDate(), order.getCheckoutDate());
    }

    // ---------- 前台侧（admin） ----------

    /** 前台订单查询：keyword 为空查全部，否则按订单号或订单上的住客手机号精确匹配（BR-01 代订口径） */
    public List<OrderInfo> listForAdmin(String keyword) {
        QueryWrapper<HotelOrder> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            query.and(q -> q.eq("order_no", value).or().eq("guest_phone", value));
        }
        query.orderByDesc("id");
        List<HotelOrder> orders = orderMapper.selectList(query);
        Map<Long, String> typeNames = roomService.listRoomTypes().stream()
                .collect(Collectors.toMap(RoomType::getId, RoomType::getName));
        return orders.stream()
                .map(order -> OrderInfo.from(order, typeNames.get(order.getRoomTypeId())))
                .toList();
    }

    /** 前台订单详情：不限归属，已入住订单追加房间号与身份证号 */
    public OrderInfo detailForAdmin(String orderNo) {
        HotelOrder order = findByOrderNo(orderNo);
        return OrderInfo.from(order, roomService.getRoomType(order.getRoomTypeId()).getName(), resolveRoomNo(order));
    }

    /** 该订单房型当前可分配房间（不含在住房间，TC-D08） */
    public List<AssignableRoomInfo> listAssignableRooms(String orderNo) {
        HotelOrder order = findByOrderNo(orderNo);
        return roomService.listAssignableRooms(order.getRoomTypeId()).stream()
                .map(AssignableRoomInfo::from)
                .toList();
    }

    /** 入住预检（原型 P-A3）：打开详情页即展示"可办理/不可办理及原因"，与 checkIn 共用同一套阻断原因 */
    public CheckInPrecheckInfo checkInPrecheck(String orderNo) {
        HotelOrder order = findByOrderNo(orderNo);
        List<String> reasons = checkInBlockingReasons(order);
        return new CheckInPrecheckInfo(reasons.isEmpty(), reasons);
    }

    /**
     * 办理入住（BR-06，技术方案 §5.3）：四项校验 → 房间行锁 → 锁内复查无在住订单（当前读）
     * → 带源状态条件更新。并发分配同一房间在行锁处串行化，后到者复查时拒绝；
     * 生成列唯一索引（uk_order_active_room）在 DB 层兜底。
     */
    @Transactional
    public OrderInfo checkIn(String orderNo, CheckInRequest request) {
        HotelOrder order = findByOrderNo(orderNo);
        validateCheckInRules(order, request);
        Room room = roomService.getByRoomNo(request.getRoomNo().trim());
        if (!room.getRoomTypeId().equals(order.getRoomTypeId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "所选房间不属于该订单房型");
        }
        roomService.lockById(room.getId());
        if (orderMapper.selectCheckedInIdByRoomId(room.getId()) != null) {
            throw new BizException(ErrorCode.CHECKIN_NOT_ALLOWED, "房间刚被分配，请刷新重选");
        }
        if (orderMapper.checkIn(orderNo, room.getId(), request.getIdCard().trim()) == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_CHANGED);
        }
        HotelOrder checkedIn = findByOrderNo(orderNo);
        return OrderInfo.from(checkedIn, roomService.getRoomType(checkedIn.getRoomTypeId()).getName(), room.getRoomNo());
    }

    /** 办理退房：带源状态条件更新，成功后逐日释放库存（提前退房剩余晚数立即可再售，BR-07） */
    @Transactional
    public void checkOut(String orderNo) {
        HotelOrder order = findByOrderNo(orderNo);
        if (orderMapper.checkOut(orderNo) == 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_CHANGED, "订单状态已变更，请刷新");
        }
        inventoryService.release(order.getRoomTypeId(), order.getCheckinDate(), order.getCheckoutDate());
    }

    /** BR-06 入住校验：阻断原因（状态/窗口/空房）→ 入参校验（身份证、房间），文案对齐原型 P-A3 */
    private void validateCheckInRules(HotelOrder order, CheckInRequest request) {
        List<String> blocking = checkInBlockingReasons(order);
        if (!blocking.isEmpty()) {
            throw new BizException(ErrorCode.CHECKIN_NOT_ALLOWED, blocking.get(0));
        }
        if (request == null || !Validators.isIdCard(request.getIdCard())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "身份证号格式不正确");
        }
        if (request.getRoomNo() == null || request.getRoomNo().isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请选择要分配的房间");
        }
    }

    /** 订单侧的入住阻断原因（状态、入住窗口、空房），办理入住与预检共用；状态不符时其余判断无意义，直接短路 */
    private List<String> checkInBlockingReasons(HotelOrder order) {
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            return List.of("该订单当前状态不可办理入住");
        }
        List<String> reasons = new ArrayList<>();
        LocalDate today = LocalDate.now();
        if (today.isBefore(order.getCheckinDate())) {
            reasons.add("未到入住日期（入住日：" + order.getCheckinDate() + "）");
        }
        if (!today.isBefore(order.getCheckoutDate())) {
            reasons.add("已超过可入住时间（离店日：" + order.getCheckoutDate() + "），请引导客人取消重订");
        }
        if (roomService.listAssignableRooms(order.getRoomTypeId()).isEmpty()) {
            reasons.add("该房型当前无空闲房间");
        }
        return reasons;
    }

    private HotelOrder findMine(String orderNo) {
        LoginUser user = UserContext.require();
        HotelOrder order = orderMapper.selectOne(new QueryWrapper<HotelOrder>()
                .eq("order_no", orderNo)
                .eq("user_id", user.id()));
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private HotelOrder findByOrderNo(String orderNo) {
        HotelOrder order = orderMapper.selectOne(new QueryWrapper<HotelOrder>().eq("order_no", orderNo));
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    /** 标记当前事务回滚（撤销已占库存与序号）后按幂等号查单，用于并发重复提交撞上唯一索引的场景 */
    private HotelOrder rollbackAndFindByRequestNo(String requestNo, Long userId) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return orderMapper.selectOne(new QueryWrapper<HotelOrder>()
                .eq("request_no", requestNo)
                .eq("user_id", userId));
    }

    private String resolveRoomNo(HotelOrder order) {
        return order.getRoomId() != null ? roomService.getRoom(order.getRoomId()).getRoomNo() : null;
    }

    /** BR-01 预订规则：校验失败直接拒绝，不进入库存占用环节 */
    private void validateBookingRules(CreateOrderRequest request) {
        if (request.getRequestNo() == null || request.getRequestNo().isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请求号不能为空");
        }
        if (request.getRoomTypeId() == null || request.getCheckinDate() == null || request.getCheckoutDate() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请选择入住和离店日期");
        }
        if (!request.getCheckoutDate().isAfter(request.getCheckinDate())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "离店日期必须晚于入住日期");
        }
        if (request.getCheckinDate().isBefore(LocalDate.now())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "入住日期不能早于今天");
        }
        if (request.getGuestName() == null || request.getGuestName().isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请输入住客姓名");
        }
        if (!Validators.isPhone(request.getGuestPhone())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "手机号格式不正确");
        }
    }
}
