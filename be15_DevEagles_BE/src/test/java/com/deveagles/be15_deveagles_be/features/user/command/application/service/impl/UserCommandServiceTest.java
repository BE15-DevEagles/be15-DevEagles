package com.deveagles.be15_deveagles_be.features.user.command.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.amazonaws.services.s3.AmazonS3;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserUpdateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

class UserCommandServiceTest {

  private UserRepository userRepository;
  private ModelMapper modelMapper;
  private PasswordEncoder passwordEncoder;
  private MultipartFile profile;
  private AmazonS3 amazonS3;

  private UserCommandServiceImpl userCommandService;

  private final Long validUserId = 1L;
  private final String validPassword = "eagles1234!";

  private UserCreateRequest createUser;
  private UserUpdateRequest updateUser;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    modelMapper = mock(ModelMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);
    amazonS3 = mock(AmazonS3.class);
    profile = mock(MultipartFile.class);

    userCommandService =
        new UserCommandServiceImpl(userRepository, modelMapper, passwordEncoder, amazonS3);

    createUser =
        UserCreateRequest.builder()
            .email("eagles@email.com")
            .password(validPassword)
            .userName("김이글")
            .phoneNumber("01012345678")
            .build();

    updateUser = UserUpdateRequest.builder().userName("김이글").phoneNumber("01088889999").build();
  }

  @Test
  @DisplayName("회원가입 정상 동작")
  void testUserRegister() throws Exception {
    User mockUser = mock(User.class);

    when(profile.isEmpty()).thenReturn(true);
    when(amazonS3.getUrl(any(), any())).thenReturn(new URL("https://dummy-url.com"));

    when(userRepository.findUserByEmail(createUser.email())).thenReturn(Optional.empty());
    when(userRepository.findUserByPhoneNumber(createUser.phoneNumber()))
        .thenReturn(Optional.empty());
    when(modelMapper.map(createUser, User.class)).thenReturn(mockUser);
    when(passwordEncoder.encode(createUser.password())).thenReturn("encodedPassword");

    userCommandService.userRegister(createUser, profile);

    verify(mockUser).setEncodedPassword("encodedPassword");
    verify(userRepository).save(mockUser);
  }

  @Test
  @DisplayName("비밀번호 검증 - 일치")
  void testValidUserPassword_True() {
    User mockUser = mock(User.class);
    when(mockUser.getPassword()).thenReturn("encodedPassword");
    when(userRepository.findUserByUserId(validUserId)).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches(validPassword, "encodedPassword")).thenReturn(true);

    Boolean result = userCommandService.validUserPassword(validUserId, validPassword);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("비밀번호 검증 - 불일치")
  void testValidUserPassword_False() {
    User mockUser = mock(User.class);
    when(mockUser.getPassword()).thenReturn("encodedPassword");
    when(userRepository.findUserByUserId(validUserId)).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    Boolean result = userCommandService.validUserPassword(validUserId, "wrongPassword");

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("비밀번호 변경 정상 동작")
  void testUpdateUserPassword() {
    User mockUser = mock(User.class);

    when(mockUser.getUserId()).thenReturn(validUserId);
    when(userRepository.findUserByUserId(validUserId)).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.encode(validPassword)).thenReturn("encodedNewPassword");
    when(userRepository.save(mockUser)).thenReturn(mockUser);

    UserDetailResponse response = userCommandService.updateUserPassword(validUserId, validPassword);

    verify(mockUser).setEncodedPassword("encodedNewPassword");
    assertThat(response.getUserId()).isEqualTo(validUserId);
  }

  @Test
  @DisplayName("회원 정보 조회 정상 동작")
  void testGetUserDetails() {
    User mockUser = mock(User.class);
    when(mockUser.getUserId()).thenReturn(validUserId);
    when(mockUser.getEmail()).thenReturn("eagles@email.com");
    when(mockUser.getPhoneNumber()).thenReturn("01012345678");
    when(mockUser.getUserName()).thenReturn("김이글");
    when(mockUser.getUserThumbnailUrl()).thenReturn(null);

    when(userRepository.findUserByUserId(validUserId)).thenReturn(Optional.of(mockUser));

    UserDetailResponse response = userCommandService.getUserDetails(validUserId);

    assertThat(response.getUserId()).isEqualTo(validUserId);
    assertThat(response.getEmail()).isEqualTo("eagles@email.com");
  }

  @Test
  @DisplayName("회원 정보 수정 정상 동작")
  void testUpdateUserDetails() throws Exception {
    User mockUser = mock(User.class);

    when(profile.isEmpty()).thenReturn(true);
    when(amazonS3.getUrl(any(), any())).thenReturn(new URL("https://dummy-url.com"));

    when(mockUser.getUserId()).thenReturn(validUserId);
    when(userRepository.findUserByUserId(validUserId)).thenReturn(Optional.of(mockUser));
    when(userRepository.save(mockUser)).thenReturn(mockUser);

    UserDetailResponse response =
        userCommandService.updateUserDetails(validUserId, updateUser, profile);

    verify(mockUser).modifyUserInfo(updateUser.userName(), updateUser.phoneNumber());
    assertThat(response.getUserId()).isEqualTo(validUserId);
  }

  @Test
  @DisplayName("회원 탈퇴 성공")
  void testWithdrawUserSuccess() {
    User mockUser = mock(User.class);
    when(userRepository.findUserByUserId(validUserId)).thenReturn(Optional.of(mockUser));

    userCommandService.withDrawUser(validUserId);

    verify(mockUser).deleteUser(any(LocalDateTime.class));
    verify(userRepository).save(mockUser);
  }
}
