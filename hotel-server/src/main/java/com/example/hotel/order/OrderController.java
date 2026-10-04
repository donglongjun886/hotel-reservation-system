package com.example.hotel.order;

import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.api.PageResult;
import com.example.hotel.common.api.Result;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.common.util.Validators;
import com.example.hotel.order.dto.CreateOrderRequest;
import com.example.hotel.order.dto.OrderInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<OrderInfo> create(@RequestBody CreateOrderRequest request) {
        return Result.ok(orderService.create(request));
    }

    @GetMapping("/mine")
    public Result<PageResult<OrderInfo>> mine(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        if (!Validators.isValidPage(page, pageSize)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "分页参数不正确");
        }
        return Result.ok(orderService.listMine(page, pageSize));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderInfo> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.detailMine(orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelMine(orderNo);
        return Result.ok();
    }
}
