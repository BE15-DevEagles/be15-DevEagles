package com.deveagles.be15_deveagles_be.features.user.command.application.service;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserUpdateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface UserCommandService {

  void userRegister(UserCreateRequest request);

  UserDetailResponse getUserDetails(Long userId);

  UserDetailResponse updateUserDetails(
      Long userId, @Valid UserUpdateRequest request, MultipartFile profile);

  Boolean validUserPassword(Long userId, String password);

  UserDetailResponse updateUserPassword(Long userId, String newPassword);

  void withDrawUser(Long userId);
}
