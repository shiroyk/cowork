package com.shiroyk.cowork.coworkgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticateFilter implements GlobalFilter, Ordered {

    /**
     * @Description: 从Jwt中获取用户Id放入请求Header中，以便后续微服务使用
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info(exchange.getRequest().getPath().toString());
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Authentication auth = securityContext.getAuthentication();
                    if (auth == null || auth instanceof AnonymousAuthenticationToken ||
                            !auth.isAuthenticated()) {
                        return chain.filter(exchange);
                    }

                    Jwt jwt = (Jwt) auth.getPrincipal();
                    String userId = jwt.getClaims().get("id").toString();
                    log.debug("AuthenticateFilter User Id - " + userId);

                    return chain.filter(exchange.mutate()
                            .request(exchange.getRequest()
                                    .mutate()
                                    .headers(headers -> headers
                                            .set("X-User-Id", userId))
                                    .build())
                            .build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}