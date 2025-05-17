package com.deveagles.be15_deveagles_be.common.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PagedResult<T> {
  private final List<T> content;
  private final Pagination pagination;

  public PagedResult(List<T> content, Pagination pagination) {
    this.content = content;
    this.pagination = pagination;
  }

  public static <T> PagedResult<T> from(Page<T> page) {
    Pagination pagination =
        Pagination.builder()
            .currentPage(page.getNumber())
            .totalPages(page.getTotalPages())
            .totalItems(page.getTotalElements())
            .build();

    return new PagedResult<>(page.getContent(), pagination);
  }
}
