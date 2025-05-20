package com.deveagles.be15_deveagles_be.features.user.command.exception;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserUpdateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

class UserCommandServiceImplTest {

  private UserRepository userRepository;
  private ModelMapper modelMapper;
  private PasswordEncoder passwordEncoder;
  private MultipartFile profile;
  private UserCommandServiceImpl userCommandService;

  private Long invalidUserId;
  private String validPassword;
  private UserCreateRequest createUser;
  private UserUpdateRequest updateUser;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    modelMapper = mock(ModelMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);
    profile = mock(MultipartFile.class);
    userCommandService = new UserCommandServiceImpl(userRepository, modelMapper, passwordEncoder);

    invalidUserId = -1L;
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

  @Test
  @DisplayName("회원가입 예외 - 중복 이메일")
  void testUserRegister_DuplicateEmail() {
    // given
    when(userRepository.findUserByEmail(createUser.email()))
        .thenReturn(Optional.of(mock(User.class)));

    // when & then
    assertThatThrownBy(() -> userCommandService.userRegister(createUser))
        .isInstanceOf(UserBusinessException.class)
        .hasMessageContaining(UserErrorCode.DUPLICATE_USER_EMAIL_EXCEPTION.getMessage());
  }

  @Test
  @DisplayName("회원가입 예외 - 중복 전화번호")
  void testUserRegister_DuplicatePhone() {
    // given
    when(userRepository.findUserByEmail(createUser.email())).thenReturn(Optional.empty());
    when(userRepository.findUserByPhoneNumber(createUser.phoneNumber()))
        .thenReturn(Optional.of(mock(User.class)));

    // when & then
    assertThatThrownBy(() -> userCommandService.userRegister(createUser))
        .isInstanceOf(UserBusinessException.class)
        .hasMessageContaining(UserErrorCode.DUPLICATE_USER_PHONE_EXCEPTION.getMessage());
  }

  @Test
  @DisplayName("회원 조회 관련 예외 - 존재하지 않는 사용자")
  void testUserNotFound() {
    // given
    when(userRepository.findUserByUserId(invalidUserId)).thenReturn(Optional.empty());

    // then - getUserDetails
    assertThatThrownBy(() -> userCommandService.getUserDetails(invalidUserId))
        .isInstanceOf(UserBusinessException.class)
        .hasMessageContaining(UserErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());

    // then - updateUserPassword
    assertThatThrownBy(() -> userCommandService.updateUserPassword(invalidUserId, validPassword))
        .isInstanceOf(UserBusinessException.class)
        .hasMessageContaining(UserErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());

    // then - updateUserDetails
    assertThatThrownBy(
            () -> userCommandService.updateUserDetails(invalidUserId, updateUser, profile))
        .isInstanceOf(UserBusinessException.class)
        .hasMessageContaining(UserErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());

    // then - validUserPassword
    assertThatThrownBy(() -> userCommandService.validUserPassword(invalidUserId, validPassword))
        .isInstanceOf(UserBusinessException.class)
        .hasMessageContaining(UserErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());
  }
}
