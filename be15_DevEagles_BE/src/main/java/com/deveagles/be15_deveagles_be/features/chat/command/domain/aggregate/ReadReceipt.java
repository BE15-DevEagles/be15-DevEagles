package com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "read_receipt")
@CompoundIndexes({
  @CompoundIndex(name = "message_user_idx", def = "{'messageId': 1, 'userId': 1}", unique = true),
  @CompoundIndex(name = "chatroom_user_idx", def = "{'chatroomId': 1, 'userId': 1}")
})
@Getter
@Builder
public class ReadReceipt {

  @Id private String id;

  private String messageId;

  private String userId;

  private String chatroomId;

  private LocalDateTime readAt;
}
