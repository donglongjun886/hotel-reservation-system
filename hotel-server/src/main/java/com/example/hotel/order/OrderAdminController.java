package com.example.hotel.order;

import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.api.PageResult;
import com.example.hotel.common.api.Result;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.common.util.Validators;
import com.example.hotel.order.dto.AssignableRoomInfo;
import com.example.hotel.order.dto.CheckInPrecheckInfo;
import com.example.hotel.order.dto.CheckInRequest;
import com.example.hotel.order.dto.OrderInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 前台管理侧订单接口（仅 ADMIN 角色，由 AuthInterceptor 对 /api/admin/** 拦截） */
@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {

    private final OrderService orderService;

    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<PageResult<OrderInfo>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        if (!Validators.isValidPage(page, pageSize)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "分页参数不正确");
        }
        return Result.ok(orderService.listForAdmin(keyword, page, pageSize));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderInfo> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.detailForAdmin(orderNo));
    }

    @GetMapping("/{orderNo}/assignable-rooms")
    public Result<List<AssignableRoomInfo>> assignableRooms(@PathVariable String orderNo) {
        return Result.ok(orderService.listAssignableRooms(orderNo));
    }

    /** 入住预检（原型 P-A3）：打开详情页即展示校验结果，不做任何状态变更 */
    @GetMapping("/{orderNo}/checkin-precheck")
    public Result<CheckInPrecheckInfo> checkInPrecheck(@PathVariable String orderNo) {
        return Result.ok(orderService.checkInPrecheck(orderNo));
    }

    @PostMapping("/{orderNo}/check-in")
    public Result<OrderInfo> checkIn(@PathVariable String orderNo, @RequestBody CheckInRequest request) {
        return Result.ok(orderService.checkIn(orderNo, request));
    }

    @PostMapping("/{orderNo}/check-out")
    public Result<Void> checkOut(@PathVariable String orderNo) {
        orderService.checkOut(orderNo);
        return Result.ok();
    }
}
