package com.example.delivery.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Turns exceptions into consistent JSON error responses:
 *   400 - invalid input, 404 - unknown resource, 409 - business rule violated.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(int status, String error, String message) {
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(NotFoundException ex) {
        return new ErrorResponse(404, "Not Found", ex.getMessage());
    }

    @ExceptionHandler({InvalidStatusTransitionException.class, CourierUnavailableException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflict(RuntimeException ex) {
        return new ErrorResponse(409, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequest(IllegalArgumentException ex) {
        return new ErrorResponse(400, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse unreadable(Exception ex) {
        return new ErrorResponse(400, "Bad Request", "Malformed request: check field types and enum values");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validationFailed(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Validation failed",
                "fieldErrors", fieldErrors);
    }
}
