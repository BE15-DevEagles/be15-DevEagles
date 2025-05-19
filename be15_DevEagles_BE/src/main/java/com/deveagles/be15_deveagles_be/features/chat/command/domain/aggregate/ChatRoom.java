package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "chatroom")
@Getter
@Builder
public class ChatRoom {

  @Id private String id;

  private String teamId;

  private String name;

  @Field("default")
  @Builder.Default
  private boolean isDefault = false;

  private String userId;

  private ChatRoomType type;

  private LastMessageInfo lastMessage;

  @Builder.Default private List<Participant> participants = new ArrayList<>();

  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;

  @Getter
  @Builder
  public static class LastMessageInfo {
    private String id;
    private String content;
    private String senderId;
    private String senderName;
    private LocalDateTime sentAt;
  }

  @Getter
  @Builder
  public static class Participant {
    private String userId;
    private String lastReadMessageId;
    @Builder.Default private boolean notificationEnabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public boolean isActive() {
      return deletedAt == null;
    }

    public boolean isDeleted() {
      return deletedAt != null;
    }

    public void delete() {
      this.deletedAt = LocalDateTime.now();
    }

    public void updateLastReadMessage(String messageId) {
      this.lastReadMessageId = messageId;
    }

    public void toggleNotification() {
      this.notificationEnabled = !this.notificationEnabled;
    }
  }

  public enum ChatRoomType {
    GROUP,
    DIRECT,
    TEAM,
    AI
  }

  public boolean isActive() {
    return deletedAt == null;
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void updateLastMessage(LastMessageInfo lastMessage) {
    this.lastMessage = lastMessage;
  }

  public boolean isAiChatRoom() {
    return type == ChatRoomType.AI && userId != null;
  }

  public void addParticipant(String userId) {
    participants.add(
        Participant.builder()
            .userId(userId)
            .notificationEnabled(true)
            .createdAt(LocalDateTime.now())
            .build());
  }

  public void addParticipants(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return;
    }

    for (String userId : userIds) {
      addParticipant(userId);
    }
  }

  public void removeParticipant(String userId) {
    participants.stream()
        .filter(p -> p.getUserId().equals(userId) && !p.isDeleted())
        .findFirst()
        .ifPresent(Participant::delete);
  }

  public Participant getParticipant(String userId) {
    return participants.stream()
        .filter(p -> p.getUserId().equals(userId) && !p.isDeleted())
        .findFirst()
        .orElse(null);
  }

  public List<Participant> getActiveParticipants() {
    return participants.stream().filter(p -> !p.isDeleted()).toList();
  }
}
