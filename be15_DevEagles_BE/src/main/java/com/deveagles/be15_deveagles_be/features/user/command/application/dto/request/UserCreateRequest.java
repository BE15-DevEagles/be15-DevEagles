package com.deveagles.be15_deveagles_be.features.user.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserCreateRequest(
    @NotBlank(message = "아이디는 필수 입력값입니다.") String email,
    @NotBlank(message = "비밀번호는 필수 입력값입니다.") String password,
    @NotBlank(message = "이름은 필수 입력값입니다.") String userName,
    @NotBlank(message = "휴대 전화 번호는 필수 입력값입니다.") String phoneNumber) {}
