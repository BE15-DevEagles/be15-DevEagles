package com.deveagles.be15_deveagles_be.features.user.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserCommandController {

  private final UserCommandService userCommandService;

  @PostMapping("/users")
  public ResponseEntity<ApiResponse<Void>> userRegister(
      @RequestBody @Valid UserCreateRequest request) {

    userCommandService.userRegister(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @GetMapping("/users/me")
  public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetails(
      @AuthenticationPrincipal CustomUser customUser) {

    UserDetailResponse response = userCommandService.getUserDetails(customUser.getUserId());

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }
}
