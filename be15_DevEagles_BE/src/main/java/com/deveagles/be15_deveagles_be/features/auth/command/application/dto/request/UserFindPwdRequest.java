package com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserFindPwdRequest(
    @NotBlank(message = "회원의 이름은 필수 입력값입니다.") String userName,
    @NotBlank @Email(message = "회원의 이메일은 필수 입력값입니다.") String email) {}
