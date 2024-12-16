package com.qaq.gateway.component;

import com.qaq.gateway.jpa.repository.RouteConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

@RequiredArgsConstructor
@Repository
public class RouterConfigRepository implements RouteDefinitionRepository {

    private final RouteConfigRepository routeConfigRepository;

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return null;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = this.getRouteConfigs();
        return Flux.fromIterable(routeDefinitions);
    }

    public List<RouteDefinition> getRouteConfigs() {
        var rules = routeConfigRepository.findByActive(true);
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        for (var rule : rules) {
            RouteDefinition routeDefinition = new RouteDefinition();
            routeDefinition.setId(rule.getId().toString());
            routeDefinition.setUri(URI.create(rule.getUri()));
            routeDefinition.setPredicates(this.getPredicates(rule.getPredicates()));
            routeDefinition.setFilters(this.getFilters(rule.getFilters()));

            var meta = this.getMetadata(rule.getMetadata());
            meta.put("app", rule.getApp());
            meta.put("uniauth_active", rule.getUniauthActive());
            routeDefinition.setMetadata(meta);
            var ruleOrder = rule.getRouterOrder();
            if (null != ruleOrder) {
                routeDefinition.setOrder(ruleOrder);
            }
            routeDefinitions.add(routeDefinition);
        }
        return routeDefinitions;
    }


    private List<PredicateDefinition> getPredicates(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        Yaml yaml = new Yaml();
        List<String> predicateList = yaml.load(text);
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();
        for (String predicate : predicateList) {
            if (predicate.isBlank()) {
                continue;
            }
            PredicateDefinition definition = new PredicateDefinition(predicate);
            predicateDefinitions.add(definition);
        }
        return predicateDefinitions;
    }

    private List<FilterDefinition> getFilters(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        Yaml yaml = new Yaml();
        List<String> filterList = yaml.load(text);
        List<FilterDefinition> filterDefinitions = new ArrayList<>();
        for (String filter : filterList) {
            if (filter.isBlank()) {
                continue;
            }
            FilterDefinition definition = new FilterDefinition(filter);
            filterDefinitions.add(definition);
        }
        return filterDefinitions;
    }

    private Map<String, Object> getMetadata(String text) {
        if (text.isBlank()) {
            return Collections.emptyMap();
        }
        Yaml yaml = new Yaml();
        return yaml.load(text);
    }

}