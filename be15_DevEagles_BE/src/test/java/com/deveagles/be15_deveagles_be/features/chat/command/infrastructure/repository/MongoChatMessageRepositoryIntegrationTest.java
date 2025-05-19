package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage.MessageType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

/** MongoDB 실제 연결 통합 테스트 @Disabled("실제 DB 연결이 필요한 테스트") */
@Disabled
@SpringBootTest
public class MongoChatMessageRepositoryIntegrationTest {

  @Autowired private ChatMessageRepository chatMessageRepository;

  @Autowired private MongoTemplate mongoTemplate;

  private final String TEST_CHATROOM_ID = "test-chatroom-" + UUID.randomUUID();

  @BeforeEach
  void setUp() {
    // 테스트 데이터 정리
    cleanupTestData();
  }

  @AfterEach
  void tearDown() {
    // 테스트 데이터 정리
    cleanupTestData();
  }

  private void cleanupTestData() {
    Query query = new Query();
    mongoTemplate.remove(query, ChatMessage.class);
  }

  @Test
  @DisplayName("채팅 메시지 저장 및 조회 테스트")
  void whenSaveChatMessage_thenCanRetrieveIt() {
    // Given
    ChatMessage message = createTestMessage("테스트 메시지");

    // When
    ChatMessage savedMessage = chatMessageRepository.save(message);

    // Then
    assertNotNull(savedMessage.getId());

    Optional<ChatMessage> retrievedMessage = chatMessageRepository.findById(savedMessage.getId());
    assertTrue(retrievedMessage.isPresent());
    assertEquals("테스트 메시지", retrievedMessage.get().getContent());
  }

  @Test
  @DisplayName("채팅방 ID로 모든 메시지 조회 테스트")
  void whenFindByChatroomId_thenReturnsMessages() {
    // Given
    ChatMessage message1 = createTestMessage("메시지 1");
    ChatMessage message2 = createTestMessage("메시지 2");
    ChatMessage message3 = createTestMessage("메시지 3");

    chatMessageRepository.save(message1);
    chatMessageRepository.save(message2);
    chatMessageRepository.save(message3);

    // When
    List<ChatMessage> messages = chatMessageRepository.findByChatroomId(TEST_CHATROOM_ID);

    // Then
    assertEquals(3, messages.size());
  }

  @Test
  @DisplayName("채팅방의 최근 메시지 제한 조회 테스트")
  void whenFindRecentMessagesByChatroomId_thenReturnsLimitedMessages() {
    // Given
    for (int i = 0; i < 10; i++) {
      LocalDateTime createdTime = LocalDateTime.now().minusMinutes(i);
      ChatMessage message =
          ChatMessage.builder()
              .chatroomId(TEST_CHATROOM_ID)
              .senderId("test-user")
              .senderName("테스트 사용자")
              .messageType(MessageType.TEXT)
              .content("메시지 " + i)
              .metadata(Map.of("test", "value"))
              .createdAt(createdTime)
              .build();
      chatMessageRepository.save(message);
    }

    // When
    List<ChatMessage> recentMessages =
        chatMessageRepository.findRecentMessagesByChatroomId(TEST_CHATROOM_ID, 5);

    // Then
    assertEquals(5, recentMessages.size());
    // 최신 메시지부터 정렬되어야 함
    assertTrue(recentMessages.get(0).getContent().contains("메시지"));
  }

  @Test
  @DisplayName("채팅방 메시지 페이징 조회 테스트")
  void whenFindMessagesByChatroomIdWithPagination_thenReturnsPagedResult() {
    // Given
    for (int i = 0; i < 20; i++) {
      LocalDateTime createdTime = LocalDateTime.now().minusMinutes(i);
      ChatMessage message =
          ChatMessage.builder()
              .chatroomId(TEST_CHATROOM_ID)
              .senderId("test-user")
              .senderName("테스트 사용자")
              .messageType(MessageType.TEXT)
              .content("페이지 메시지 " + i)
              .metadata(Map.of("test", "value"))
              .createdAt(createdTime)
              .build();
      chatMessageRepository.save(message);
    }

    // When
    PagedResult<ChatMessage> pagedResult =
        chatMessageRepository.findMessagesByChatroomIdWithPagination(TEST_CHATROOM_ID, 0, 10);

    // Then
    assertEquals(10, pagedResult.getContent().size());
    assertEquals(20, pagedResult.getPagination().getTotalItems());
    assertEquals(2, pagedResult.getPagination().getTotalPages());
  }

  private ChatMessage createTestMessage(String content) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("test", "value");

    ChatMessage message =
        ChatMessage.builder()
            .chatroomId(TEST_CHATROOM_ID)
            .senderId("test-user")
            .senderName("테스트 사용자")
            .messageType(MessageType.TEXT)
            .content(content)
            .metadata(metadata)
            .createdAt(LocalDateTime.now())
            .build();

    return message;
  }
}
