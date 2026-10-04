package com.example.hotel.room;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.room.entity.Room;
import com.example.hotel.room.entity.RoomType;
import com.example.hotel.room.mapper.RoomMapper;
import com.example.hotel.room.mapper.RoomTypeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomTypeMapper roomTypeMapper;
    private final RoomMapper roomMapper;

    public RoomService(RoomTypeMapper roomTypeMapper, RoomMapper roomMapper) {
        this.roomTypeMapper = roomTypeMapper;
        this.roomMapper = roomMapper;
    }

    public List<RoomType> listRoomTypes() {
        return roomTypeMapper.selectList(new QueryWrapper<RoomType>().orderByAsc("id"));
    }

    public RoomType getRoomType(Long id) {
        RoomType roomType = roomTypeMapper.selectById(id);
        if (roomType == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "房型不存在");
        }
        return roomType;
    }

    public int countRooms(Long typeId) {
        return Math.toIntExact(roomMapper.selectCount(new QueryWrapper<Room>().eq("room_type_id", typeId)));
    }

    /** 该房型当前可分配房间：全部房间 − 被已入住订单占用的房间（房间状态不入库，由在住订单推导） */
    public List<Room> listAssignableRooms(Long typeId) {
        return roomMapper.selectAssignable(typeId);
    }
}
