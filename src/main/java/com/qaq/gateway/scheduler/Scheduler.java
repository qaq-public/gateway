package com.qaq.gateway.scheduler;

import java.util.List;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qaq.gateway.component.RouterConfigRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class Scheduler {

    private final GatewayProperties gatewayProperties;
    private final ApplicationEventPublisher publisher;
    private final RouterConfigRepository routerConfigRepository;

    private void refreshRouteConfig() {
        List<RouteDefinition> routeDefinitions = routerConfigRepository.getRouteConfigs();
        gatewayProperties.setRoutes(routeDefinitions);
        publisher.publishEvent(new RefreshRoutesEvent(this));
        log.debug("完成刷新网关路由配置 总数 {}", routeDefinitions.size());
    }

    //这里是更新路由的策略，大家根据自己的情况来就好，定时刷新
    @Scheduled(fixedRate = 60 * 1000)
    @PostConstruct
    public void refreshConfig() {
        this.refreshRouteConfig();
    }
}
