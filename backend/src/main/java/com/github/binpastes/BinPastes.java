package com.github.binpastes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BinPastes {

    public static void main(String[] args) {
        SpringApplication.run(BinPastes.class, args);
    }

}
