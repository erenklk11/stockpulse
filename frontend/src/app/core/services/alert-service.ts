import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertDTO} from '../../features/stocks/alert-popup/model/alert';
import {environment} from '../../../environments/environments';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  constructor(private http: HttpClient) {}

  createAlert(alert: AlertDTO): any {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.alert.create,
      alert, {withCredentials: true});
  }

  deleteAlert(alertId: number): any {
    return this.http.delete<any>(environment.apiUrl + environment.endpoints.alert.delete +
      `/${alertId}`, {withCredentials: true});
  }

}
