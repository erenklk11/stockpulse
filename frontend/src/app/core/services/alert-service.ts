import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertDTO} from '../../features/stocks/alert-popup/model/alert';
import {environment} from '../../../environments/environments';
import {Observable} from 'rxjs';
import {Alert} from '../../features/watchlist/model/alert';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  constructor(private http: HttpClient) {}

  createAlert(alert: AlertDTO): any {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.alert.create,
      alert, {withCredentials: true});
  }

  deleteAlert(id: number): any {
    return this.http.delete<any>(environment.apiUrl + environment.endpoints.alert.delete +
      `/${id}`, {withCredentials: true});
  }

}
