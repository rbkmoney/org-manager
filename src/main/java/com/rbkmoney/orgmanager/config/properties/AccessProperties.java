package com.rbkmoney.orgmanager.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "access-check")
@Data
public class AccessProperties {

    private Boolean enabled;

}
