package com.deveagles.be15_deveagles_be.features.user.command.application.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserUpdateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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
  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Override
  @Transactional
  public void userRegister(UserCreateRequest request, MultipartFile profile) {

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

    if (!profile.isEmpty()) {
      String profileUrl = saveProfile(profile);
      user.setProfile(profileUrl);
    }
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

    if (!profile.isEmpty()) {
      String profileUrl = saveProfile(profile);
      user.setProfile(profileUrl);
    }

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

  private String saveProfile(MultipartFile profile) {

    String fileName = "user/thumbnail_" + UUID.randomUUID() + "_" + profile.getOriginalFilename();

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(profile.getSize());
    metadata.setContentType(profile.getContentType());

    try {
      amazonS3.putObject(bucket, fileName, profile.getInputStream(), metadata);
    } catch (IOException e) {
      throw new UserBusinessException(UserErrorCode.PROFILE_SAVE_ERROR);
    }

    return amazonS3.getUrl(bucket, fileName).toString();
  }
}
