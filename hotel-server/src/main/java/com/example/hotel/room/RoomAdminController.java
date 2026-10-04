package com.example.hotel.room;

import com.example.hotel.common.api.Result;
import com.example.hotel.room.dto.RoomAdminInfo;
import com.example.hotel.room.dto.RoomSaveRequest;
import com.example.hotel.room.dto.RoomTypeAdminInfo;
import com.example.hotel.room.dto.RoomTypeSaveRequest;
import com.example.hotel.room.entity.Room;
import com.example.hotel.room.entity.RoomType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 前台管理侧房型/房间维护接口（UC-09；仅 ADMIN 角色，由 AuthInterceptor 对 /api/admin/** 拦截） */
@RestController
@RequestMapping("/api/admin")
public class RoomAdminController {

    private final RoomService roomService;

    public RoomAdminController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/room-types")
    public Result<List<RoomTypeAdminInfo>> listRoomTypes() {
        List<RoomTypeAdminInfo> roomTypes = roomService.listRoomTypes().stream()
                .map(roomType -> RoomTypeAdminInfo.from(roomType, roomService.countRooms(roomType.getId())))
                .toList();
        return Result.ok(roomTypes);
    }

    @PostMapping("/room-types")
    public Result<RoomTypeAdminInfo> createRoomType(@RequestBody RoomTypeSaveRequest request) {
        RoomType roomType = roomService.createRoomType(request.getName(), request.toPriceYuan(), request.getDescription());
        return Result.ok(RoomTypeAdminInfo.from(roomType, roomService.countRooms(roomType.getId())));
    }

    @PutMapping("/room-types/{id}")
    public Result<RoomTypeAdminInfo> updateRoomType(@PathVariable Long id, @RequestBody RoomTypeSaveRequest request) {
        RoomType roomType = roomService.updateRoomType(id, request.getName(), request.toPriceYuan(), request.getDescription());
        return Result.ok(RoomTypeAdminInfo.from(roomType, roomService.countRooms(roomType.getId())));
    }

    @GetMapping("/rooms")
    public Result<List<RoomAdminInfo>> listRooms(@RequestParam(required = false) Long roomTypeId) {
        Map<Long, String> typeNames = roomService.listRoomTypes().stream()
                .collect(Collectors.toMap(RoomType::getId, RoomType::getName));
        List<RoomAdminInfo> rooms = roomService.listRooms(roomTypeId).stream()
                .map(room -> RoomAdminInfo.from(room, typeNames.get(room.getRoomTypeId())))
                .toList();
        return Result.ok(rooms);
    }

    @PostMapping("/rooms")
    public Result<RoomAdminInfo> createRoom(@RequestBody RoomSaveRequest request) {
        Room room = roomService.createRoom(request.getRoomNo(), request.getRoomTypeId());
        return Result.ok(RoomAdminInfo.from(room, roomService.getRoomType(room.getRoomTypeId()).getName()));
    }

    @PutMapping("/rooms/{id}")
    public Result<RoomAdminInfo> updateRoom(@PathVariable Long id, @RequestBody RoomSaveRequest request) {
        Room room = roomService.updateRoom(id, request.getRoomNo(), request.getRoomTypeId());
        return Result.ok(RoomAdminInfo.from(room, roomService.getRoomType(room.getRoomTypeId()).getName()));
    }
}
