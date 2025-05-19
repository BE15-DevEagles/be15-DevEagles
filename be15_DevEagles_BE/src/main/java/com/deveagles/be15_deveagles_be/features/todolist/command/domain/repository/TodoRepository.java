package com.deveagles.be15_deveagles_be.features.todolist.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.todolist.command.domain.aggregate.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {}
