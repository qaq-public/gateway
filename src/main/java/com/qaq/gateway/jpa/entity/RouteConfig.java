package com.qaq.gateway.jpa.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonProperty("uniauth_active")
    private Boolean uniauthActive = true;

    @Column(name = "router_order", unique = true)
    private Integer routerOrder;

    private Boolean active = true;

    @JsonProperty("last_modify_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
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