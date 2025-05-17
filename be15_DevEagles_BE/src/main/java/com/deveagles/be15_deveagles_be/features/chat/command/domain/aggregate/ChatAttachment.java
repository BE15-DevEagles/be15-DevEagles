package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_attachment")
@Getter
@Builder
public class ChatAttachment {

  @Id private String id;

  private String messageId;

  private String chatroomId;

  private String fileName;

  private String originalFileName;

  private String contentType;

  private String fileUrl;

  private Long fileSize;

  private AttachmentType attachmentType;

  private Map<String, Object> metadata;

  private String uploaderId;

  private LocalDateTime createdAt;

  private LocalDateTime deletedAt;

  public enum AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    OTHER
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }

  public boolean isImage() {
    return attachmentType == AttachmentType.IMAGE;
  }

  public boolean isVideo() {
    return attachmentType == AttachmentType.VIDEO;
  }

  public boolean isAudio() {
    return attachmentType == AttachmentType.AUDIO;
  }

  public String getThumbnailUrl() {
    if (metadata != null && (isImage() || isVideo())) {
      return (String) metadata.get("thumbnailUrl");
    }
    return null;
  }
}
