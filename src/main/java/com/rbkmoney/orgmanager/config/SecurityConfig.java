package com.rbkmoney.orgmanager.config;

import com.google.common.base.Strings;
import com.rbkmoney.orgmanager.config.properties.KeyCloakProperties;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@ComponentScan(
        basePackageClasses = KeycloakSecurityComponents.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "org.keycloak.adapters.springsecurity.management.HttpSessionManager"
        )
)
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@ConditionalOnProperty(value = "auth.enabled", havingValue = "true")
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    private KeyCloakProperties keyCloakProperties;

    @Override
    protected HttpSessionManager httpSessionManager() {
        return super.httpSessionManager();
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.GET, "/**/health").permitAll()
                .anyRequest().authenticated();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return facade -> {
            KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(adapterConfig());
            deployment.setNotBefore(keyCloakProperties.getNotBefore());
            return deployment;
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.addAllowedMethod(HttpMethod.PUT);
        configuration.addAllowedMethod(HttpMethod.DELETE);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private AdapterConfig adapterConfig() {
        String keycloakRealmPublicKey;
        if (!Strings.isNullOrEmpty(keyCloakProperties.getRealmPublicKeyFilePath())) {
            keycloakRealmPublicKey = readKeyFromFile(keyCloakProperties.getRealmPublicKeyFilePath());
        } else {
            keycloakRealmPublicKey = keyCloakProperties.getRealmPublicKey();
        }

        AdapterConfig adapterConfig = new AdapterConfig();
        adapterConfig.setRealm(keyCloakProperties.getRealm());
        adapterConfig.setRealmKey(keycloakRealmPublicKey);
        adapterConfig.setResource(keyCloakProperties.getResource());
        adapterConfig.setAuthServerUrl(keyCloakProperties.getAuthServerUrl());
        adapterConfig.setUseResourceRoleMappings(true);
        adapterConfig.setBearerOnly(true);
        adapterConfig.setSslRequired(keyCloakProperties.getSslRequired());
        return adapterConfig;
    }

    private String readKeyFromFile(String filePath) {
        try {
            List<String> strings = Files.readAllLines(Paths.get(filePath));
            strings.remove(strings.size() - 1);
            strings.remove(0);

            return strings.stream().map(String::trim).collect(Collectors.joining());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
