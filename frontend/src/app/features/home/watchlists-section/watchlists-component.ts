import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {Watchlist} from './model/watchlist';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environments';

@Component({
  selector: 'app-watchlist-component',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './watchlists-component.html',
  styleUrl: './watchlists-component.css'
})
export class WatchlistsComponent implements OnInit {

  watchlists: Watchlist[] = [];
  newWatchlistName: string = '';
  isCreating: boolean = false;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
        this.getAllWatchlists();
    }

  toggleCreateForm(): void {
    this.isCreating = !this.isCreating;
    if (!this.isCreating) {
      this.newWatchlistName = '';
    }
  }

  getAllWatchlists(): void {
    this.http.get<any>(environment.apiUrl + environment.endpoints.watchlist.getAll,
      {withCredentials: true}).subscribe({
      next: (response) => {
        if (Array.isArray(response)) {
          this.watchlists = response;
          this.cdr.detectChanges();
        }
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

  createWatchlist(): void {

    if (this.newWatchlistName && this.newWatchlistName.trim()) {
      this.http.post<any>(environment.apiUrl + environment.endpoints.watchlist.create +
        '?watchlistName=' + this.newWatchlistName, {}, {withCredentials: true}).subscribe({
        next: (response) => {
          if (response) {
            const newWatchlist: Watchlist = response;
            newWatchlist.alertCount = 0;
            this.watchlists.push(newWatchlist);
            this.cdr.detectChanges();
          }
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

  onWatchlistClick(watchlist: Watchlist): void {
    // TODO: Navigate to watchlist details or emit event
    console.log('Clicked watchlist:', watchlist);
  }

  deleteWatchlist(event: Event, watchlistId: number): void {
    event.stopPropagation();
    this.watchlists = this.watchlists.filter(w => w.id !== watchlistId);

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
