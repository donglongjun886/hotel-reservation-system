package com.example.hotel.order;

import com.example.hotel.order.mapper.OrderSeqMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 订单号生成（技术方案 §5.2）：每日序号表，格式 HR+yyyyMMdd-NNNN。
 * 须在调用方事务内调用：序号随订单事务提交，回滚不复用（允许跳号，唯一性优先）。
 * 当日序号行由独立小事务预建（同决策 13 的库存行思路），本事务内只做条件更新 + 一致性读，
 * 避免并发首单在事务内 INSERT 撞主键/间隙引发死锁。
 */
@Component
public class OrderNoGenerator {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderSeqMapper orderSeqMapper;
    private final OrderSeqRowCreator rowCreator;

    public OrderNoGenerator(OrderSeqMapper orderSeqMapper, OrderSeqRowCreator rowCreator) {
        this.orderSeqMapper = orderSeqMapper;
        this.rowCreator = rowCreator;
    }

    public String next() {
        LocalDate today = LocalDate.now();
        // 一致性读判断行是否存在（不加锁，避免对不存在行的加锁 UPDATE 产生间隙锁）；
        // 当日行一般由历史订单创建，未命中即当日首单
        if (orderSeqMapper.selectValue(today) == null) {
            rowCreator.ensureRow(today);
        }
        orderSeqMapper.incr(today);
        Long value = orderSeqMapper.selectValue(today);
        return "HR" + today.format(DAY) + "-" + String.format("%04d", value);
    }
}
