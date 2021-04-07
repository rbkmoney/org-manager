package com.rbkmoney.orgmanager.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.orgmanager.controller.converter.InvitationStatusConverter;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JsonNullableModule());
        return mapper;
    }

    @Autowired
    public void fillWebConverter(FormattingConversionService formatService) {
        formatService.addConverter(new InvitationStatusConverter());
    }

}
