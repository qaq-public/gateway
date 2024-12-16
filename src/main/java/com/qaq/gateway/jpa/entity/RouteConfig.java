package com.qaq.gateway.jpa.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
@Entity
@Table(name = "gateway_route_config")
public class RouteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String uri;

    // yaml
    @Column(unique = true, nullable = false)
    private String predicates = "";

    // yaml
    private String filters = "";

    // yaml
    private String metadata = "";

    private String app;

    private Boolean uniauthActive = true;

    @Column(name = "router_order", unique = true)
    private Integer routerOrder;

    private Boolean active = true;

    @JsonProperty("last_modify_time")
    private Date lastModifyTime = new Date();

    // 在插入记录之前设置 lastModifyTime
    @PrePersist
    protected void onCreate() {
        lastModifyTime = new Date();
    }

    // 在更新记录之前设置 lastModifyTime
    @PreUpdate
    protected void onUpdate() {
        lastModifyTime = new Date();
    }
}