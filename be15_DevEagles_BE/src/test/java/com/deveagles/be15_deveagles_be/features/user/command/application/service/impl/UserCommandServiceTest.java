package com.deveagles.be15_deveagles_be.features.user.command.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import lombok.extern.slf4j.Slf4j;
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

  @DisplayName("사용자 회원가입 테스트")
  @Test
  void testUserRegister() {

    String email = "eagles@email.com";
    String password = "eagles1234";
    String userName = "김이글";
    String phoneNumber = "01012345678";

    UserCreateRequest request =
        UserCreateRequest.builder()
            .email(email)
            .password(password)
            .userName(userName)
            .phoneNumber(phoneNumber)
            .build();

    assertDoesNotThrow(() -> userCommandService.userRegister(request));
  }
}
