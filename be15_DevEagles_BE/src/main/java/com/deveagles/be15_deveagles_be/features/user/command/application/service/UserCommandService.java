package com.deveagles.be15_deveagles_be.features.user.command.application.service;

import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;

public interface UserCommandService {

  void userRegister(UserCreateRequest request);
}
