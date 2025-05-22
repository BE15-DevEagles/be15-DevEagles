package com.deveagles.be15_deveagles_be.features.comment.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentCreateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.request.CommentUpdateRequest;
import com.deveagles.be15_deveagles_be.features.comment.command.application.dto.response.CommentResponse;
import com.deveagles.be15_deveagles_be.features.comment.command.application.service.CommentService;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate.Comment;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.exception.CommentBusinessException;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.exception.CommentErrorCode;
import com.deveagles.be15_deveagles_be.features.comment.command.domain.repository.CommentRepository;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.TeamMemberResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamMemberCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogDetailResponse;
import com.deveagles.be15_deveagles_be.features.worklog.command.application.service.WorklogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
  private final WorklogService worklogService;
  private final CommentRepository commentRepository;
  private final UserCommandService userCommandService;
  private final TeamMemberCommandService teamMemberCommandService;

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

  @Transactional(readOnly = true)
  @Override
  public List<CommentResponse> getComments(Long worklogId, Long userId) {
    WorklogDetailResponse worklog = worklogService.getWorklogById(worklogId, userId);
    if (worklog.getWorklogId() == null) {
      throw new CommentBusinessException(CommentErrorCode.INVALID_REQUEST);
    }
    Long teamId = worklog.getTeamId();
    validateTeamMemberExists(teamId, userId);

    List<Comment> comments = commentRepository.findByWorklogIdAndDeletedAtIsNull(worklogId);

    return comments.stream()
        .map(
            comment -> {
              LocalDateTime createdAt = comment.getCreatedAt();
              LocalDateTime updatedAt = comment.getUpdatedAt();
              boolean isEdited = updatedAt != null && updatedAt.isAfter(createdAt);
              LocalDateTime time = isEdited ? updatedAt : createdAt;

              String userName =
                  userCommandService.getUserDetails(userId).getUserName(); // 필요하면 나중에 넣기

              return CommentResponse.fromEntity(comment, time, userName, isEdited);
            })
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void updateComment(Long commentId, CommentUpdateRequest request, Long userId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new CommentBusinessException(CommentErrorCode.INVALID_REQUEST));
    validateUserExists(userId);
    if (!comment.getUserId().equals(userId)) {
      throw new CommentBusinessException(CommentErrorCode.NO_PERMISSION, "수정 권한이 없습니다.");
    }
    comment.updateContent(request.getCommentContent());
  }

  @Transactional
  @Override
  public void removeComment(Long commentId, Long userId) {
    validateUserExists(userId);
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(
                () ->
                    new CommentBusinessException(CommentErrorCode.INVALID_REQUEST, "해당 댓글이 없습니다."));

    if (!comment.getUserId().equals(userId)) {
      throw new CommentBusinessException(CommentErrorCode.NO_PERMISSION, "삭제 권한이 없습니다.");
    }

    comment.softDelete();
  }

  public void validateTeamMemberExists(Long teamId, Long userId) {
    TeamMemberResponse detail = teamMemberCommandService.findTeamMember(userId, teamId);
    if (detail == null || detail.getTeamId() == null || detail.getUserId() == null) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER);
    }
  }

  private void validateWorklogAccess(Long userId, Long worklogId) {
    try {
      worklogService.getWorklogById(worklogId, userId);
    } catch (CommentBusinessException e) {
      throw new CommentBusinessException(CommentErrorCode.INVALID_REQUEST, "해당 워크로그에 접근할 수 없습니다.");
    }
  }

  public void validateUserExists(Long userId) {
    UserDetailResponse detail = userCommandService.getUserDetails(userId);
    if (detail == null || detail.getUserId() == null) {
      throw new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION);
    }
  }
}
