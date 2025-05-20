package com.deveagles.be15_deveagles_be.features.chat.query.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository.SpringDataMongoChatRoomRepository;
import com.deveagles.be15_deveagles_be.features.chat.query.domain.repository.ChatroomQueryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoChatroomQueryRepositoryImpl implements ChatroomQueryRepository {

  private final MongoTemplate mongoTemplate;
  private final SpringDataMongoChatRoomRepository chatRoomRepository;

  @Override
  public List<ChatRoom> findChatrooms(String teamId, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    if (teamId != null && !teamId.isBlank()) {
      Page<ChatRoom> chatRoomPage =
          chatRoomRepository.findByTeamIdAndDeletedAtIsNull(teamId, pageRequest);
      return chatRoomPage.getContent();
    } else {
      Query query = new Query(Criteria.where("deletedAt").isNull());
      query.with(pageRequest);
      return mongoTemplate.find(query, ChatRoom.class);
    }
  }

  @Override
  public int countChatrooms(String teamId) {
    if (teamId != null && !teamId.isBlank()) {
      Query query = new Query(Criteria.where("teamId").is(teamId).and("deletedAt").isNull());
      return (int) mongoTemplate.count(query, ChatRoom.class);
    } else {
      Query query = new Query(Criteria.where("deletedAt").isNull());
      return (int) mongoTemplate.count(query, ChatRoom.class);
    }
  }

  @Override
  public Optional<ChatRoom> findChatroomById(String chatroomId) {
    if (chatroomId == null || chatroomId.isBlank()) {
      return Optional.empty();
    }

    // deletedAt이 null인 채팅방만 조회
    Query query = new Query(Criteria.where("_id").is(chatroomId).and("deletedAt").isNull());

    ChatRoom chatRoom = mongoTemplate.findOne(query, ChatRoom.class);
    return Optional.ofNullable(chatRoom);
  }

  @Override
  public Optional<ChatRoom> findChatroomReadSummaryById(String chatroomId) {
    // 채팅방의 읽음 상태 요약을 조회하는 것은 기본적으로 채팅방을 조회하는 것과 동일
    // 실제 읽음 상태는 메시지 읽음 상태와 연관됨
    return findChatroomById(chatroomId);
  }
}
