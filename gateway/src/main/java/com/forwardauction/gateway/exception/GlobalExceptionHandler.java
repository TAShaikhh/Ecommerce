package com.forwardauction.gateway.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fmt)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleConflict(IllegalStateException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleForbidden(SecurityException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiError> handleClientError(HttpClientErrorException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String msg = extractDownstreamMessage(ex.getResponseBodyAsString(), ex.getMessage());
        return build(status, msg, req.getRequestURI());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiError> handleServerError(HttpServerErrorException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, "Downstream service error", req.getRequestURI());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiError> handleServiceDown(ResourceAccessException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Downstream service unavailable", req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", req.getRequestURI());
    }

    private String extractDownstreamMessage(String body, String fallback) {
        if (body == null || body.isBlank()) return fallback;
        try {
            JsonNode node = MAPPER.readTree(body);
            JsonNode msgNode = node.get("message");
            if (msgNode != null && !msgNode.isNull()) return msgNode.asText();
        } catch (Exception ignored) { }
        return body;
    }

    private ResponseEntity<ApiError> build(HttpStatus s, String message, String path) {
        ApiError body = new ApiError(Instant.now(), s.value(), s.getReasonPhrase(), message, path);
        return ResponseEntity.status(s).body(body);
    }

    private String fmt(FieldError fe) {
        return fe.getField() + " " + fe.getDefaultMessage();
    }
}
