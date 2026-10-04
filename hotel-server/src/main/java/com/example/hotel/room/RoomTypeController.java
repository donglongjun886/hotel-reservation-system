package com.example.hotel.room;

import com.example.hotel.common.api.Result;
import com.example.hotel.room.dto.RoomTypeInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    private final RoomService roomService;

    public RoomTypeController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Result<List<RoomTypeInfo>> list() {
        List<RoomTypeInfo> roomTypes = roomService.listRoomTypes().stream()
                .map(RoomTypeInfo::from)
                .toList();
        return Result.ok(roomTypes);
    }

    @GetMapping("/{id}")
    public Result<RoomTypeInfo> detail(@PathVariable Long id) {
        return Result.ok(RoomTypeInfo.from(roomService.getRoomType(id)));
    }
}
