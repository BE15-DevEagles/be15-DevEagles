package com.deveagles.be15_deveagles_be.features.chat.command.domain.exception;

public class GeminiApiException extends RuntimeException {
  private final String errorCode;

  public GeminiApiException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public static class ValidationException extends GeminiApiException {
    public ValidationException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ConnectionException extends GeminiApiException {
    public ConnectionException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ClientException extends GeminiApiException {
    public ClientException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ServerException extends GeminiApiException {
    public ServerException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ParsingException extends GeminiApiException {
    public ParsingException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class UnexpectedException extends GeminiApiException {
    public UnexpectedException(String message, String errorCode) {
      super(message, errorCode);
    }
  }
}
