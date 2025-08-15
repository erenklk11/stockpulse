package com.erenkalkan.stockpulse.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockFinancialsDTO {

  // Balance Sheet
  private long assets;
  private long liabilities;
  private long equity;
  private long currentAssets;
  private long currentLiabilities;

  // Income Statement
  private long revenues;
  private long grossProfit;
  private long operatingIncome;
  private long netIncome;
  private double basicEarningsPerShare;
  private double dilutedEarningsPerShare;

  // Cash Flow Statement
  private long netCashFlowFromOperatingActivities;
  private long netCashFlowFromInvestingActivities;
  private long netCashFlowFromFinancingActivities;
  private long netCashFlow;
}
