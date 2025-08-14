package com.erenkalkan.stockpulse.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockDataDTO {

  private String marketCap;
  private String exchange;
  private String industry;
}
