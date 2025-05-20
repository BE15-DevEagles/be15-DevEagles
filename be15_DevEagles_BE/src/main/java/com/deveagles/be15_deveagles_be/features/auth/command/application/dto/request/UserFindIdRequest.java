package com.deveagles.be15_deveagles_be.features.auth.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserFindIdRequest(
    @NotBlank(message = "회원의 이름은 필수 입력값입니다.") String userName,
    @NotBlank(message = "회원의 전화번호는 필수 입력값입니다.") String phoneNumber) {}
