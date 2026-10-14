package com.blooddonor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BloodDonorFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodDonorFinderApplication.class, args);
    }
}
