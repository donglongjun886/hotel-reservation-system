package com.example.hotel.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hotel.order.entity.HotelOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface OrderMapper extends BaseMapper<HotelOrder> {

    /** 住客取消：带源状态 + user_id 条件更新，杜绝"先查后改"与越权取消 */
    @Update("UPDATE hotel_order SET status = 'CANCELLED' "
            + "WHERE order_no = #{orderNo} AND status = 'CONFIRMED' AND user_id = #{userId}")
    int cancelByGuest(@Param("orderNo") String orderNo, @Param("userId") Long userId);
}
