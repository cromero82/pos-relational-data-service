package com.infinitesoft.pos_relational_data_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class PosRelationalDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosRelationalDataServiceApplication.class, args);
    }

}
