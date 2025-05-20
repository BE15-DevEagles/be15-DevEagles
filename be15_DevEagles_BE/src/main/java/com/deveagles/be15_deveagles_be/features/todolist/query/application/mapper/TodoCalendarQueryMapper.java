package com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.MyCalendarTodoResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoCalendarQueryMapper {
  List<MyCalendarTodoResponse> selectMyCalendarTodos(Long userId);
}
