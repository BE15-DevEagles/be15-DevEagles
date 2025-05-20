package com.deveagles.be15_deveagles_be.features.user.command.exception;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
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
public class UserExceptionTest {

  @Autowired UserCommandService userCommandService;

  private Long invalidUserId;

  @BeforeEach
  void setUp() {
    invalidUserId = 9999L;
  }

  @DisplayName("중복된 이메일로 회원가입 시 예외 발생 테스트")
  @Test
  void testUserRegisterDuplicatedEmailException() {

    String email = "nare20027@gmail.com";
    String password = "eagles1234";
    String userName = "김이글";
    String phoneNumber = "01012345678";

    UserErrorCode errorCode = UserErrorCode.DUPLICATE_USER_EMAIL_EXCEPTION;

    UserCreateRequest request =
        UserCreateRequest.builder()
            .email(email)
            .password(password)
            .userName(userName)
            .phoneNumber(phoneNumber)
            .build();

    assertThatThrownBy(() -> userCommandService.userRegister(request))
        .isInstanceOf(UserBusinessException.class)
        .hasMessage(errorCode.getMessage());
  }

  @DisplayName("중복된 전화번호로 회원가입 시 예외 발생 테스트")
  @Test
  void testUserRegisterDuplicatedPhoneNumberException() {

    String email = "eagles@email.com";
    String password = "eagles1234";
    String userName = "김이글";
    String phoneNumber = "01012348888";

    UserErrorCode errorCode = UserErrorCode.DUPLICATE_USER_PHONE_EXCEPTION;

    UserCreateRequest request =
        UserCreateRequest.builder()
            .email(email)
            .password(password)
            .userName(userName)
            .phoneNumber(phoneNumber)
            .build();

    assertThatThrownBy(() -> userCommandService.userRegister(request))
        .isInstanceOf(UserBusinessException.class)
        .hasMessage(errorCode.getMessage());
  }

  @DisplayName("존재하지 않는 사용자가 회원정보 조회 시 예외 발생")
  @Test
  void testGetUserDetailsNotFoundUserException() {

    UserErrorCode errorCode = UserErrorCode.NOT_FOUND_USER_EXCEPTION;

    assertThatThrownBy(() -> userCommandService.getUserDetails(invalidUserId))
        .isInstanceOf(UserBusinessException.class)
        .hasMessage(errorCode.getMessage());
  }
}
