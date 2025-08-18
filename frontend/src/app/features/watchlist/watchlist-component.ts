import {ChangeDetectorRef, Component} from '@angular/core';
import {Watchlist} from './model/watchlist';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environments';

@Component({
  selector: 'app-watchlist-component',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './watchlist-component.html',
  styleUrl: './watchlist-component.css'
})
export class WatchlistComponent {

  watchlists: Watchlist[] = [];
  newWatchlistName: string = '';
  isCreating: boolean = false;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  toggleCreateForm(): void {
    this.isCreating = !this.isCreating;
    if (!this.isCreating) {
      this.newWatchlistName = '';
    }
  }

  createWatchlist(): void {

    if (this.newWatchlistName && this.newWatchlistName.trim()) {
      this.http.post<any>(environment.apiUrl + environment.endpoints.watchlist.create +
        '?watchlistName=' + this.newWatchlistName, {}, {withCredentials: true}).subscribe({
        next: (response) => {
          if (response) {
            const newWatchlist: Watchlist = response;
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
