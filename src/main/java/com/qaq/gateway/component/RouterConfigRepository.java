package com.qaq.gateway.component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.Yaml;

import com.qaq.gateway.model.entity.RouteConfig;
import com.qaq.gateway.model.repository.RouteConfigRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        var rules = routeConfigRepository.findAll();
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        for (RouteConfig rule : rules) {
            RouteDefinition routeDefinition = new RouteDefinition();
            routeDefinition.setId(rule.getRouteId());
            routeDefinition.setUri(URI.create(rule.getUri()));
            routeDefinition.setPredicates(this.getPredicates(rule.getPredicates()));
            routeDefinition.setFilters(this.getFilters(rule.getFilters()));
            routeDefinition.setMetadata(this.getMetadata(rule.getMetadata()));
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