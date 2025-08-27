package com.licensing.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the licensing API.
 * Provides consistent error responses across all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFound(
      EntityNotFoundException ex, HttpServletRequest request) {
    logger.warn("Entity not found: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        "ENTITY_NOT_FOUND",
        ex.getMessage(),
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    logger.warn("Validation error: {}", ex.getMessage());

    List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> new ErrorResponse.ValidationError(
            error.getField(),
            error.getRejectedValue(),
            error.getDefaultMessage()))
        .collect(Collectors.toList());

    ErrorResponse error = new ErrorResponse(
        "VALIDATION_ERROR",
        "Validation failed",
        request.getRequestURI(),
        validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    logger.warn("Constraint violation: {}", ex.getMessage());

    List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
        .stream()
        .map(violation -> new ErrorResponse.ValidationError(
            violation.getPropertyPath().toString(),
            violation.getInvalidValue(),
            violation.getMessage()))
        .collect(Collectors.toList());

    ErrorResponse error = new ErrorResponse(
        "VALIDATION_ERROR",
        "Validation failed",
        request.getRequestURI(),
        validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    logger.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        "INVALID_REQUEST",
        ex.getMessage(),
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedOperation(
      UnsupportedOperationException ex, HttpServletRequest request) {
    logger.warn("Unsupported operation: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        "NOT_IMPLEMENTED",
        ex.getMessage(),
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    logger.warn("Method not allowed: {}", ex.getMessage());

    String message = String.format("HTTP method %s is not supported for this endpoint", ex.getMethod());
    ErrorResponse error = new ErrorResponse(
        "METHOD_NOT_ALLOWED",
        message,
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(
      MissingRequestHeaderException ex, HttpServletRequest request) {
    logger.warn("Missing required header: {}", ex.getMessage());

    String message = "X-Tenant-ID header is required";
    if (!"X-Tenant-ID".equals(ex.getHeaderName())) {
      message = String.format("Required header '%s' is missing", ex.getHeaderName());
    }

    ErrorResponse error = new ErrorResponse(
        "MISSING_TENANT",
        message,
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    logger.warn("Message not readable: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        "INVALID_REQUEST_BODY",
        "Request body is malformed or unreadable",
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    logger.warn("No handler found: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        "ENDPOINT_NOT_FOUND",
        String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()),
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    logger.error("Unexpected error occurred", ex);

    ErrorResponse error = new ErrorResponse(
        "INTERNAL_ERROR",
        "An unexpected error occurred",
        request.getRequestURI());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
