package com.licensing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@ConfigurationPropertiesScan
public class LicenseManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LicenseManagementApiApplication.class, args);
    }

}
