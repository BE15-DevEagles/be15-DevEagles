package com.deveagles.be15_deveagles_be.features.chat.command.domain.exception;

public class HuggingFaceApiException extends RuntimeException {
  private final String errorCode;

  public HuggingFaceApiException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public static class ValidationException extends HuggingFaceApiException {
    public ValidationException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ConnectionException extends HuggingFaceApiException {
    public ConnectionException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ClientException extends HuggingFaceApiException {
    public ClientException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ServerException extends HuggingFaceApiException {
    public ServerException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class ParsingException extends HuggingFaceApiException {
    public ParsingException(String message, String errorCode) {
      super(message, errorCode);
    }
  }

  public static class UnexpectedException extends HuggingFaceApiException {
    public UnexpectedException(String message, String errorCode) {
      super(message, errorCode);
    }
  }
}
