package com.qaq.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaq.base.enums.GatewayHeaderEnum;
import com.qaq.base.model.uniauth.AuthResult;
import com.qaq.base.model.uniauth.AuthResultResp;
import com.qaq.gateway.enums.GatewayErrorEnum;
import com.qaq.gateway.model.repository.RouteConfigRepository;
import com.qaq.gateway.utils.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class UniAuthFilter implements GlobalFilter, Ordered {

    @Resource
    private RouteConfigRepository routeConfigRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String id = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ROUTE_ID_ATTR);
        var permissionStr = "";
        try {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            assert route != null;
            var metadata = route.getMetadata();

            var appName = (String) metadata.get("app");
            var userId = getUserId(exchange);
            var userIdType = getUserIdType(exchange);
            var uniauthUri = getUniauthUri();
            var url = String.format("%s/permissions/%s/user?user_id=%s&user_id_type=%s", uniauthUri, appName, userId, userIdType);

            var authResult = retrieveAuthResult(url);
            permissionStr = Base64.getUrlEncoder().encodeToString(objectMapper.writeValueAsString(authResult).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Fail to get user permissions", e);
            return Response.sendError(exchange.getResponse(), GatewayErrorEnum.NO_PERMISSION);
        }
        return chain.filter(
                exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header(GatewayHeaderEnum.X_Gateway_Permission.getHeaderName(), permissionStr)
                                .build())
                        .build());
    }

    private String getUserId(ServerWebExchange exchange) {
        var userIds = exchange.getRequest().getHeaders().get(GatewayHeaderEnum.X_Gateway_UserId.getHeaderName());
        var userId = "";
        if (userIds == null || userIds.size() != 1) {
            log.error("getUserId error");
        } else {
            userId = userIds.get(0);
        }
        return userId;
    }

    private String getUserIdType(ServerWebExchange exchange) {
        var userIdTypes = exchange.getRequest().getHeaders().get(GatewayHeaderEnum.X_Gateway_UserIdType.getHeaderName());
        var userIdType = "";
        if (userIdTypes == null || userIdTypes.size() != 1) {
            log.error("getUserIdType error");
        } else {
            userIdType = userIdTypes.get(0);
        }
        return userIdType;
    }

    private String getUniauthUri() {
        var config = routeConfigRepository.findByRouteId("uniauth").orElseThrow();
        return config.getUri();
    }

    private AuthResult retrieveAuthResult(String url) {
        var resp = restTemplate.getForObject(url, AuthResultResp.class);
        assert resp != null;
        if (resp.getCode() != 0) {
            log.error("Fail to get user permissions: {}", resp);
            throw new RuntimeException("Fail to get user permissions");
        }
        return resp.getData();
    }

}
