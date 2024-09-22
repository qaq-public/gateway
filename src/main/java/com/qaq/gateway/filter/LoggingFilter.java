package com.qaq.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        // Log the HTTP method and path
        String method = request.getMethod().toString();
        String path = request.getURI().getRawPath();
        // logger.info("Request method: {}, Request path: {}", method, path);

        // Log query params if you need to (Be careful with sensitive data!)
        String queryParams = request.getURI().getQuery();
        // logger.info("Request query params: {}", queryParams);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long endTime = System.currentTimeMillis();
            Long duration = endTime - startTime;
            ServerHttpResponse response = exchange.getResponse();

            // Log the response status code
            if (queryParams != null) {
                log.info("{} {} {}?{} {}", response.getStatusCode(), method, path, queryParams, duration);
            } else {
                log.info("{} {} {} {}", response.getStatusCode(), method, path, duration);
            }

        }));
    }
}
