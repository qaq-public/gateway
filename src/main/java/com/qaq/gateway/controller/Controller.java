package com.qaq.gateway.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaq.base.exception.UnAuthorizedException;
import com.qaq.base.model.uniauth.AuthResult;
import com.qaq.base.response.ApiResponse;
import com.qaq.gateway.enums.PermissionConstant;
import com.qaq.gateway.jpa.entity.RouteConfig;
import com.qaq.gateway.jpa.repository.RouteConfigRepository;
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
    public Mono<ApiResponse<List<RouteConfig>>> list(
            @RequestParam(required = false) String app,
            @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, new ArrayList<>(), "No permission"));
        }
        List<RouteConfig> apps;
        if (app == null) {
            apps = routeConfigRepository.findAll(Sort.by(Sort.Order.asc("app"), Sort.Order.desc("lastModifyTime")));
        } else {
            apps = routeConfigRepository.findByAppOrderByLastModifyTimeDesc(app);
        }
        return Mono.just(new ApiResponse<>(apps));

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
    public Mono<ApiResponse<RouteConfig>> retrieve(@PathVariable Integer id, @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, null, "No permission"));
        }
        var routeConfig = routeConfigRepository.findById(id).orElseThrow();
        return Mono.just(new ApiResponse<>(routeConfig));
    }

    @DeleteMapping("/routes/{id}")
    public Mono<ApiResponse<Integer>> destroy(@PathVariable Integer id, @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, null, "No permission"));
        }
        routeConfigRepository.deleteById(id);
        scheduler.refreshConfig();
        return Mono.just(new ApiResponse<>(id));
    }

    @PutMapping("/routes/{id}")
    public Mono<ApiResponse<RouteConfig>> update(@PathVariable Integer id,  @RequestBody RouteConfig routeConfig, @RequestHeader("X-Gateway-Permission") String permissionStr) {
        var authResult = parseAuthResult(permissionStr);
        if (!authResult.getPermissions().contains(PermissionConstant.PLATFORM_ADMIN)) {
            return Mono.just(new ApiResponse<>(-401, null, "No permission"));
        }
        var routeConfigInDb = routeConfigRepository.findById(id).orElseThrow();
        routeConfigInDb.setUri(routeConfig.getUri());
        routeConfigInDb.setPredicates(routeConfig.getPredicates());
        routeConfigInDb.setFilters(routeConfig.getFilters());
        routeConfigInDb.setMetadata(routeConfig.getMetadata());
        routeConfigInDb.setRouterOrder(routeConfig.getRouterOrder());
        routeConfigInDb.setUniauthActive(routeConfig.getUniauthActive());
        routeConfigInDb.setApp(routeConfig.getApp());
        routeConfigInDb.setActive(routeConfig.getActive());
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
