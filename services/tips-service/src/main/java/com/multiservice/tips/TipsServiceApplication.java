package com.multiservice.tips;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.multiservice.tips", "com.multiservice.common"})
public class TipsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TipsServiceApplication.class, args);
    }
}
