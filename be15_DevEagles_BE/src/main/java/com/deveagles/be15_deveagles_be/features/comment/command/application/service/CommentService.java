package com.deveagles.be15_deveagles_be.features.comment.command.application.service;

import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentCreateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentUpdateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
  CommentResponse registerComment(Long userId, CommentCreateRequest request);

  List<CommentResponse> getComments(Long worklogId, Long userId);

  void updateComment(Long commentId, CommentUpdateRequest request, Long userId);
}
