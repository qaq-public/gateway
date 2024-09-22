package com.qaq.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.qaq.base.config.JWTVerifierConfig;

@Configuration
@Import(JWTVerifierConfig.class)
public class BeanConfig {
    
}
