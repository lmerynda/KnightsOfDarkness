package com.knightsofdarkness.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages =
{ "com.knightsofdarkness.web*" })
@EntityScan(basePackages =
{ "com.knightsofdarkness.web*" })
@EnableJpaRepositories(basePackages =
{ "com.knightsofdarkness.web*" })
@EnableScheduling
public class GameApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(GameApplication.class, args);
    }
}
