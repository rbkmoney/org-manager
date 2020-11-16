package com.rbkmoney.orgmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JsonMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows(JsonProcessingException.class)
    public String toJson(Object data) {
        return objectMapper.writeValueAsString(data);
    }

    @SuppressWarnings("rawtypes")
    @SneakyThrows(JsonProcessingException.class)
    public Map toMap(String json) {
        return objectMapper.readValue(json, Map.class);
    }
}
