package com.deveagles.be15_deveagles_be.features.chat.query.application.service;

import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.MessageReadStatusResponse;

public interface MessageQueryService {

  MessageListResponse getMessages(Long userId, String chatroomId, String before, int limit);

  MessageReadStatusResponse getMessageReadStatus(Long userId, String chatroomId, String messageId);
}
