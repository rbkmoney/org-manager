package com.rbkmoney.orgmanager.config;

import com.rbkmoney.bouncer.decisions.ArbiterSrv;
import com.rbkmoney.woody.thrift.impl.http.THClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class BouncerConfig {

    @Bean
    public ArbiterSrv.Iface bouncerClient(@Value("${bouncer.url}") Resource resource,
                                          @Value("${bouncer.networkTimeout}") int networkTimeout) throws IOException {
        return new THClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI())
                .build(ArbiterSrv.Iface.class);
    }

}
