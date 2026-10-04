package com.example.hotel.order;

import com.example.hotel.order.mapper.OrderMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 入住时在住复查（独立小事务）。办理入住的事务在拿房间行锁之前已做过多次一致性读，
 * RR 读视图已固化——若在同事务内复查，看不到并发事务刚提交的在住订单；
 * 房间行锁（roomService.lockById）已把"同一房间的并发分配"串行化，后到者拿到锁时前者必已提交，
 * 因此用独立小事务取全新读视图做普通一致性读即可，无需加锁读（避免唯一索引间隙锁死锁）。
 * 与 InventoryRowCreator 同属"独立小事务"模式，连接池需留余量（application.yml 已注）。
 */
@Component
public class CheckedInRechecker {

    private final OrderMapper orderMapper;

    public CheckedInRechecker(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /** 该房间当前是否有在住订单；须在房间行锁持有后调用（在入住事务内调用，借独立连接执行） */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean isRoomOccupied(Long roomId) {
        return orderMapper.selectCheckedInIdByRoomId(roomId) != null;
    }
}
