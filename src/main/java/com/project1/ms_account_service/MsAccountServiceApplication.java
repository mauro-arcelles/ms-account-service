package com.project1.ms_account_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class MsAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAccountServiceApplication.class, args);
    }

}
