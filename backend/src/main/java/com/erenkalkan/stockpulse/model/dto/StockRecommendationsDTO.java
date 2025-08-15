package com.erenkalkan.stockpulse.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockRecommendationsDTO {

  private int strongBuy;
  private int buy;
  private int hold;
  private int sell;
  private int strongSell;

}
