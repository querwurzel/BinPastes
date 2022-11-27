package it.wylke.binpastes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableR2dbcRepositories
@EnableR2dbcAuditing
@EnableScheduling
public class BinPastesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinPastesApplication.class, args);
    }

}
