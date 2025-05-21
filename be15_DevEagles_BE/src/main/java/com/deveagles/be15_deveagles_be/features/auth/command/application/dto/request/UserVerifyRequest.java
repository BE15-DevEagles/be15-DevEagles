package com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserVerifyRequest(
    @Email @NotBlank(message = "이메일이 전달되지 않았습니다.") String email,
    @NotBlank(message = "인증코드가 전달되지 않았습니다.") String authCode) {}
