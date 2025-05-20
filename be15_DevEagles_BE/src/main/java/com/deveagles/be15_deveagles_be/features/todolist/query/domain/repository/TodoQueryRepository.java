package com.deveagles.be15_deveagles_be.features.todolist.query.domain.repository;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TodoDetailResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoQueryRepository {
  TodoDetailResponse selectTodoDetail(@Param("todoId") Long todoId, @Param("userId") Long userId);
}
