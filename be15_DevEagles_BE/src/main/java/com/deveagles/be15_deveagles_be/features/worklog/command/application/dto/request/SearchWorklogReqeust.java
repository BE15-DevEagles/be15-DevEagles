package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchWorklogReqeust {
    private Long teamId;
    private int page;
    private int size;
}
