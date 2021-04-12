package com.shiroyk.cowork.coworkgateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GateWayExceptionHandler extends DefaultErrorWebExceptionHandler {

    public GateWayExceptionHandler(ErrorAttributes errorAttributes,
                                   ResourceProperties resourceProperties,
                                   ErrorProperties errorProperties,
                                   ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = super.getError(request);
        log.error(
                "请求发生异常，请求URI：{}，请求方法：{}，异常信息：{}",
                request.path(), request.methodName(), error.getMessage()
        );
        int errorCode = HttpStatus.BAD_GATEWAY.value();
        String errorMessage;
        if (error instanceof NotFoundException) {
            errorCode = HttpStatus.SERVICE_UNAVAILABLE.value();
            String serverId = StringUtils.substringAfterLast(error.getMessage(), "Unable to find instance for ");
            serverId = StringUtils.replace(serverId, "\"", StringUtils.EMPTY);
            errorMessage = String.format("无法找到%s服务", serverId);
        } else if (StringUtils.containsIgnoreCase(error.getMessage(), "connection refused")) {
            errorCode = HttpStatus.SERVICE_UNAVAILABLE.value();
            errorMessage = "目标服务拒绝连接";
        } else if (error instanceof TimeoutException) {
            errorCode = HttpStatus.REQUEST_TIMEOUT.value();
            errorMessage = "访问服务超时";
        } else if (error instanceof ResponseStatusException
                && StringUtils.containsIgnoreCase(error.getMessage(), HttpStatus.NOT_FOUND.toString())) {
            errorCode = HttpStatus.NOT_FOUND.value();
            errorMessage = "未找到该资源";
        } else {
            errorMessage = "网关转发异常";
        }
        Map<String, Object> errorAttributes = new HashMap<>(2);
        errorAttributes.put("msg", errorMessage);
        errorAttributes.put("code", errorCode);
        return errorAttributes;
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return HttpStatus.OK.value();
    }
}
