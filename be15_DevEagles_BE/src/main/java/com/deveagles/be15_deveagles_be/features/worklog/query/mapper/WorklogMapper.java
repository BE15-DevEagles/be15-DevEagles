package com.deveagles.be15_deveagles_be.features.worklog.query.mapper;

import com.deveagles.be15_deveagles_be.features.worklog.command.application.dto.response.WorklogResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorklogMapper {
  List<WorklogResponse> searchWorklogs(
      @Param("teamId") Long teamId,
      @Param("searchType") String searchType,
      @Param("keyword") String keyword,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("offset") int offset,
      @Param("size") int size);

  // 전체 개수 조회 (페이징용)
  int countWorklogs(
      @Param("teamId") Long teamId,
      @Param("searchType") String searchType,
      @Param("keyword") String keyword,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
