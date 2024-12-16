package com.qaq.gateway.utils;

import java.nio.charset.StandardCharsets;

import com.qaq.gateway.enums.GatewayErrorEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaq.base.response.ApiResponse;

import reactor.core.publisher.Mono;

@Slf4j
public class Response {

    private Response() { }

    public static Mono<Void> sendError(ServerHttpResponse response, GatewayErrorEnum errorEnum) {
        var objectMapper = new ObjectMapper();
        var apiResponse = new ApiResponse<>();
        apiResponse.setMessage(errorEnum.getMessage());
        apiResponse.setCode(errorEnum.getErrcode());
        response.getHeaders().add("content-type", "application/json");
        response.setStatusCode(HttpStatus.valueOf(errorEnum.getHttpStatus()));
        byte[] bits = null;
        try {
            bits = objectMapper.writeValueAsString(apiResponse).getBytes(StandardCharsets.UTF_8);

        } catch (JsonProcessingException ex) {
            log.error("", ex);
        }
        assert bits != null;
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        return response.writeWith(Mono.just(buffer));
    }
}
