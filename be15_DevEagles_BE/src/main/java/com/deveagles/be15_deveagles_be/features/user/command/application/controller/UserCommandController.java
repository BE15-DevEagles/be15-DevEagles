package com.deveagles.be15_deveagles_be.features.user.command.application.controller;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.user.command.application.dto.request.UserCreateRequest;
import com.deveagles.be15_deveagles_be.features.user.command.application.service.UserCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
