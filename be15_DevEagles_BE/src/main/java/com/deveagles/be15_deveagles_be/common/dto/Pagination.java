package com.deveagles.be15_deveagles_be.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
  private int currentPage;
  private int totalPages;
  private long totalItems;
}
