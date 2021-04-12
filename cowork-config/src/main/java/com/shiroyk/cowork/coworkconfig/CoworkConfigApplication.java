package com.shiroyk.cowork.coworkconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@EnableDiscoveryClient
@SpringBootApplication
public class CoworkConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoworkConfigApplication.class, args);
    }

}
