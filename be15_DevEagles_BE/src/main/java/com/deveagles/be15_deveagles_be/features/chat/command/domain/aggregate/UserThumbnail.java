package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_thumbnail")
@Getter
@Builder
public class UserThumbnail {

  @Id private String thumbnailId;

  private String url;

  private String userId;

  private Map<String, Object> metadata;

  private LocalDateTime createdAt;

  private LocalDateTime modifiedAt;
}
