package com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendAuthEmailRequest(@NotBlank(message = "사용자의 이메일 값은 필수입니다.") String email) {}
