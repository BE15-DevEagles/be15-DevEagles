package com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyDdayTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyTeamDdayTodoResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoDdayQueryMapper {
  List<MyDdayTodoResponse> selectMyIncompleteTodosWithDday(
      @Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

  int countMyIncompleteTodos(@Param("userId") Long userId);

  List<MyTeamDdayTodoResponse> selectMyTeamIncompleteTodosWithDday(
      @Param("userId") Long userId,
      @Param("teamId") Long teamId,
      @Param("offset") int offset,
      @Param("size") int size);

  int countMyTeamIncompleteTodos(@Param("userId") Long userId, @Param("teamId") Long teamId);
}
