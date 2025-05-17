package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatroomParticipant;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatroomParticipantRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class MongoChatroomParticipantRepositoryImpl implements ChatroomParticipantRepository {

  private final SpringDataMongoChatroomParticipantRepository repository;

  public MongoChatroomParticipantRepositoryImpl(
      SpringDataMongoChatroomParticipantRepository repository) {
    this.repository = repository;
  }

  @Override
  public ChatroomParticipant save(ChatroomParticipant participant) {
    return repository.save(participant);
  }

  @Override
  public Optional<ChatroomParticipant> findById(String id) {
    return repository.findById(id);
  }

  @Override
  public Optional<ChatroomParticipant> findByChatroomIdAndUserId(String chatroomId, String userId) {
    return repository.findByChatroomIdAndUserId(chatroomId, userId);
  }

  @Override
  public List<ChatroomParticipant> findByChatroomId(String chatroomId) {
    return repository.findByChatroomId(chatroomId);
  }

  @Override
  public List<ChatroomParticipant> findActiveByChatroomId(String chatroomId) {
    return repository.findByChatroomIdAndDeletedAtIsNull(chatroomId);
  }

  @Override
  public List<ChatroomParticipant> findByUserIdAndDeletedAtIsNull(String userId) {
    return repository.findByUserIdAndDeletedAtIsNull(userId);
  }

  @Override
  public PagedResult<ChatroomParticipant> findByUserIdAndDeletedAtIsNull(
      String userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatroomParticipant> participantPage =
        repository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    return PagedResult.from(participantPage);
  }

  @Override
  public void delete(ChatroomParticipant participant) {
    repository.delete(participant);
  }

  @Override
  public void deleteById(String id) {
    repository.deleteById(id);
  }

  @Override
  public long countByChatroomIdAndDeletedAtIsNull(String chatroomId) {
    return repository.countByChatroomIdAndDeletedAtIsNull(chatroomId);
  }
}
