package com.qaq.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaq.base.model.uniauth.AuthResult;
import com.qaq.base.model.uniauth.AuthResultResp;
import com.qaq.gateway.jpa.repository.RouteConfigRepository;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.qaq.base.component.JWTVerifierComponent;
import com.qaq.base.enums.GatewayHeaderEnum;
import com.qaq.gateway.enums.GatewayErrorEnum;
import com.qaq.gateway.utils.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JWTVerifierComponent jwtVerifierComponent;
    private final RouteConfigRepository routeConfigRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();

        var jwt = this.getJwt(request);
        if (jwt.isBlank()) {
            return Response.sendError(response, GatewayErrorEnum.MISSING_AUTH);
        }

        try {
            var openid = this.getOpenid(jwt);
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            assert route != null;
            var metadata = route.getMetadata();
            log.debug("metadata: {}", metadata.toString());
            var uniauthActive = (Boolean) metadata.get("uniauth_active");
            if (Boolean.FALSE.equals(uniauthActive)) {
                return chain.filter(
                        exchange.mutate()
                                .request(request.mutate()
                                        .header(GatewayHeaderEnum.X_Gateway_Token.getHeaderName(), jwt.split("\\.")[1])
                                        .build())
                                .build());
            }
            var appName = (String) metadata.get("app");
            var authResult = retrieveAuthResult(appName, openid);
            return chain.filter(
                    exchange.mutate()
                            .request(request.mutate()
                                    .header(GatewayHeaderEnum.X_Gateway_Token.getHeaderName(), jwt.split("\\.")[1])
                                    .header(GatewayHeaderEnum.X_Gateway_Permission.getHeaderName(), Base64.getUrlEncoder().encodeToString(objectMapper.writeValueAsString(authResult).getBytes(StandardCharsets.UTF_8)))
                                    .build())
                            .build());
        }
        catch (TokenExpiredException e) {
            return Response.sendError(response, GatewayErrorEnum.EXPIRED_TOKEN);
        } catch (JWTVerificationException e) {
            log.error("Invalid jwt token error: ", e);
            return Response.sendError(response, GatewayErrorEnum.INVALID_TOKEN);
        }
        catch (Exception e) {
            log.error("Fail to get user permissions", e);
            return Response.sendError(exchange.getResponse(), GatewayErrorEnum.NO_PERMISSION);
        }

    }

    private String getJwt(ServerHttpRequest request) {
        var cookiesList = request.getCookies().get("jwt");
        String jwt;

        if (cookiesList != null && cookiesList.size() > 1) {
            return "";
        } else if (cookiesList != null && cookiesList.size() == 1) {
            jwt = cookiesList.getFirst().getValue();
            log.debug("Jwt token is in cooke: {}", jwt);
        } else {
            var header = request.getHeaders().get("Authorization");
            if (header == null || header.size() != 1) {
                return "";
            }
            jwt = header.getFirst().replace("Bearer ", "");
            log.debug("Jwt token is in header: {}", jwt);
        }
        return jwt;
    }

    private String getOpenid(String jwt) throws JWTVerificationException {
        var claims = jwtVerifierComponent.validToken(jwt);
        var openid = claims.get("openid").asString();
        log.debug("Jwt token is valid, user email: {}", openid);
        return openid;
    }

    private String getUniauthUri() {
        var config = routeConfigRepository.findById(1).orElseThrow();
        return config.getUri();
    }

    private AuthResult retrieveAuthResult(String appName, String openid) {
        var url = String.format("%s/permissions/%s/user?user_id=%s&user_id_type=open_id", getUniauthUri(), appName, openid);
        var resp = restTemplate.getForObject(url, AuthResultResp.class);
        assert resp != null;
        if (resp.getCode() != 0) {
            log.error("Fail to get user permissions: {}", resp);
            throw new RuntimeException("Fail to get user permissions");
        }
        return resp.getData();
    }

}