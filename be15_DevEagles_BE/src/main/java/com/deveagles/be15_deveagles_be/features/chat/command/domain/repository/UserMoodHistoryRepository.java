package com.deveagles.be15_deveagles_be.features.chat.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.UserMoodHistory.MoodType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMoodHistoryRepository extends JpaRepository<UserMoodHistory, Long> {

  List<UserMoodHistory> findByUserId(String userId);

  List<UserMoodHistory> findByUserIdOrderByCreatedAtDesc(String userId);

  List<UserMoodHistory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  List<UserMoodHistory> findByUserIdAndCreatedAtBetween(
      String userId, LocalDateTime start, LocalDateTime end);

  Optional<UserMoodHistory> findByInquiryId(String inquiryId);

  boolean existsByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

  Optional<UserMoodHistory> findFirstByUserIdOrderByCreatedAtDesc(String userId);

  List<UserMoodHistory> findByUserIdAndMoodType(String userId, MoodType moodType);

  @Query(
      "SELECT h.moodType, COUNT(h) as count FROM UserMoodHistory h "
          + "WHERE h.userId = :userId AND h.createdAt BETWEEN :startDate AND :endDate "
          + "GROUP BY h.moodType ORDER BY count DESC")
  List<Object[]> findDominantMoodTypeInPeriod(
      @Param("userId") String userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  List<UserMoodHistory> findByUserIdAndIntensityGreaterThanEqual(String userId, int minIntensity);

  @Query(
      "SELECT h FROM UserMoodHistory h WHERE h.userId = :userId AND h.emotionAnalysis IS NOT NULL AND h.emotionAnalysis <> ''")
  List<UserMoodHistory> findByUserIdWithEmotionAnalysis(@Param("userId") String userId);
}
