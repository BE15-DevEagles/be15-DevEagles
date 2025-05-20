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
public class ChatroomListResponse {
  private Integer totalCount;
  private List<ChatroomResponse> chatrooms;

  public static ChatroomListResponse of(List<ChatroomResponse> chatrooms, int totalCount) {
    return ChatroomListResponse.builder().totalCount(totalCount).chatrooms(chatrooms).build();
  }
}
