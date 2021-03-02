package com.rbkmoney.orgmanager.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bouncer")
@Data
public class BouncerProperties {

    private Boolean enabled;
    private String contextFragmentId;
    private String deploymentId;
    private String authMethod;
    private String realm;

}
