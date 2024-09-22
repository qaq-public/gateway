package com.qaq.gateway.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaq.base.exception.UnAuthorizedException;
import com.qaq.base.model.uniauth.AuthResult;
import com.qaq.base.response.ApiResponse;
import com.qaq.gateway.enums.PermissionConstant;
import com.qaq.gateway.model.entity.RouteConfig;
import com.qaq.gateway.model.repository.RouteConfigRepository;
import com.qaq.gateway.scheduler.Scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class Controller {

    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final RouteConfigRepository routeConfigRepository;
    private final Scheduler scheduler;

    @GetMapping("/routes")
    public Mono<ApiResponse<List<RouteConfig>>> list(@RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, new ArrayList<>(), "No permission"));
        }
        return Mono.just(new ApiResponse<>(routeConfigRepository.findAll()));
    }

    @PostMapping("/routes")
    public Mono<ApiResponse<RouteConfig>> create(@RequestBody RouteConfig routeConfig, @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, null, "No permission"));
        }
        routeConfig.setId(null);
        routeConfig = routeConfigRepository.save(routeConfig);
        scheduler.refreshConfig();
        return Mono.just(new ApiResponse<>(routeConfig));
    }

    @GetMapping("/routes/{id}")
    public Mono<ApiResponse<RouteConfig>> retrieve(@PathVariable Long id, @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, null, "No permission"));
        }
        var routeConfigOptional = routeConfigRepository.findById(id);
        return Mono.just(new ApiResponse<>(routeConfigOptional.get()));
    }

    @DeleteMapping("/routes/{id}")
    public Mono<ApiResponse<Long>> destroy(@PathVariable Long id, @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, null, "No permission"));
        }
        routeConfigRepository.deleteById(id);
        scheduler.refreshConfig();
        return Mono.just(new ApiResponse<>(id));
    }

    @PutMapping("/routes/{id}")
    public Mono<ApiResponse<RouteConfig>> update(@PathVariable Long id,  @RequestBody RouteConfig routeConfig, @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, null, "No permission"));
        }
        var routeConfigInDb = routeConfigRepository.findById(id).orElseThrow();
        routeConfigInDb.setLastModifyTime(new Date());
        routeConfigInDb.setActive(routeConfig.getActive());
        routeConfigInDb.setRouteId(routeConfig.getRouteId());
        routeConfigInDb.setUri(routeConfig.getUri());
        routeConfigInDb.setPredicates(routeConfig.getPredicates());
        routeConfigInDb.setFilters(routeConfig.getFilters());
        routeConfigInDb.setMetadata(routeConfig.getMetadata());
        routeConfigInDb.setRouterOrder(routeConfig.getRouterOrder());
        routeConfigInDb = routeConfigRepository.save(routeConfigInDb);
        scheduler.refreshConfig();
        return Mono.just(new ApiResponse<>(routeConfigInDb));
    }

    @GetMapping("/routes/refresh")
    public Mono<ApiResponse<String>> refresh(@RequestHeader("X-Gateway-Permission") String permissionStr) {
        scheduler.refreshConfig();
        return Mono.just(new ApiResponse<>("refreshed"));
    }

    private AuthResult parseAuthResult(String permissionStr) {
        try {
            String decodedPermission = new String(BASE64_URL_DECODER.decode(permissionStr));
            return OBJECT_MAPPER.readValue(decodedPermission, AuthResult.class);
        } catch (Exception e) {
            log.error("Failed to decode and parse permission from header. permissionStr: {} ", permissionStr, e);
            throw new UnAuthorizedException("Failed to decode the auth permission from gateway header");
        }
    }
}
