package common.exception

import org.springframework.http.HttpStatus

/**
 * the API exception
 */
class ApiException(val code: HttpStatus, msg: String? = null) : RuntimeException(msg ?: code.reasonPhrase)