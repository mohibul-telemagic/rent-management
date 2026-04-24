package com.renteasebd.config;

import com.renteasebd.common.ApiResponse;
import com.renteasebd.common.AppException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleApp(AppException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "INVALID_CREDENTIALS", "TOKEN_INVALID", "TOKEN_EXPIRED", "ACCOUNT_DISABLED", "UNAUTHORIZED" ->
                HttpStatus.UNAUTHORIZED;
            case "PROPERTY_NOT_FOUND", "UNIT_NOT_FOUND", "TENANT_NOT_FOUND", "UTILITY_CONFIG_NOT_FOUND", "INVOICE_NOT_FOUND",
                "TRANSACTION_NOT_FOUND", "EXPORT_JOB_NOT_FOUND", "EXPENSE_NOT_FOUND", "EXPENSE_RECEIPT_NOT_FOUND",
                "SESSION_NOT_FOUND" ->
                HttpStatus.NOT_FOUND;
            case "UNIT_ALREADY_OCCUPIED", "UNIT_COUNT_MISMATCH", "DUPLICATE_INVOICE", "OVERPAYMENT", "EXPORT_JOB_NOT_READY",
                "DEPOSIT_BOUNDS_EXCEEDED" ->
                HttpStatus.CONFLICT;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "NOT_IMPLEMENTED" -> HttpStatus.NOT_IMPLEMENTED;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status)
            .body(ApiResponse.fail(ex.getCode(), ex.getMessage(), ex.getField()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String field = fieldError == null ? null : fieldError.getField();
        String message = fieldError == null ? "Validation failed" : fieldError.getDefaultMessage();
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail("VALIDATION_ERROR", message, field));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION_ERROR", ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail("INTERNAL_ERROR", "Unexpected server error", null));
    }
}
