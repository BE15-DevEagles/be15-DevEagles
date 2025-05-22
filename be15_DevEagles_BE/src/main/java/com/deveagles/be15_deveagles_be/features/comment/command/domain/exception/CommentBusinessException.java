package com.deveagles.be15_deveagles_be.features.comment.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;

public class CommentBusinessException extends BusinessException {
  public CommentBusinessException(CommentErrorCode errorCode) {
    super(errorCode);
  }

  public CommentBusinessException(CommentErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
