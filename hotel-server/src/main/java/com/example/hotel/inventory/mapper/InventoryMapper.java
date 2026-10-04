package com.example.hotel.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hotel.inventory.entity.DailyInventory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

public interface InventoryMapper extends BaseMapper<DailyInventory> {

    @Update("UPDATE daily_inventory SET occupied_count = occupied_count + 1 "
            + "WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count < total_count")
    int occupyOne(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate);

    @Update("UPDATE daily_inventory SET occupied_count = occupied_count - 1 "
            + "WHERE room_type_id = #{typeId} AND stay_date = #{stayDate} AND occupied_count > 0")
    int releaseOne(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate);

    @Select("SELECT COUNT(*) FROM room WHERE room_type_id = #{typeId}")
    int countRooms(@Param("typeId") Long typeId);

    @Insert("INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) "
            + "VALUES (#{typeId}, #{stayDate}, #{totalRooms}, 0)")
    int insertIgnore(@Param("typeId") Long typeId, @Param("stayDate") LocalDate stayDate,
                     @Param("totalRooms") int totalRooms);

    @Select("SELECT id FROM room_type")
    java.util.List<Long> selectRoomTypeIds();

    /** 批量创建某房型 [startDate, endDate) 的库存行（已存在则忽略），total_count 取当前房间数 */
    @Insert("INSERT IGNORE INTO daily_inventory (room_type_id, stay_date, total_count, occupied_count) "
            + "WITH RECURSIVE dates(d) AS ("
            + "  SELECT #{startDate} "
            + "  UNION ALL SELECT d + INTERVAL 1 DAY FROM dates WHERE d + INTERVAL 1 DAY < #{endDate}"
            + ") "
            + "SELECT #{typeId}, d, #{totalRooms}, 0 FROM dates")
    int preCreate(@Param("typeId") Long typeId, @Param("startDate") LocalDate startDate,
                  @Param("endDate") LocalDate endDate, @Param("totalRooms") int totalRooms);

    /** UC-09 房间数变更同步：把 stay_date ≥ fromDate 的库存行 total_count 刷成新房间数（历史日期行不改） */
    @Update("UPDATE daily_inventory SET total_count = #{totalRooms} "
            + "WHERE room_type_id = #{typeId} AND stay_date >= #{fromDate}")
    int syncFutureTotal(@Param("typeId") Long typeId, @Param("fromDate") LocalDate fromDate,
                        @Param("totalRooms") int totalRooms);
}
