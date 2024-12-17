# spring cloud gateway

## filters

### LoggingFilter
记录请求日志

### AuthenticationFilter
1. 从Header(Authorization)或Cookie(jwt)中获取jwt token(由uniauth生成), 并验证有效性
2. 可选(通过[uniatuth](https://qaq-dev.openqaq.fun/uniauth/admin/gateways)配置), 使用 user openid和路由的app字段去 uniauth请求权限信息
3. 如果验证通过, 将用户信息和权限信息存入request的header中, 供子应用使用
   1. 设置`X-Gateway-Permission`header
   2. 设置`X-Gateway-Permission`header