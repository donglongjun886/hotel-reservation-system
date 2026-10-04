package com.example.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hotel.room.entity.Room;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoomMapper extends BaseMapper<Room> {

    @Select("SELECT * FROM room WHERE room_type_id = #{typeId} AND id NOT IN "
            + "(SELECT room_id FROM hotel_order WHERE status = 'CHECKED_IN' AND room_id IS NOT NULL)")
    List<Room> selectAssignable(Long typeId);
}
