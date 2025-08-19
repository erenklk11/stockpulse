import {TriggerType} from './trigger-type';

export interface AlertDTO {
  symbol: string;
  triggerType: TriggerType;
  alertValue: number;
  watchlistId: number;
}
