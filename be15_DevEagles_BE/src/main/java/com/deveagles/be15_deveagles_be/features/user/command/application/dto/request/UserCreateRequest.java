package com.deveagles.be15_deveagles_be.features.user.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UserCreateRequest(
    @NotBlank(message = "아이디는 필수 입력값입니다.") String email,
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:\";'<>?,./]).{8,}$",
            message = "비밀번호는 영문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.")
        String password,
    @NotBlank(message = "이름은 필수 입력값입니다.") String userName,
    @NotBlank(message = "휴대 전화 번호는 필수 입력값입니다.") String phoneNumber) {}
