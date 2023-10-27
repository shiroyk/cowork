package common.handler;

import common.constants.GrpcConstants;
import common.exception.ApiException;
import common.utils.GrpcUtilsKt;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the GRPC exceptions
 */
@GrpcAdvice
public class GrpcExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(GrpcExceptionAdvice.class);

    @GrpcExceptionHandler
    public Status exceptionHandler(Throwable e) {
        logger.error(String.format("request_id %s user_id %s", GrpcConstants.CTX_REQUEST_ID.get(), GrpcConstants.CTX_USER_ID.get()), e);
        return Status.INTERNAL.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(ApiException.class)
    public Status apiExceptionHandler(ApiException e) {
        return GrpcUtilsKt.toGrpcStatus(e.getCode()).withDescription(e.getMessage()).withCause(e);
    }
}
