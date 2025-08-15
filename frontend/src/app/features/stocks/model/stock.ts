import {StockDataDTO} from './stock-data';
import {StockFinancialsDTO} from './stock-financials';
import {StockRecommendationsDTO} from './stock-recommendations';

export interface Stock {
  data: StockDataDTO;
  financials: StockFinancialsDTO;
  recommendations: StockRecommendationsDTO;
}
