package com.example.hotel.room;

import com.example.hotel.common.api.Result;
import com.example.hotel.room.dto.RoomTypeAvailability;
import com.example.hotel.room.dto.RoomTypeInfo;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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

    /** 各房型日期区间剩余可订量；精确路径优先于 /{id} 模板匹配，AuthInterceptor 对 /api/room-types 的 GET 一律放行 */
    @GetMapping("/availability")
    public Result<List<RoomTypeAvailability>> availability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout) {
        return Result.ok(roomService.queryAvailability(checkin, checkout));
    }

    @GetMapping("/{id}")
    public Result<RoomTypeInfo> detail(@PathVariable Long id) {
        return Result.ok(RoomTypeInfo.from(roomService.getRoomType(id)));
    }
}
