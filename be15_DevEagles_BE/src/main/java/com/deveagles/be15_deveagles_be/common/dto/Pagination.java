package com.deveagles.be15_deveagles_be.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Pagination {
  private final int currentPage;
  private final int totalPages;
  private final long totalItems;
}
