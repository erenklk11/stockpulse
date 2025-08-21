import {Alert} from './alert';

export interface Watchlist {
  id: number;
  watchlistName: string;
  alerts: Alert[];
  alertCount: number;
}
