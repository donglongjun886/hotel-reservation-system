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

    /** 在住复查：该房间是否已有在住订单。按生成列 active_room_id 等值查询，命中唯一索引
     *  uk_order_active_room；不加锁——加锁读在记录不存在时会对唯一索引间隙加锁，两个并发入住
     *  各自锁住间隙再插入（UPDATE 触发生成列索引插入）会形成死锁。房间行锁已串行化并发分房，
     *  本查询由 CheckedInRechecker 在独立小事务中执行（全新读视图，读到的是已提交的最新状态） */
    @Select("SELECT id FROM hotel_order WHERE active_room_id = #{roomId} LIMIT 1")
    Long selectCheckedInIdByRoomId(@Param("roomId") Long roomId);
}
