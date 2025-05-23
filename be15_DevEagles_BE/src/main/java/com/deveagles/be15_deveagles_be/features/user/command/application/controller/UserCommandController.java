package com.deveagles.be15_deveagles_be.features.user.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.auth.command.application.model.CustomUser;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserDuplRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserPasswordRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserUpdateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.response.UserDetailResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserCommandController {

  private final UserCommandService userCommandService;
  private final UserRepository userRepository;

  @PostMapping("/users")
  public ResponseEntity<ApiResponse<Void>> userRegister(
      @RequestPart @Valid UserCreateRequest request,
      @RequestPart(required = false) MultipartFile profile) {

    userCommandService.userRegister(request, profile);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @PostMapping("/users/duplcheck")
  public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestBody UserDuplRequest request) {

    Optional<User> user = Optional.empty();
    if (request.email() != null) user = userRepository.findUserByEmail(request.email());
    else if (request.phoneNumber() != null)
      user = userRepository.findUserByPhoneNumber(request.phoneNumber());

    Boolean checked;

    if (user.isEmpty()) checked = Boolean.TRUE;
    else checked = Boolean.FALSE;

    return ResponseEntity.ok().body(ApiResponse.success(checked));
  }

  @GetMapping("/users/me")
  public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetails(
      @AuthenticationPrincipal CustomUser customUser) {

    UserDetailResponse response = userCommandService.getUserDetails(customUser.getUserId());

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @PostMapping("/users/valid")
  public ResponseEntity<ApiResponse<Boolean>> validUserPassword(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestBody @Valid UserPasswordRequest request) {

    Boolean is_Valid =
        userCommandService.validUserPassword(customUser.getUserId(), request.password());

    return ResponseEntity.ok().body(ApiResponse.success(is_Valid));
  }

  @PatchMapping("/users/mod")
  public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserDetails(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestPart @Valid UserUpdateRequest request,
      @RequestPart(required = false) MultipartFile profile) {
    UserDetailResponse response =
        userCommandService.updateUserDetails(customUser.getUserId(), request, profile);

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @PatchMapping("/users/mod/pwd")
  public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserPassword(
      @AuthenticationPrincipal CustomUser customUser,
      @RequestBody @Valid UserPasswordRequest request) {

    UserDetailResponse response =
        userCommandService.updateUserPassword(customUser.getUserId(), request.password());

    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @DeleteMapping("/users")
  public ResponseEntity<ApiResponse<Void>> withDrawUser(
      @AuthenticationPrincipal CustomUser customUser) {

    userCommandService.withDrawUser(customUser.getUserId());

    return ResponseEntity.ok().body(ApiResponse.success(null));
  }
}
