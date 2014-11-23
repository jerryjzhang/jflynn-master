package com.tencent.jflynn.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages={
		"com.tencent.jflynn.controller", 
		"com.tencent.jflynn.service",
		"com.tencent.jflynn.dao"})
@Configuration
@EnableAutoConfiguration
public class JFlynnMain {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(JFlynnMain.class, args);
    }
}
