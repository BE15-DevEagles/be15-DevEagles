package com.deveagles.be15_deveagles_be.features.user.command.application.service;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;

public interface UserCommandService {

  void userRegister(UserCreateRequest request);

  UserDetailResponse getUserDetails(Long userId);
}
