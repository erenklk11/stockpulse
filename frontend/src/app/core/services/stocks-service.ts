import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environments';

@Injectable({
  providedIn: 'root'
})
export class StocksService {

  constructor(private http: HttpClient) {}

  getStockClosePrice(symbol: string): Observable<any> {
    return this.http.get<any>(environment.apiUrl + environment.endpoints.api.stockClosePrice +
    `?symbol=${symbol}`, {withCredentials: true});
  }

}
