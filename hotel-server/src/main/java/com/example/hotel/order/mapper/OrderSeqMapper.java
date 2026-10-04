package com.example.hotel.order.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

public interface OrderSeqMapper {

    @Update("UPDATE order_seq SET current_value = current_value + 1 WHERE seq_date = #{seqDate}")
    int incr(@Param("seqDate") LocalDate seqDate);

    @Insert("INSERT IGNORE INTO order_seq (seq_date, current_value) VALUES (#{seqDate}, 0)")
    int insertIgnore(@Param("seqDate") LocalDate seqDate);

    @Select("SELECT current_value FROM order_seq WHERE seq_date = #{seqDate}")
    Long selectValue(@Param("seqDate") LocalDate seqDate);
}
