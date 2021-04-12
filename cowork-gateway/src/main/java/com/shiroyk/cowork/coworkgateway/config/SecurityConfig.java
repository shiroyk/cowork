package com.shiroyk.cowork.coworkgateway.config;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS).permitAll() // 放行所有options请求
            .pathMatchers("/oauth/**").permitAll() // 放行认证路径
            .pathMatchers("/collab/**").permitAll() // 放行Websocket
            .pathMatchers(HttpMethod.GET,"/doc/image/**").permitAll() // 放行文档图片Get请求
            .pathMatchers("/collab/onlineUser").hasAnyAuthority("SCOPE_Admin")
            .pathMatchers("/admin/**").hasAnyAuthority("SCOPE_Admin") // 对admin路径进行鉴权
            .anyExchange().authenticated()
            .and()
            .exceptionHandling()
            .accessDeniedHandler(accessDeniedHandler())
            .authenticationEntryPoint(authenticationEntryPoint())
            .and().cors().and().csrf().disable()
            .oauth2ResourceServer().jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter());
        return http.build();
    }

    @Bean
    ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) ->
                Mono.defer(() -> Mono.just(exchange.getResponse()))
                        .flatMap(response -> {
                            response.setStatusCode(HttpStatus.OK);
                            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                            String body = APIResponse.create(ResultCode.Forbidden, "无权访问！").toJsonStr();
                            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                            return response.writeWith(Mono.just(buffer))
                                    .doOnError(error -> DataBufferUtils.release(buffer));
                        });
    }

    @Bean
    ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, e) ->
                Mono.defer(() -> Mono.just(exchange.getResponse()))
                        .flatMap(response -> {
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);
                            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                            String body = APIResponse.create(ResultCode.UnAuthorized, "Token无效或已过期！").toJsonStr();
                            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                            return response.writeWith(Mono.just(buffer))
                                    .doOnError(error -> DataBufferUtils.release(buffer));
                        });
    }

    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
