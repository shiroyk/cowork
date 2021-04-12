package com.shiroyk.cowork.coworkeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class CoworkEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoworkEurekaApplication.class, args);
    }

}
