package com.deveagles.be15_deveagles_be.common.exception;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deveagles.be15_deveagles_be.features.chat.command.application.controller.ChatRoomController;
import com.deveagles.be15_deveagles_be.features.chat.command.application.service.ChatRoomService;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/** GlobalExceptionHandler 테스트 */
@WebMvcTest(ChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
public class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ChatRoomService chatRoomService;

  @Test
  @DisplayName("BusinessException 발생 시 ApiResponse 형식으로 응답이 반환되는지 확인")
  public void whenBusinessExceptionThrown_thenReturnApiResponse() throws Exception {
    // given
    String chatRoomId = "non-existent-id";
    ChatErrorCode errorCode = ChatErrorCode.CHAT_ROOM_NOT_FOUND;
    when(chatRoomService.deleteChatRoom(chatRoomId))
        .thenThrow(new ChatBusinessException(errorCode));

    // when & then
    mockMvc
        .perform(delete("/api/v1/chatrooms/{chatroomId}", chatRoomId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
        .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
  }

  @Test
  @DisplayName("커스텀 메시지가 있는 BusinessException 발생 시 해당 메시지가 응답에 포함되는지 확인")
  public void whenBusinessExceptionWithCustomMessageThrown_thenReturnCustomMessage()
      throws Exception {
    // given
    String chatRoomId = "non-existent-id";
    ChatErrorCode errorCode = ChatErrorCode.CHAT_ROOM_NOT_FOUND;
    String customMessage = "채팅방 ID: " + chatRoomId + "를 찾을 수 없습니다.";

    when(chatRoomService.deleteChatRoom(chatRoomId))
        .thenThrow(new ChatBusinessException(errorCode, customMessage));

    // when & then
    mockMvc
        .perform(delete("/api/v1/chatrooms/{chatroomId}", chatRoomId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
        .andExpect(jsonPath("$.message").value(customMessage));
  }

  @Test
  @DisplayName("기본 채팅방 삭제 시도 시 적절한 에러 응답이 반환되는지 확인")
  public void whenDefaultChatRoomDeletionAttempted_thenReturnProperErrorResponse()
      throws Exception {
    // given
    String chatRoomId = "default-chatroom-id";
    ChatErrorCode errorCode = ChatErrorCode.DEFAULT_CHATROOM_CANNOT_BE_DELETED;

    when(chatRoomService.deleteChatRoom(chatRoomId))
        .thenThrow(new ChatBusinessException(errorCode));

    // when & then
    mockMvc
        .perform(delete("/api/v1/chatrooms/{chatroomId}", chatRoomId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
        .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
  }
}
