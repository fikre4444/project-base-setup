package com.sample.sampleservice.shared.advice.infrastructure.primary;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.sample.sampleservice.shared.error.domain.AssertionException;
import com.sample.sampleservice.shared.error.domain.ErrorResponse;
import com.sample.sampleservice.shared.error.domain.GeneratorException;
import com.sample.sampleservice.shared.openfeign.infrastructure.secondary.OpenFeignErrorDecoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class RestControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public final ResponseEntity<Object> handleAllGeneratorException(GeneratorException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.key() != null ? ex.key().get() : "", ex.getMessage()));
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleAllAssertionException(AssertionException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.type().name(), ex.getMessage()));
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleSqlException(JDBCException ex, WebRequest req) {
        return ResponseEntity.internalServerError()
                .body(buildError("INTERNAL_SERVER_ERROR", "Something went wrong."));
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleSqlException(OpenFeignErrorDecoder.OpenFeignException ex, WebRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(buildError("REQUEST_FAILED", ex.getErrorResponse() != null ? ex.getErrorResponse().getError_description() : "Something went wrong"));
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError("ACCESS_DENIED", ex.getMessage()));
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleConstraintException(ConstraintViolationException ex, WebRequest req) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(error -> String.format("%s. ", error.getMessage()))
                .collect(Collectors.joining());
        return ResponseEntity.badRequest()
                .body(buildError("INVALID_REQUEST", message));
    }

    @ExceptionHandler
    public final ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest req) {
        return ResponseEntity.badRequest()
                .body(buildError("INVALID_REQUEST", ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        StringBuilder messageBuilder = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            FieldError field = ((FieldError) error);

            List<String> list = new ArrayList<>(Arrays.stream(field.getField().split("\\."))
                    .toList());
            messageBuilder
                    .append(String.join(" ", list))
                    .append(": ")
                    .append(field.getDefaultMessage())
                    .append(". ");
        });

        return ResponseEntity.badRequest()
                .body(buildError("INVALID_REQUEST", messageBuilder.toString()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError("MESSAGE_NOT_READABLE", ex.getLocalizedMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ErrorResponse buildError(final String code, final String message) {
        return new ErrorResponse().code(code).message(message);
    }
}
