package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoChatRoomRepository extends MongoRepository<ChatRoom, String> {
  List<ChatRoom> findByTeamId(String teamId);

  List<ChatRoom> findByTeamIdAndDeletedAtIsNull(String teamId);

  Page<ChatRoom> findByTeamIdAndDeletedAtIsNull(String teamId, Pageable pageable);

  Page<ChatRoom> findByTypeAndDeletedAtIsNull(ChatRoomType type, Pageable pageable);

  @Query("{'teamId': ?0, 'default': ?1, 'deletedAt': null}")
  Optional<ChatRoom> findByTeamIdAndDefaultAndDeletedAtIsNull(String teamId, boolean isDefault);

  List<ChatRoom> findByTeamIdAndTypeAndDeletedAtIsNull(String teamId, ChatRoomType type);

  Optional<ChatRoom> findByTeamIdAndUserIdAndTypeAndDeletedAtIsNull(
      String teamId, String userId, ChatRoomType type);
}
