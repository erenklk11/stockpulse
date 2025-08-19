import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environments';
import {Watchlist} from '../../features/home/watchlists-section/model/watchlist';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WatchlistService {

  constructor(private http: HttpClient) {}

  getAllWatchlists(): Observable<Watchlist[]> {
    return this.http.get<Watchlist[]>(environment.apiUrl + environment.endpoints.watchlist.getAll, {
      withCredentials: true
    });
  }

  createWatchlist(newWatchlistName: string): any {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.watchlist.create +
      '?watchlistName=' + newWatchlistName, {}, {withCredentials: true});
  }

  deleteWatchlist(event: Event, watchlistId: number): any {
    return this.http.delete<any>(environment.apiUrl + environment.endpoints.watchlist.delete +
      '?id=' + watchlistId, {withCredentials: true});
  }

}
