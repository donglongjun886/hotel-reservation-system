package com.example.hotel.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hotel.order.entity.HotelOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface OrderMapper extends BaseMapper<HotelOrder> {

    /** 住客取消：带源状态 + user_id 条件更新，杜绝"先查后改"与越权取消 */
    @Update("UPDATE hotel_order SET status = 'CANCELLED' "
            + "WHERE order_no = #{orderNo} AND status = 'CONFIRMED' AND user_id = #{userId}")
    int cancelByGuest(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    /** 办理入住：带源状态条件更新，并发入住同一订单仅一笔生效 */
    @Update("UPDATE hotel_order SET status = 'CHECKED_IN', room_id = #{roomId}, id_card = #{idCard} "
            + "WHERE order_no = #{orderNo} AND status = 'CONFIRMED'")
    int checkIn(@Param("orderNo") String orderNo, @Param("roomId") Long roomId, @Param("idCard") String idCard);

    /** 办理退房：带源状态条件更新 */
    @Update("UPDATE hotel_order SET status = 'COMPLETED' "
            + "WHERE order_no = #{orderNo} AND status = 'CHECKED_IN'")
    int checkOut(@Param("orderNo") String orderNo);

    /** 锁内复查（当前读）：该房间是否已有在住订单；须在房间行锁持有后调用，配合行锁串行化并发分房 */
    @Select("SELECT id FROM hotel_order WHERE room_id = #{roomId} AND status = 'CHECKED_IN' LIMIT 1 FOR UPDATE")
    Long selectCheckedInIdByRoomId(@Param("roomId") Long roomId);
}
