package com.deveagles.be15_deveagles_be.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {
  private List<T> content;
  private Pagination pagination;

  public static <T> PagedResponse<T> from(PagedResult<T> pagedResult) {
    return new PagedResponse<>(pagedResult.getContent(), pagedResult.getPagination());
  }
}
