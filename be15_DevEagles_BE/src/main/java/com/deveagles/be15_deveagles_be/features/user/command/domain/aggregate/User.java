package com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_Id")
  private Long userId;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "user_name", nullable = false)
  private String userName;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_status", nullable = false)
  private UserStatus userStatus = UserStatus.PENDING;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "modified_at", nullable = false)
  private LocalDateTime modifiedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "user_thumbnail_url")
  private String userThumbnailUrl;

  @Builder
  public User(
      String email,
      String password,
      String userName,
      String phoneNumber,
      UserStatus userStatus,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt,
      LocalDateTime deletedAt,
      String user_thumbnail_url) {
    this.email = email;
    this.password = password;
    this.userName = userName;
    this.phoneNumber = phoneNumber;
    this.userStatus = userStatus;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
    this.deletedAt = deletedAt;
    this.userThumbnailUrl = user_thumbnail_url;
  }

  public void modifyUserInfo(String userName, String phoneNumber) {
    this.userName = userName;
    this.phoneNumber = phoneNumber;
  }

  public void setProfile(String userThumbnailUrl) {
    this.userThumbnailUrl = userThumbnailUrl;
  }

  public void setEncodedPassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void deleteUser(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public void returnUser() {
    this.deletedAt = null;
  }

  public void setEnabledUser(UserStatus userStatus) {
    this.userStatus = userStatus;
  }
}
