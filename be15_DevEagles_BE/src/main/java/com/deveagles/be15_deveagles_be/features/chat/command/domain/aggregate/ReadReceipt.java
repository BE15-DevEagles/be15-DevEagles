package com.deveagles.be15_deveagles_be.features.chat.command.domain.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "read_receipt")
@Getter
@Builder
public class ReadReceipt {

  @Id private String id;

  private String messageId;

  private String userId;

  private LocalDateTime readAt;
}
