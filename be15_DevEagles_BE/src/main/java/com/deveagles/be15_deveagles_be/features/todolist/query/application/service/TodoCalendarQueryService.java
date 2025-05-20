package com.deveagles.be15_deveagles_be.features.todolist.query.application.service;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyCalendarTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TeamCalendarTodoResponse;
import com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper.TodoCalendarQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TodoCalendarQueryService {

  private final TodoCalendarQueryMapper todoCalendarQueryMapper;

  public TodoCalendarQueryService(TodoCalendarQueryMapper todoCalendarQueryMapper) {
    this.todoCalendarQueryMapper = todoCalendarQueryMapper;
  }

  public List<MyCalendarTodoResponse> getMyCalendarTodos(Long userId) {
    return todoCalendarQueryMapper.selectMyCalendarTodos(userId);
  }

  public List<TeamCalendarTodoResponse> getTeamCalendarTodos(Long teamId) {
    return todoCalendarQueryMapper.selectTeamCalendarTodos(teamId);
  }
}
