package com.deveagles.be15_deveagles_be.features.user.command.repository;

import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findUserByEmail(String email);

  Optional<User> findUserByPhoneNumber(String phoneNumber);

  @Query(
      "SELECT u FROM User u WHERE u.email = :email AND (u.deletedAt IS NULL OR u.deletedAt > :threshold)")
  Optional<User> findValidUserForLogin(
      @Param("email") String email, @Param("threshold") LocalDateTime threshold);

  Optional<User> findUserByUserId(Long userId);

  @Query(
      "SELECT u FROM User u WHERE u.userName = :userName AND u.phoneNumber = :phoneNumber AND (u.deletedAt IS NULL OR u.deletedAt > :threshold)")
  Optional<User> findValidUserForGetEmail(
      @Param("userName") String userName,
      @Param("phoneNumber") String phoneNumber,
      @Param("threshold") LocalDateTime threshold);
}
