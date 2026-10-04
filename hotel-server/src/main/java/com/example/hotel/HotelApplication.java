package com.example.hotel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.example.hotel.**.mapper")
public class HotelApplication {

    public static void main(String[] args) {
        // 与 DB 连接时区（Asia/Shanghai）对齐：入住窗口、订单号日期、库存预创建窗口都按此口径取"今天"
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(HotelApplication.class, args);
    }
}
