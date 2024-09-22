package com.qaq.gateway.model.repository;

import com.qaq.gateway.model.entity.RouteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteConfigRepository extends JpaRepository<RouteConfig, Long> {
    Optional<RouteConfig> findByRouteId(String routeId);
}
