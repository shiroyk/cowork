package com.shiroyk.cowork.coworkgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CoworkGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoworkGatewayApplication.class, args);
    }

}
