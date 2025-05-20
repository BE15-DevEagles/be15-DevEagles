package com.deveagles.be15_deveagles_be.features.user.command.application.service;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserUpdateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void userRegister(UserCreateRequest request) {

    Optional<User> duplEmail = userRepository.findUserByEmail(request.email());

    if (duplEmail.isPresent()) {
      throw new UserBusinessException(UserErrorCode.DUPLICATE_USER_EMAIL_EXCEPTION);
    }

    Optional<User> duplPhone = userRepository.findUserByPhoneNumber(request.phoneNumber());

    if (duplPhone.isPresent()) {
      throw new UserBusinessException(UserErrorCode.DUPLICATE_USER_PHONE_EXCEPTION);
    }

    User user = modelMapper.map(request, User.class);

    user.setEncodedPassword(passwordEncoder.encode(request.password()));

    userRepository.save(user);
  }

  @Override
  @Transactional
  public UserDetailResponse getUserDetails(Long userId) {

    User user = findUserByUserId(userId);
    return buildUserDetailResponse(user);
  }

  @Override
  @Transactional
  public UserDetailResponse updateUserDetails(
      Long userId, UserUpdateRequest request, MultipartFile profile) {

    User user = findUserByUserId(userId);

    user.modifyUserInfo(request.userName(), request.phoneNumber());

    // TODO : S3 연동 후 개발 필요
    /*
    if(!profile.isEmpty()) {
       user.modifyProfile(profile.getOriginalFilename());
     }
     */

    return buildUserDetailResponse(userRepository.save(user));
  }

  @Override
  @Transactional
  public Boolean validUserPassword(Long userId, String password) {

    User user = findUserByUserId(userId);

    if (!passwordEncoder.matches(password, user.getPassword())) return Boolean.FALSE;
    else return Boolean.TRUE;
  }

  @Override
  @Transactional
  public UserDetailResponse updateUserPassword(Long userId, String newPassword) {

    User user = findUserByUserId(userId);

    user.setEncodedPassword(passwordEncoder.encode(newPassword));

    return buildUserDetailResponse(userRepository.save(user));
  }

  @Override
  @Transactional
  public void withDrawUser(Long userId) {

    User user = findUserByUserId(userId);

    user.deleteUser(LocalDateTime.now());

    userRepository.save(user);
  }

  private User findUserByUserId(Long userId) {

    Optional<User> findUser = userRepository.findUserByUserId(userId);

    if (findUser.isEmpty()) {
      throw new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION);
    }

    return findUser.get();
  }

  private UserDetailResponse buildUserDetailResponse(User user) {

    return UserDetailResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .userName(user.getUserName())
        .thumbnailUrl(user.getUserThumbnailUrl())
        .build();
  }
}
