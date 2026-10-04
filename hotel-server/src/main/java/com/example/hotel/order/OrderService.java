package com.example.hotel.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.context.LoginUser;
import com.example.hotel.common.context.UserContext;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.common.util.Validators;
import com.example.hotel.inventory.InventoryService;
import com.example.hotel.order.dto.CreateOrderRequest;
import com.example.hotel.order.dto.OrderInfo;
import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.entity.OrderStatus;
import com.example.hotel.order.mapper.OrderMapper;
import com.example.hotel.room.RoomService;
import com.example.hotel.room.entity.RoomType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
     */
    @Transactional
    public OrderInfo create(CreateOrderRequest request) {
        LoginUser user = UserContext.require();
        validateBookingRules(request);
        RoomType roomType = roomService.getRoomType(request.getRoomTypeId());
        int nights = (int) ChronoUnit.DAYS.between(request.getCheckinDate(), request.getCheckoutDate());
        BigDecimal amount = roomType.getPrice().multiply(BigDecimal.valueOf(nights));

        String orderNo = orderNoGenerator.next();
        inventoryService.tryOccupy(request.getRoomTypeId(), request.getCheckinDate(), request.getCheckoutDate());

        HotelOrder order = new HotelOrder();
        order.setOrderNo(orderNo);
        order.setUserId(user.id());
        order.setGuestName(request.getGuestName().trim());
        order.setGuestPhone(request.getGuestPhone());
        order.setRoomTypeId(request.getRoomTypeId());
        order.setCheckinDate(request.getCheckinDate());
        order.setCheckoutDate(request.getCheckoutDate());
        order.setNights(nights);
        order.setAmount(amount);
        order.setStatus(OrderStatus.CONFIRMED);
        orderMapper.insert(order);
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
        return OrderInfo.from(order, roomService.getRoomType(order.getRoomTypeId()).getName());
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

    /** BR-01 预订规则：校验失败直接拒绝，不进入库存占用环节 */
    private void validateBookingRules(CreateOrderRequest request) {
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
