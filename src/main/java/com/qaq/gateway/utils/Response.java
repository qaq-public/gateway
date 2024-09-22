package com.qaq.gateway.utils;

import java.nio.charset.StandardCharsets;

import com.qaq.gateway.enums.GatewayErrorEnum;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaq.base.response.ApiResponse;

import reactor.core.publisher.Mono;

public class Response {
    public static Mono<Void> sendError(ServerHttpResponse response, GatewayErrorEnum errorEnum) {
        var objectMapper = new ObjectMapper();
        var apiRsponse = new ApiResponse<Object>();
        apiRsponse.setMessage(errorEnum.getMessage());
        apiRsponse.setCode(errorEnum.getErrcode());
        response.getHeaders().add("content-type", "application/json");
        response.setStatusCode(HttpStatus.valueOf(errorEnum.getHttpStatus()));
        byte[] bits = null;
        try {
            bits = objectMapper.writeValueAsString(apiRsponse).getBytes(StandardCharsets.UTF_8);

        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        assert bits != null;
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        return response.writeWith(Mono.just(buffer));
    }
}
