package common.constants;

import io.grpc.Context;
import io.grpc.Metadata;

public class GrpcConstants {
    public static final Context.Key<String> CTX_REQUEST_ID = Context.keyWithDefault(KeyConstants.KEY_REQUEST_ID, "unknown");
    public static final Context.Key<String> CTX_USER_ID = Context.keyWithDefault(KeyConstants.KEY_USER_ID, "unknown");
    public static final Metadata.Key<String> META_REQUEST_ID = Metadata.Key.of(KeyConstants.KEY_REQUEST_ID, Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> META_USER_ID = Metadata.Key.of(KeyConstants.KEY_USER_ID, Metadata.ASCII_STRING_MARSHALLER);
}
