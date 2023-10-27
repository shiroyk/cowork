package common.utils

import io.grpc.Status
import org.springframework.http.HttpStatus


/**
 * covert the HttpStatus to grpc Status
 */
fun toGrpcStatus(status: HttpStatus): Status {
    return when (status) {
        HttpStatus.BAD_REQUEST -> Status.INVALID_ARGUMENT
        HttpStatus.REQUEST_TIMEOUT -> Status.DEADLINE_EXCEEDED
        HttpStatus.NOT_FOUND -> Status.NOT_FOUND
        HttpStatus.CONFLICT -> Status.ALREADY_EXISTS
        HttpStatus.FORBIDDEN -> Status.UNAUTHENTICATED
        HttpStatus.NOT_IMPLEMENTED -> Status.UNIMPLEMENTED
        HttpStatus.INTERNAL_SERVER_ERROR -> Status.INTERNAL
        HttpStatus.SERVICE_UNAVAILABLE -> Status.UNAVAILABLE
        else -> if (status.value() >= 400) Status.INVALID_ARGUMENT else Status.OK
    }
}

/**
 * covert the HttpStatus to grpc Status
 */
fun HttpStatus.toGrpcStatus(status: HttpStatus) = common.utils.toGrpcStatus(status)