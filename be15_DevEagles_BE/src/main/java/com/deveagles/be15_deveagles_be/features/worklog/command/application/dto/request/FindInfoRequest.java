package com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.request;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.aggregate.SearchType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class FindInfoRequest {
  private int page = 1;
  private int size = 10;
  private Long teamId;
  private SearchType searchType; // ALL, AUTHOR, KEYWORD, DATE
  private String keyword; // 검색 키워드 (작성자 or 내용 관련)

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startDate; // 날짜 검색 시작 (DATE 타입에서 사용)

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime endDate;
}
