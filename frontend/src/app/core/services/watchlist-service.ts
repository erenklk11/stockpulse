import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {WatchlistDTO} from '../../features/home/watchlists-section/model/watchlist';
import {Observable} from 'rxjs';
import {Watchlist} from '../../features/watchlist/model/watchlist';

@Injectable({
  providedIn: 'root'
})
export class WatchlistService {

  constructor(private http: HttpClient) {}

  getWatchlist(id: number): Observable<Watchlist> {
    return this.http.get<any>(environment.apiUrl + environment.endpoints.watchlist.get +
      `/${id}`, {
      withCredentials: true
    });
  }

  getAllWatchlists(): Observable<WatchlistDTO[]> {
    return this.http.get<WatchlistDTO[]>(environment.apiUrl + environment.endpoints.watchlist.getAll, {
      withCredentials: true
    });
  }

  createWatchlist(newWatchlistName: string): any {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.watchlist.create +
      '?watchlistName=' + newWatchlistName, {}, {withCredentials: true});
  }

  deleteWatchlist(watchlistId: number): any {
    return this.http.delete<any>(environment.apiUrl + environment.endpoints.watchlist.delete +
      `/${watchlistId}`, {withCredentials: true});
  }

}
