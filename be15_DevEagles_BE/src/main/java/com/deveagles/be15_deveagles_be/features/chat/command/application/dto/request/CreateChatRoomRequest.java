package com.deveagles.be15_deveagles_be.features.chat.command.application.dto.request;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {
  private String teamId;
  private String name;
  private ChatRoomType type;
  private boolean isDefault;
  private List<String> participantIds;
}
