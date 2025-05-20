package com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
    @NotBlank(message = "아이디는 필수 입력값입니다.") String username,
    @NotBlank(message = "비밀번호는 필수 입력값입니다.") String password) {}
