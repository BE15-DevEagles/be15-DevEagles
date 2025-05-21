package com.deveagles.be15_deveagles_be.features.todolist.query.application.mapper;

import com.deveagles.be15_deveagles_be.features.todolist.query.application.dto.response.TeamFilteredTodoResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoTeamQueryMapper {

  List<TeamFilteredTodoResponse> selectTeamTodosByUser(
      @Param("teamId") Long teamId,
      @Param("userId") Long userId,
      @Param("offset") int offset,
      @Param("size") int size);

  int countTeamTodosByUser(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
