package com.deveagles.be15_deveagles_be.features.user.command.application.service;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserBusinessException;
import com.deveagles.be15_deveagles_be.features.user.command.domain.exception.UserErrorCode;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    Optional<User> findUser = userRepository.findUserByUserId(userId);

    if (findUser.isEmpty()) {
      throw new UserBusinessException(UserErrorCode.NOT_FOUND_USER_EXCEPTION);
    }

    User user = findUser.get();

    return UserDetailResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .userName(user.getUserName())
        .thumbnailUrl(user.getUserThumbnailUrl())
        .build();
  }
}
