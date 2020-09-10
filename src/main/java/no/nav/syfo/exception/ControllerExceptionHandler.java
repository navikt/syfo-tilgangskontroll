package no.nav.syfo.exception;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
import no.nav.syfo.metric.Metric;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

import javax.inject.Inject;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    private final String BAD_REQUEST_MSG = "Vi kunne ikke tolke inndataene";
    private final String INTERNAL_MSG = "Det skjedde en uventet feil";
    private final String UNAUTHORIZED_MSG = "Autorisasjonsfeil";

    private Metric metric;

    @Inject
    public ControllerExceptionHandler(Metric metric) {
        this.metric = metric;
    }

    @ExceptionHandler({
            Exception.class,
            IllegalArgumentException.class,
    })
    public final ResponseEntity<ApiError> handleException(Exception ex, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();

        if (ex instanceof JwtTokenUnauthorizedException) {
            JwtTokenUnauthorizedException notAuthorizedException = (JwtTokenUnauthorizedException) ex;

            return handleOIDCUnauthorizedException(notAuthorizedException, headers, request);
        } else if (ex instanceof IllegalArgumentException) {
            IllegalArgumentException illegalArgumentException = (IllegalArgumentException) ex;

            return handleIllegalArgumentException(illegalArgumentException, headers, request);
        } else {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

            return handleExceptionInternal(ex, new ApiError(status.value(), INTERNAL_MSG), headers, status, request);
        }
    }

    private ResponseEntity<ApiError> handleOIDCUnauthorizedException(JwtTokenUnauthorizedException ex, HttpHeaders headers, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return handleExceptionInternal(ex, new ApiError(status.value(), UNAUTHORIZED_MSG), headers, status, request);
    }

    private ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex, HttpHeaders headers, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return handleExceptionInternal(ex, new ApiError(status.value(), BAD_REQUEST_MSG), headers, status, request);
    }

    private ResponseEntity<ApiError> handleExceptionInternal(Exception ex, ApiError body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        metric.tellHttpKall(status.value());

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            log.error("Uventet feil: {} : {}", ex.getClass().toString(), ex.getMessage(), ex);
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }

        return new ResponseEntity<>(body, headers, status);
    }
}
