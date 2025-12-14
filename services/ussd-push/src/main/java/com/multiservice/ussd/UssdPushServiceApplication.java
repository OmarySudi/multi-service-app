package com.multiservice.ussd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.multiservice.ussd", "com.multiservice.common"})
public class UssdPushServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UssdPushServiceApplication.class, args);
    }
}
