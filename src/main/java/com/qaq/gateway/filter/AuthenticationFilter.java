package com.qaq.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JWTVerifierComponent jwtVerifierComponent;

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();
        var cookiesList = request.getCookies().get("jwt");
        String jwt;

        if (cookiesList != null && cookiesList.size() > 1) {
            return Response.sendError(response, GatewayErrorEnum.MULTIPLE_TOKEN);
        } else if (cookiesList != null && cookiesList.size() == 1) {
            jwt = cookiesList.get(0).getValue();
            log.debug("Jwt token is in cooke: {}", jwt);
        } else {
            var header = request.getHeaders().get("Authorization");
            if (header == null || header.size() != 1) {
                return Response.sendError(response, GatewayErrorEnum.MISSING_AUTH);
            }
            jwt = header.get(0).replace("Bearer ", "");
            log.debug("Jwt token is in header: {}", jwt);
        }
        String openid;
        try {
            var claims = jwtVerifierComponent.validToken(jwt);
            openid = claims.get("openid").asString();
        } catch (TokenExpiredException e) {
            return Response.sendError(response, GatewayErrorEnum.EXPIRED_TOKEN);
        } catch (JWTVerificationException e) {
            log.debug("Invalid jwt token error: ", e);
            return Response.sendError(response, GatewayErrorEnum.INVALID_TOKEN);
        }
        if ("".equals(openid)) {
            return Response.sendError(response, GatewayErrorEnum.MISSING_EMAIL);
        }
        log.debug("Jwt token is valid, user email: {}", openid);
        String authInfo = jwt.split("\\.")[1];
        return chain.filter(
                exchange.mutate()
                        .request(request.mutate()
                                .header(GatewayHeaderEnum.X_Gateway_UserId.getHeaderName(), openid)
                                .header(GatewayHeaderEnum.X_Gateway_UserIdType.getHeaderName(), "open_id")
                                .header(GatewayHeaderEnum.X_Gateway_Token.getHeaderName(), authInfo)
                                .build())
                        .build());
    }

}