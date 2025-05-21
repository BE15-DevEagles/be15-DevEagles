package com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageListResponse {
  private List<MessageResponse> messages;

  public static MessageListResponse of(List<MessageResponse> messages) {
    return MessageListResponse.builder().messages(messages).build();
  }
}
