package com.deveagles.be15_deveagles_be.features.chat.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.common.dto.PagedResult;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatRoom.ChatRoomType;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.repository.ChatRoomRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class MongoChatRoomRepositoryImpl implements ChatRoomRepository {

  private final SpringDataMongoChatRoomRepository repository;

  public MongoChatRoomRepositoryImpl(SpringDataMongoChatRoomRepository repository) {
    this.repository = repository;
  }

  @Override
  public ChatRoom save(ChatRoom chatRoom) {
    return repository.save(chatRoom);
  }

  @Override
  public Optional<ChatRoom> findById(String id) {
    return repository.findById(id);
  }

  @Override
  public List<ChatRoom> findByTeamId(String teamId) {
    return repository.findByTeamId(teamId);
  }

  @Override
  public List<ChatRoom> findActiveChatRoomsByTeamId(String teamId) {
    return repository.findByTeamIdAndDeletedAtIsNull(teamId);
  }

  @Override
  public PagedResult<ChatRoom> findActiveChatRoomsByTeamIdWithPagination(
      String teamId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatRoom> chatRoomPage = repository.findByTeamIdAndDeletedAtIsNull(teamId, pageable);

    return PagedResult.from(chatRoomPage);
  }

  @Override
  public PagedResult<ChatRoom> findActiveChatRoomsByType(ChatRoomType type, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<ChatRoom> chatRoomPage = repository.findByTypeAndDeletedAtIsNull(type, pageable);

    return PagedResult.from(chatRoomPage);
  }

  @Override
  public Optional<ChatRoom> findDefaultChatRoomByTeamId(String teamId) {
    return repository.findByTeamIdAndIsDefaultAndDeletedAtIsNull(teamId, true);
  }

  @Override
  public List<ChatRoom> findActiveChatRoomsByTeamIdAndType(String teamId, ChatRoomType type) {
    return repository.findByTeamIdAndTypeAndDeletedAtIsNull(teamId, type);
  }

  @Override
  public void delete(ChatRoom chatRoom) {
    repository.delete(chatRoom);
  }

  @Override
  public void deleteById(String id) {
    repository.deleteById(id);
  }
}
