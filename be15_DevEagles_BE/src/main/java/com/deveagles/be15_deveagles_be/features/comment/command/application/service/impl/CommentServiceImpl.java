package com.deveagles.be15_deveagles_be.features.comment.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentCreateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.response.CommentResponse;
import com.deveagles.be15_deveagles_be.features.comment.command.application.service.CommentService;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate.Comment;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.exception.CommentBusinessException;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.exception.CommentErrorCode;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.repository.CommentRepository;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
  private final WorklogService worklogService;
  private final CommentRepository commentRepository;
  private final UserCommandService userCommandService;

  @Transactional
  @Override
  public CommentResponse registerComment(Long userId, CommentCreateRequest request) {
    validateUserExists(userId);
    if (worklogService.getWorklogById(request.getWorklogId(), userId).getWorklogId() == null) {
      throw new CommentBusinessException(CommentErrorCode.INVALID_REQUEST);
    }

    validateWorklogAccess(userId, request.getWorklogId());

    String userName = userCommandService.getUserDetails(userId).getUserName();
    Comment comment =
        Comment.builder()
            .worklogId(request.getWorklogId())
            .commentContent(request.getCommentContent())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .userId(userId)
            .build();

    commentRepository.save(comment);

    return CommentResponse.builder()
        .commentId(comment.getCommentId())
        .commentContent(comment.getCommentContent())
        .time(comment.getCreatedAt())
        .username(userName)
        .isEdited(false)
        .build();
  }

  public void validateUserExists(Long userId) {
    UserDetailResponse detail = userCommandService.getUserDetails(userId);
    if (detail == null || detail.getUserId() == null) {
      throw new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION);
    }
  }

  private void validateWorklogAccess(Long userId, Long worklogId) {
    try {
      worklogService.getWorklogById(worklogId, userId);
    } catch (CommentBusinessException e) {
      throw new CommentBusinessException(CommentErrorCode.INVALID_REQUEST, "해당 워크로그에 접근할 수 없습니다.");
    }
  }
}
