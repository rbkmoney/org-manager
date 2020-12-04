package com.rbkmoney.orgmanager.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Data
public class KeyCloakProperties {

    private String realm;

    private String resource;

    private String realmPublicKey;

    private String realmPublicKeyFilePath;

    private String authServerUrl;

    private String sslRequired;

    private int notBefore;

}
