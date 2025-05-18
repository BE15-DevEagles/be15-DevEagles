package com.deveagles.be15_deveagles_be.features.chat.command.application.service;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import java.util.List;

public interface MoodInquiryService {

  UserMoodHistory generateMoodInquiry(String userId);

  UserMoodHistory saveMoodAnswer(String inquiryId, String userAnswer);

  List<UserMoodHistory> getUserMoodHistory(String userId);

  void sendMoodInquiryToAllUsers();
}
