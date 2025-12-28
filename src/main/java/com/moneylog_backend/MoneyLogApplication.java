package com.moneylog_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoneyLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoneyLogApplication.class, args);
    } // func end
} // class end