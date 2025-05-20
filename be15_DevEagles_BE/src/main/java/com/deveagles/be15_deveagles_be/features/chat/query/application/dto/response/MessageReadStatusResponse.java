package com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReadStatusResponse {
  private Integer totalParticipants;
  private Integer readCount;
  private Integer unreadCount;
  private List<ReadByUserDto> readBy;
  private List<NotReadByUserDto> notReadBy;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReadByUserDto {
    private String userId;
    private String userName;
    private String userThumbnailUrl;
    private LocalDateTime readAt;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NotReadByUserDto {
    private String userId;
    private String userName;
    private String userThumbnailUrl;
  }

  public Integer getTotalParticipants() {
    return totalParticipants;
  }

  public void setTotalParticipants(Integer totalParticipants) {
    this.totalParticipants = totalParticipants;
  }

  public Integer getReadCount() {
    return readCount;
  }

  public void setReadCount(Integer readCount) {
    this.readCount = readCount;
  }

  public Integer getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(Integer unreadCount) {
    this.unreadCount = unreadCount;
  }

  public List<ReadByUserDto> getReadBy() {
    return readBy;
  }

  public void setReadBy(List<ReadByUserDto> readBy) {
    this.readBy = readBy;
  }

  public List<NotReadByUserDto> getNotReadBy() {
    return notReadBy;
  }

  public void setNotReadBy(List<NotReadByUserDto> notReadBy) {
    this.notReadBy = notReadBy;
  }
}
