package org.ddd.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author <template/>
 * @date
 */
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableJpaRepositories(basePackages = "org.ddd.example.adapter.domain.repositories")
@EntityScan(basePackages = "org.ddd.example.domain.aggregates")
public class StartApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }

}
