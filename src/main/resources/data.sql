-- ----------------------------
-- Records of route_config
-- ----------------------------
INSERT IGNORE INTO `gateway_route_config` (`id`, `active`, `filters`, `last_modify_time`, `metadata`, `predicates`, `route_id`, `router_order`, `uri`, `app`, `uniauth_active`) VALUES (1, b'1', '- StripPrefix=2', NOW(), 'app: uniauth', '- Path=/api/uniauth/**', 'uniauth', NULL, 'http://uniauth-api', 'uniauth', b'1');
INSERT IGNORE INTO `gateway_route_config` (`id`, `active`, `filters`, `last_modify_time`, `metadata`, `predicates`, `route_id`, `router_order`, `uri`, `app`, `uniauth_active`) VALUES (2, b'1', '- StripPrefix=2', NOW(), 'app: uniauth', '- Path=/api/gateway/**', 'gateway', NULL, 'http://gateway-api', 'uniauth', b'1');
