package com.demo.advice;

import com.demo.dto.BaseResponse;
import com.demo.dto.ErrorDetail;
import com.demo.exception.BadRequestException;
import com.demo.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        BaseResponse<Map<String, String>> response = BaseResponse.<Map<String, String>>builder()
                .success(false)
                .results(errors)
                .error(ErrorDetail.builder()
                        .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .message("Validation error")
                        .build())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle resource not found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        BaseResponse<?> response = BaseResponse.fail(
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse<?>> handleBadRequestException(
            BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());

        BaseResponse<?> response = BaseResponse.fail(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.fail("403", "Access denied: " + ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.fail("401", "Authentication failed: " + ex.getMessage()));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleGeneralException(
            Exception ex) {
        log.error("Internal server error: ", ex);

        BaseResponse<?> response = BaseResponse.fail(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "Internal server error"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
