package com.deveagles.be15_deveagles_be.features.user.command.application.service.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserUpdateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class UserCommandServiceTest {

  @Autowired UserCommandService userCommandService;

  private Long validUserId;
  private UserCreateRequest createUser;
  private UserUpdateRequest updateUser;
  private String validPassword;

  @BeforeEach
  void setUp() {
    validUserId = 1L;

    validPassword = "eagles1234!";

    updateUser = UserUpdateRequest.builder().userName("김이글").phoneNumber("01088889999").build();

    createUser =
        UserCreateRequest.builder()
            .email("eagles@email.com")
            .password(validPassword)
            .userName("김이글")
            .phoneNumber("01012345678")
            .build();
  }

  @DisplayName("사용자 회원가입 테스트")
  @Test
  void testUserRegister() {
    assertDoesNotThrow(() -> userCommandService.userRegister(createUser));
  }

  @DisplayName("사용자 회원 정보 조회 테스트")
  @Test
  void testGetUserDetails() {

    UserDetailResponse response = userCommandService.getUserDetails(validUserId);

    assertThat(response).isNotNull();

    log.info("## TEST getUserDetails : {}", response.toString());
  }

  @DisplayName("사용자 비밀번호 검증 테스트")
  @Test
  void testValidUserPassword() {

    Boolean is_Valid = userCommandService.validUserPassword(validUserId, validPassword);

    assertThat(is_Valid).isTrue();
  }

  @DisplayName("사용자 회원 정보 (이름, 전화번호) 수정 테스트")
  @Test
  void testUpdateUserDetails() {

    UserDetailResponse response =
        userCommandService.updateUserDetails(validUserId, updateUser, null);

    assertThat(response).isNotNull();
    assertThat(response.getUserName()).isEqualTo(updateUser.userName());
    assertThat(response.getPhoneNumber()).isEqualTo(updateUser.phoneNumber());
  }
}
