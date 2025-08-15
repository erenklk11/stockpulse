export interface StockFinancialsDTO {
  // Balance Sheet
  assets: number;
  liabilities: number;
  equity: number;
  currentAssets: number;
  currentLiabilities: number;

  // Income Statement
  revenues: number;
  grossProfit: number;
  operatingIncome: number;
  netIncome: number;
  basicEarningsPerShare: number;
  dilutedEarningsPerShare: number;

  // Cash Flow Statement
  netCashFlowFromOperatingActivities: number;
  netCashFlowFromInvestingActivities: number;
  netCashFlowFromFinancingActivities: number;
  netCashFlow: number;
}
