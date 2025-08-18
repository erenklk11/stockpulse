import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environments';
import {Watchlist} from '../../features/home/watchlists-section/model/watchlist';

@Injectable({
  providedIn: 'root'
})
export class WatchlistService {

  constructor(private http: HttpClient) {}

  getAllWatchlists(): any {
    this.http.get<any>(environment.apiUrl + environment.endpoints.watchlist.getAll,
      {withCredentials: true}).subscribe({
      next: (response) => {
        if (response) {
          return response;
        }
          return null;
      },
      error: (error) => {
        if (error.message()) {
          console.error("Could not get wishlists: " + error.message());
        }
        else {
          console.error("Could not get wishlists");
        }
      }
    });
  }

  createWatchlist(newWatchlistName: string): any {

    if (newWatchlistName && newWatchlistName.trim()) {
      this.http.post<any>(environment.apiUrl + environment.endpoints.watchlist.create +
        '?watchlistName=' + newWatchlistName, {}, {withCredentials: true}).subscribe({
        next: (response) => {
          if (response) {
            const newWatchlist: Watchlist = response;
            newWatchlist.alertCount = 0;
            return newWatchlist;
          }
          return null;
        },
        error: (error) => {
          if (error.message) {
            console.error("Error creating watchlist: " + error.message());
          }
          else {
            console.error("Error creating watchlist");
          }
        }
      });
    }
  }

  deleteWatchlist(event: Event, watchlistId: number): void {
    event.stopPropagation();

    this.http.delete<any>(environment.apiUrl + environment.endpoints.watchlist.delete +
      '?id=' + watchlistId, {withCredentials: true}).subscribe({
      next: (response) => {
        if (response.deleted) {}
      },
      error: (error) => {
        if (error.message) {
          console.error("Error deleting watchlist: " + error.message());
        }
        else {
          console.error("Error deleting watchlist");
        }
      }
    });
    console.log('Deleted watchlist with ID:', watchlistId);
  }

}
