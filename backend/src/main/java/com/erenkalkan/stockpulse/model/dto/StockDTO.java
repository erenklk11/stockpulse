package com.erenkalkan.stockpulse.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockDTO {

  private StockDataDTO data;
  private StockFinancialsDTO financials;
  private StockRecommendationsDTO recommendations;
}
