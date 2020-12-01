package com.rbkmoney.orgmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@ConfigurationPropertiesScan
@SpringBootApplication
public class OrgManagerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrgManagerApplication.class, args);
    }

}
