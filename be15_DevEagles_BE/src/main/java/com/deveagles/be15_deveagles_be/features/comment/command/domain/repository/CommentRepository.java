package com.deveagles.be15_deveagles_be.features.comment.command.domain.repository;

import com.deveagles.be15_deveagles_be.features.comment.command.domain.aggregate.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {}
