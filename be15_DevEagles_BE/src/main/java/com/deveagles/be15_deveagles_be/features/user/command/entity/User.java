package com.deveagles.be15_deveagles_be.features.user.command.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_Id")
  private Long userId;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "user_name")
  private String userName;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_status")
  private UserStatus userStatus = UserStatus.PENDING;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "modified_at")
  private LocalDateTime modifiedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Builder
  public User(
      String email,
      String password,
      String userName,
      String phoneNumber,
      UserStatus userStatus,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt,
      LocalDateTime deletedAt) {
    this.email = email;
    this.password = password;
    this.userName = userName;
    this.phoneNumber = phoneNumber;
    this.userStatus = userStatus;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
    this.deletedAt = deletedAt;
  }

  public void setUserInfo(String userName, String phoneNumber) {
    this.userName = userName;
    this.phoneNumber = phoneNumber;
  }

  public void setEncodedPassword(String encodedPassword) {
    this.password = encodedPassword;
  }
}
