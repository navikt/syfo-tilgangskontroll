package no.nav.syfo.api.exception

import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import no.nav.syfo.metric.Metric
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.util.WebUtils
import javax.inject.Inject

@ControllerAdvice
class ControllerExceptionHandler @Inject constructor(
    private val metric: Metric
) {
    private val basRequestMsg = "Vi kunne ikke tolke inndataene"
    private val internalMsg = "Det skjedde en uventet feil"
    private val unauthorizedMsg = "Autorisasjonsfeil"

    @ExceptionHandler(Exception::class, IllegalArgumentException::class)
    fun handleException(ex: Exception, request: WebRequest): ResponseEntity<ApiError> {
        val headers = HttpHeaders()
        return when (ex) {
            is JwtTokenUnauthorizedException -> {
                handleOIDCUnauthorizedException(ex, headers, request)
            }
            is IllegalArgumentException -> {
                handleIllegalArgumentException(ex, headers, request)
            }
            else -> {
                val status = HttpStatus.INTERNAL_SERVER_ERROR
                handleExceptionInternal(ex, ApiError(status.value(), internalMsg), headers, status, request)
            }
        }
    }

    private fun handleOIDCUnauthorizedException(ex: JwtTokenUnauthorizedException, headers: HttpHeaders, request: WebRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.UNAUTHORIZED
        return handleExceptionInternal(ex, ApiError(status.value(), unauthorizedMsg), headers, status, request)
    }

    private fun handleIllegalArgumentException(ex: IllegalArgumentException, headers: HttpHeaders, request: WebRequest): ResponseEntity<ApiError> {
        val status = HttpStatus.BAD_REQUEST
        return handleExceptionInternal(ex, ApiError(status.value(), basRequestMsg), headers, status, request)
    }

    private fun handleExceptionInternal(ex: Exception, body: ApiError, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<ApiError> {
        metric.tellHttpKall(status.value())
        if (HttpStatus.INTERNAL_SERVER_ERROR == status || HttpStatus.BAD_REQUEST == status) {
            log.error("Uventet feil: {} : {}", ex.javaClass.toString(), ex.message, ex)
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST)
        }
        return ResponseEntity(body, headers, status)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ControllerExceptionHandler::class.java)
    }
}
