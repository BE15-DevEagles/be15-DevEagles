package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatroomParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataMongoChatroomParticipantRepository
    extends MongoRepository<ChatroomParticipant, String> {

  Optional<ChatroomParticipant> findByChatroomIdAndUserId(String chatroomId, String userId);

  List<ChatroomParticipant> findByChatroomId(String chatroomId);

  List<ChatroomParticipant> findByChatroomIdAndDeletedAtIsNull(String chatroomId);

  List<ChatroomParticipant> findByUserIdAndDeletedAtIsNull(String userId);

  Page<ChatroomParticipant> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

  long countByChatroomIdAndDeletedAtIsNull(String chatroomId);
}
