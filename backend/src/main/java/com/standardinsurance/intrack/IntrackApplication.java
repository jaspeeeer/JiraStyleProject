package com.standardinsurance.intrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IntrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntrackApplication.class, args);
    }
}
