package com.example.miniats;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.miniats")
@SpringBootApplication
public class MiniAtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniAtsApplication.class, args);
    }
}
