package common.interceptor;

import common.constants.GrpcConstants;
import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * Context with request id and user id.
 * Usage of the id:
 * <pre>
 *     GrpcConstants.CTX_REQUEST_ID.get();
 *     GrpcConstants.CTX_USER_ID.get();
 * </pre>
 */
@GrpcGlobalServerInterceptor
public class MetadataInterceptor implements ServerInterceptor {

    /**
     * Intercept {@link ServerCall} dispatch by the {@code next} {@link ServerCallHandler}. General
     * semantics of {@link ServerCallHandler#startCall} apply and the returned
     * {@link ServerCall.Listener} must not be {@code null}.
     *
     * <p>If the implementation throws an exception, {@code call} will be closed with an error.
     * Implementations must not throw an exception if they started processing that may use {@code
     * call} on another thread.
     *
     * @param call    object to receive response messages
     * @param headers which can contain extra call metadata from {@link ClientCall#start},
     *                e.g. authentication credentials.
     * @param next    next processor in the interceptor chain
     * @return listener for processing incoming messages for {@code call}, never {@code null}.
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context ctx = Context.current().withValues(GrpcConstants.CTX_REQUEST_ID, headers.get(GrpcConstants.META_REQUEST_ID),
                GrpcConstants.CTX_USER_ID, headers.get(GrpcConstants.META_USER_ID));
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
