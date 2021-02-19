package com.rbkmoney.orgmanager.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "invite-token")
@Data
public class InviteTokenProperties {
    private Long lifeTimeInDays;
}
