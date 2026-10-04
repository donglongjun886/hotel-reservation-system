package com.example.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hotel.room.entity.Room;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoomMapper extends BaseMapper<Room> {

    @Select("SELECT * FROM room WHERE room_type_id = #{typeId} AND id NOT IN "
            + "(SELECT room_id FROM hotel_order WHERE status = 'CHECKED_IN' AND room_id IS NOT NULL)")
    List<Room> selectAssignable(Long typeId);

    /** 房间行锁：办理入住时对选中房间加锁，并发事务在此串行化（技术方案 §5.3） */
    @Select("SELECT * FROM room WHERE id = #{id} FOR UPDATE")
    Room selectByIdForUpdate(@Param("id") Long id);

    /** 房间列表（可按房型筛选），occupied 由在住订单推导（房间状态不入库） */
    @Select("SELECT r.*, EXISTS(SELECT 1 FROM hotel_order o WHERE o.room_id = r.id AND o.status = 'CHECKED_IN') AS occupied "
            + "FROM room r WHERE (#{typeId} IS NULL OR r.room_type_id = #{typeId}) ORDER BY r.room_no")
    List<Room> selectWithStatus(@Param("typeId") Long typeId);

    /** 该房型当前有效订单数（已确认 + 已入住），UC-09 维护约束用 */
    @Select("SELECT COUNT(*) FROM hotel_order WHERE room_type_id = #{typeId} AND status IN ('CONFIRMED', 'CHECKED_IN')")
    int countActiveOrders(@Param("typeId") Long typeId);

    /** 该房间的在住订单数（UC-09：在住房间禁止改挂其他房型） */
    @Select("SELECT COUNT(*) FROM hotel_order WHERE room_id = #{roomId} AND status = 'CHECKED_IN'")
    int countCheckedInByRoomId(@Param("roomId") Long roomId);
}
