package com.example.hotel.room;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.exception.BizException;
import com.example.hotel.inventory.InventoryService;
import com.example.hotel.room.dto.RoomTypeAvailability;
import com.example.hotel.room.entity.Room;
import com.example.hotel.room.entity.RoomType;
import com.example.hotel.room.mapper.RoomMapper;
import com.example.hotel.room.mapper.RoomTypeMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomTypeMapper roomTypeMapper;
    private final RoomMapper roomMapper;
    private final InventoryService inventoryService;

    public RoomService(RoomTypeMapper roomTypeMapper, RoomMapper roomMapper, InventoryService inventoryService) {
        this.roomTypeMapper = roomTypeMapper;
        this.roomMapper = roomMapper;
        this.inventoryService = inventoryService;
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

    /** 各房型在 [checkin, checkout) 区间的剩余可订量（技术方案 §6.1 公开接口，P-C1 列表页） */
    public List<RoomTypeAvailability> queryAvailability(LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请选择入住和离店日期");
        }
        if (!checkout.isAfter(checkin)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "离店日期必须晚于入住日期");
        }
        return listRoomTypes().stream()
                .map(type -> new RoomTypeAvailability(type.getId(),
                        inventoryService.queryAvailability(type.getId(), checkin, checkout)))
                .toList();
    }

    public int countRooms(Long typeId) {
        return Math.toIntExact(roomMapper.selectCount(new QueryWrapper<Room>().eq("room_type_id", typeId)));
    }

    /** 该房型当前可分配房间：全部房间 − 被已入住订单占用的房间（房间状态不入库，由在住订单推导） */
    public List<Room> listAssignableRooms(Long typeId) {
        return roomMapper.selectAssignable(typeId);
    }

    public Room getRoom(Long id) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "房间不存在");
        }
        return room;
    }

    public Room getByRoomNo(String roomNo) {
        Room room = roomMapper.selectOne(new QueryWrapper<Room>().eq("room_no", roomNo));
        if (room == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "房间不存在");
        }
        return room;
    }

    /** 房间行锁（SELECT ... FOR UPDATE）：办理入住时防止同一房间被两笔订单并发分配，须在事务内调用 */
    public Room lockById(Long id) {
        return roomMapper.selectByIdForUpdate(id);
    }

    /** 房间列表（可按房型筛选），带在住状态推导（occupied = 被已入住订单占用） */
    public List<Room> listRooms(Long typeId) {
        return roomMapper.selectWithStatus(typeId);
    }

    /** 新增房型：表单校验通过后落库，并同步预创建未来库存行（决策 13） */
    @Transactional
    public RoomType createRoomType(String name, BigDecimal price, String description) {
        validateRoomTypeForm(name, price);
        RoomType roomType = new RoomType();
        roomType.setName(name.trim());
        roomType.setPrice(price);
        roomType.setDescription(description == null ? "" : description.trim());
        roomTypeMapper.insert(roomType);
        inventoryService.syncTotalForFutureDates(roomType.getId());
        return roomType;
    }

    /** 修改房型：只改名称/单价/介绍，不涉及房间数与库存；已生成订单金额固化不回溯（BR-03） */
    @Transactional
    public RoomType updateRoomType(Long id, String name, BigDecimal price, String description) {
        validateRoomTypeForm(name, price);
        RoomType roomType = getRoomType(id);
        roomType.setName(name.trim());
        roomType.setPrice(price);
        roomType.setDescription(description == null ? "" : description.trim());
        roomTypeMapper.updateById(roomType);
        return roomType;
    }

    /** 新增房间：房间号唯一校验；落库后同步该房型未来库存行 total（UC-09） */
    @Transactional
    public Room createRoom(String roomNo, Long roomTypeId) {
        validateRoomNo(roomNo, null);
        getRoomType(roomTypeId);
        Room room = new Room();
        room.setRoomNo(roomNo.trim());
        room.setRoomTypeId(roomTypeId);
        try {
            roomMapper.insert(room);
        } catch (DuplicateKeyException e) {
            // 并发新增撞房间号唯一索引：文案与先查一致
            throw new BizException(ErrorCode.PARAM_INVALID, "房间号不能为空且不可重复");
        }
        inventoryService.syncTotalForFutureDates(roomTypeId);
        return room;
    }

    /**
     * 修改房间（房间号 / 所属房型）。在住房间禁止改挂（避免订单房型与房间当前房型脱节）；
     * 改挂其他房型时按 UC-09 先校验"源房型新房间数 ≥ 当前有效订单数"，
     * 再落库并同步源、目标双方的未来库存行 total。
     */
    @Transactional
    public Room updateRoom(Long id, String roomNo, Long roomTypeId) {
        Room room = getRoom(id);
        validateRoomNo(roomNo, id);
        getRoomType(roomTypeId);
        Long oldTypeId = room.getRoomTypeId();
        boolean reAssign = !oldTypeId.equals(roomTypeId);
        if (reAssign) {
            if (roomMapper.countCheckedInByRoomId(id) > 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "该房间正在入住中，不可调整");
            }
            ensureRoomCountNotBelowActiveOrders(oldTypeId, countRooms(oldTypeId) - 1);
        }
        room.setRoomNo(roomNo.trim());
        room.setRoomTypeId(roomTypeId);
        try {
            roomMapper.updateById(room);
        } catch (DuplicateKeyException e) {
            // 并发维护撞房间号唯一索引：文案与先查一致
            throw new BizException(ErrorCode.PARAM_INVALID, "房间号不能为空且不可重复");
        }
        if (reAssign) {
            // 一次调用同步源、目标双方：建行（独立事务）必须先于双方任何库存行加锁，见 InventoryService
            inventoryService.syncTotalForFutureDates(oldTypeId, roomTypeId);
        }
        return room;
    }

    /** 房型表单校验（原型 P-A4）：名称/单价为空或单价非正数一律拒绝 */
    private void validateRoomTypeForm(String name, BigDecimal price) {
        if (name == null || name.isBlank() || price == null || price.signum() <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请填写完整的房型信息");
        }
    }

    /** 房间号校验（原型 P-A5）：不能为空且不可重复（excludeId 为编辑时排除自身） */
    private void validateRoomNo(String roomNo, Long excludeId) {
        if (roomNo == null || roomNo.isBlank() || existsRoomNo(roomNo.trim(), excludeId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "房间号不能为空且不可重复");
        }
    }

    private boolean existsRoomNo(String roomNo, Long excludeId) {
        QueryWrapper<Room> query = new QueryWrapper<Room>().eq("room_no", roomNo);
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        return roomMapper.selectCount(query) > 0;
    }

    /** UC-09 约束：维护后房型房间总数不得小于当前有效订单数（已确认 + 已入住） */
    private void ensureRoomCountNotBelowActiveOrders(Long typeId, int newRoomCount) {
        if (newRoomCount < roomMapper.countActiveOrders(typeId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该房型存在有效订单，请先处理相关订单");
        }
    }
}
