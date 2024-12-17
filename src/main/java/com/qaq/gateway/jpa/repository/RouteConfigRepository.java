package com.qaq.gateway.jpa.repository;

import com.qaq.gateway.jpa.entity.RouteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteConfigRepository extends JpaRepository<RouteConfig, Integer> {
    List<RouteConfig> findByActive(Boolean active);

    List<RouteConfig> findByAppOrderByLastModifyTimeDesc(String app);
}
