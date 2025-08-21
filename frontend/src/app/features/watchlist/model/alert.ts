import {TriggerType} from '../../stocks/alert-popup/model/trigger-type';
import {Stock} from './stock';

export interface Alert {
  id: number;
  stock: Stock;
  triggerType: TriggerType;
  isTriggered: boolean
  alertValue: number;
  targetValue: number;
  createdAt: string
  triggeredAt: string
}
