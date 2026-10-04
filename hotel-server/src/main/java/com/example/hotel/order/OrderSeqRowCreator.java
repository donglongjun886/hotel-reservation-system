package com.example.hotel.order;

import com.example.hotel.order.mapper.OrderSeqMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 当日序号行创建器：建行必须在独立小事务中完成并立即提交（与库存行懒建同理，见 InventoryRowCreator）。
 * 若在下单事务内 INSERT 撞主键/间隙，并发首单会互相等待形成死锁（TC-G07 实测复现）。
 */
@Component
public class OrderSeqRowCreator {

    private final OrderSeqMapper orderSeqMapper;

    public OrderSeqRowCreator(OrderSeqMapper orderSeqMapper) {
        this.orderSeqMapper = orderSeqMapper;
    }

    /** 建行（已存在则忽略），独立事务立即提交；初始值 0，随后由调用方事务统一 +1 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureRow(LocalDate seqDate) {
        orderSeqMapper.insertIgnore(seqDate);
    }
}
