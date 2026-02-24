package com.moshimo.backend;

import com.moshimo.backend.infrastructure.config.DotenvPropertyLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class BackendApplication {

    public static void main(String[] args) {
        DotenvPropertyLoader.load();
        SpringApplication.run(BackendApplication.class, args);
    }

}