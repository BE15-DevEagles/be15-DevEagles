package com.deveagles.be15_deveagles_be.features.user.command.application.dto.request;

import lombok.Builder;

@Builder
public record UserDuplRequest(String email, String phoneNumber) {}
