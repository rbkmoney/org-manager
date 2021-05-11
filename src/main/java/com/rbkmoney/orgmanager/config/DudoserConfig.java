package com.rbkmoney.orgmanager.config;

import com.rbkmoney.damsel.message_sender.MessageSenderSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class DudoserConfig {

    @Bean
    public MessageSenderSrv.Iface dudoserSrv(@Value("${dudoser.url}") Resource resource,
                                             @Value("${dudoser.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(MessageSenderSrv.Iface.class);
    }
}
