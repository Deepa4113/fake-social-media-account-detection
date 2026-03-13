package com.fakedetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FakeDetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FakeDetectionApplication.class, args);
    }
}
