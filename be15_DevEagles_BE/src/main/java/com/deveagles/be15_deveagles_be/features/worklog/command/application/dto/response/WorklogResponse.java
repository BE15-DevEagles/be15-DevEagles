package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorklogResponse {
    private String userName;
    private String summary;
    private LocalDateTime writtenAt;
}
