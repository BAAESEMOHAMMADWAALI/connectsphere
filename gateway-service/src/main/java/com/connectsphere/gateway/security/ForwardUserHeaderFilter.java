package com.connectsphere.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ForwardUserHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .flatMap(authentication -> {
                    ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .headers(headers -> {
                                headers.set("X-User-Id", authentication.getToken().getSubject());
                                String email = authentication.getToken().getClaimAsString("email");
                                if (email != null) {
                                    headers.set("X-User-Email", email);
                                }
                            })
                            .build();

                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

