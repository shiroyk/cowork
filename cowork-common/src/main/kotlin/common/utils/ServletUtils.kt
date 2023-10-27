package common.utils

import common.constants.KeyConstants
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Return the Request currently bound to the thread.
 */
fun getRequest() = getRequestAttributes()?.request

/**
 * Return the Response currently bound to the thread.
 */
fun getResponse() = getRequestAttributes()?.response

/**
 * Return the Session currently bound to the thread.
 */
fun getSession() = getRequest()?.session

/**
 * Return the RequestAttributes currently bound to the thread.
 */
fun getRequestAttributes() = runCatching {
    RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
}.getOrNull()

/**
 * Return the request id currently bound to the thread.
 */
fun getRequestId(): String = getRequest()?.getHeader(KeyConstants.HEADER_REQUEST_ID) ?: "unknown"

/**
 * Return the user id currently bound to the thread.
 */
fun getUserId(): String = getRequest()?.getHeader(KeyConstants.HEADER_USER_ID) ?: "unknown"