package com.deveagles.be15_deveagles_be.features.todolist.command.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class WorklogWrittenCheckResponse {
  private boolean written;
}
