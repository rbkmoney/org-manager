package com.rbkmoney.orgmanager.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class TomcatWebServerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${server.rest.port}")
    private int restPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector connector = new Connector();
        connector.setPort(restPort);

        factory.addAdditionalTomcatConnectors(connector);
    }
}
