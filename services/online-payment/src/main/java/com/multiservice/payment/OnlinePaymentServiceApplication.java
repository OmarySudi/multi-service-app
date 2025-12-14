package com.multiservice.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.multiservice.payment", "com.multiservice.common"})
public class OnlinePaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlinePaymentServiceApplication.class, args);
    }
}
