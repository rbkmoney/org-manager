package com.rbkmoney.orgmanager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

@TestConfiguration
public class KeycloakTestConfig {

    @Bean
    public KeycloakOpenIdStub keycloakOpenIdStub(@Value("${keycloak.auth-server-url}") String keycloakAuthServerUrl,
                                                 @Value("${keycloak.realm}") String keycloakRealm,
                                                 JwtTokenBuilder jwtTokenBuilder) {
        return new KeycloakOpenIdStub(keycloakAuthServerUrl, keycloakRealm, jwtTokenBuilder);
    }

    @Bean
    public JwtTokenBuilder JwtTokenBuilder(KeyPair keyPair) {
        return new JwtTokenBuilder(keyPair.getPrivate());
    }

    @Bean
    public KeyPair keyPair() throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(KeyPair keyPair) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = fact.getKeySpec(keyPair.getPublic(), X509EncodedKeySpec.class);
        String publicKey = Base64.getEncoder().encodeToString(spec.getEncoded());
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.load(new ClassPathResource("application.yml").getInputStream());
        properties.setProperty("keycloak.realm-public-key", publicKey);
        pspc.setProperties(properties);
        pspc.setLocalOverride(true);
        return pspc;
    }

}
