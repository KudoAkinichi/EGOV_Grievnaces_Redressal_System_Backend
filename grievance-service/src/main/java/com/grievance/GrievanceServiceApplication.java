package com.grievance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.grievance", "com.grievance.common"})
public class GrievanceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrievanceServiceApplication.class, args);
    }
}