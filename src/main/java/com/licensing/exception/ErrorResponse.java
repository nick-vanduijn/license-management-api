package com.licensing.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response for API exceptions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private String error;
  private String message;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
  private Instant timestamp;
  private String path;
  private List<ValidationError> validationErrors;

  public ErrorResponse() {
    this.timestamp = Instant.now();
  }

  public ErrorResponse(String error, String message, String path) {
    this();
    this.error = error;
    this.message = message;
    this.path = path;
  }

  public ErrorResponse(String error, String message, String path, List<ValidationError> validationErrors) {
    this(error, message, path);
    this.validationErrors = validationErrors;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<ValidationError> getValidationErrors() {
    return validationErrors;
  }

  public void setValidationErrors(List<ValidationError> validationErrors) {
    this.validationErrors = validationErrors;
  }

  /**
   * Validation error details.
   */
  public static class ValidationError {
    private String field;
    private Object rejectedValue;
    private String message;

    public ValidationError() {
    }

    public ValidationError(String field, Object rejectedValue, String message) {
      this.field = field;
      this.rejectedValue = rejectedValue;
      this.message = message;
    }

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public Object getRejectedValue() {
      return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
      this.rejectedValue = rejectedValue;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
