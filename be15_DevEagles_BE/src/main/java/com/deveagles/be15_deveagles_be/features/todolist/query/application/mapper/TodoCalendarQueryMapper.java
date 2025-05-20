package com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyCalendarTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TeamCalendarTodoResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoCalendarQueryMapper {
  List<MyCalendarTodoResponse> selectMyCalendarTodos(Long userId);

  List<TeamCalendarTodoResponse> selectTeamCalendarTodos(@Param("teamId") Long teamId);
}
