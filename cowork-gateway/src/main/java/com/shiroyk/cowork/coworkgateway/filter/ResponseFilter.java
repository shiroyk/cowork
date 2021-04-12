package com.shiroyk.cowork.coworkgateway.filter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ResponseFilter implements GlobalFilter, Ordered {
    private final ObjectMapper objectMapper;

    public ResponseFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * @Description: 统一响应
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();

        ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(response) {
            @NonNull
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // 只封装Json响应
                if (MediaType.APPLICATION_JSON.isCompatibleWith(getDelegate().getHeaders().getContentType())) {
                    Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                        DataBuffer buffer = dataBufferFactory.join(dataBuffers);

                        byte[] content = new byte[buffer.readableByteCount()];
                        buffer.read(content);
                        DataBufferUtils.release(buffer);

                        Integer code = response.getRawStatusCode();
                        if (code != null && code != 401) {
                            content = processResponse(code, content);
                            this.getDelegate().getHeaders().setContentLength(content.length);
                            response.setStatusCode(HttpStatus.OK);
                        }
                        return bufferFactory.wrap(content);
                    }));
                } else {
                    return chain.filter(exchange);
                }
            }
        };
        return chain.filter(exchange.mutate().response(decorator).build());
    }

    private byte[] processResponse(Integer statusCode, byte[] content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            APIResponse<Object> result;
            switch (statusCode) {
                case 200:
                    //封装oauth成功响应
                    if (jsonNode.has("access_token")) {
                        result = APIResponse.ok(jsonNode);
                    } else {
                        //其他成功响应不封装
                        return content;
                    }
                    break;
                case 400:
                case 403:
                    //封装oauth错误响应
                    if (jsonNode.has("error_description")) {
                        result = APIResponse.create(ResultCode.fromCode(statusCode),
                                jsonNode.get("error_description").asText());
                    } else {
                        result = handleErrorMsg(statusCode, jsonNode);
                    }
                    break;
                default:
                    //封装其他错误响应
                    result = handleErrorMsg(statusCode, jsonNode);
            }
            log.debug("ResponseFilter " + result.toJsonStr());
            return objectMapper.writeValueAsString(result)
                    .getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private APIResponse<Object> handleErrorMsg(Integer code, JsonNode jsonNode) {
        String msg = jsonNode.get("message").asText();
        if (StringUtils.isEmpty(msg))
            msg = jsonNode.get("error").asText();
        return APIResponse.create(ResultCode.fromCode(code), msg);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
