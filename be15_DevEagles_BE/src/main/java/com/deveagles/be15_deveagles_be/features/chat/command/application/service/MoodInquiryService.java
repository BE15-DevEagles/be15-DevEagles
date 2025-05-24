package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import java.util.List;
import java.util.Optional;

public interface MoodInquiryService {

  UserMoodHistory generateMoodInquiry(String userId);

  UserMoodHistory saveMoodAnswer(String inquiryId, String userAnswer);

  List<UserMoodHistory> getUserMoodHistory(String userId);

  void sendMoodInquiryToAllUsers();

  // 비즈니스 로직 일관성을 위한 추가 메서드들
  Optional<UserMoodHistory> getTodayMoodInquiry(String userId);

  Optional<UserMoodHistory> getTodayUnansweredInquiry(String userId);

  boolean hasPendingMoodInquiry(String userId);

  Optional<String> getPendingInquiryId(String userId);
}
