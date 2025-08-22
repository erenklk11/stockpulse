import {TriggerType} from './trigger-type';
import {Stock} from './stock';

export interface AlertDTO {
  stock: Stock;
  triggerType: TriggerType;
  percentageValue?: number;
  targetValue: number;
  watchlistId: number;
}
