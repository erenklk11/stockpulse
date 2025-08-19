import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {Watchlist} from './model/watchlist';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {WatchlistService} from '../../../core/services/watchlist-service';

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

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef,
              private watchlistService: WatchlistService) {}

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
    this.watchlistService.getAllWatchlists().subscribe({
      next: (watchlists) => {
        this.watchlists = watchlists ?? []; // fallback if null
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Error fetching watchlists:", err);
        this.watchlists = [];
      }
    });
  }

  createWatchlist(): void {
    this.watchlistService.createWatchlist(this.newWatchlistName).subscribe({
      next: (response: any) => {
        if (response) {
          const newWatchlist: Watchlist = response;
          newWatchlist.alertCount = 0;
          this.watchlists.push(newWatchlist);
          this.cdr.detectChanges();
        }
        return null;
      },
      error: (error: any) => {
        if (error.message) {
          console.error("Error creating watchlist: " + error.message());
        }
        else {
          console.error("Error creating watchlist");
        }
      }
    });
  }

  onWatchlistClick(watchlist: Watchlist): void {
    // TODO: Navigate to watchlist details or emit event
    console.log('Clicked watchlist:', watchlist);
  }

  deleteWatchlist(event: Event, watchlistId: number): void {

    event.stopPropagation();
    this.watchlists = this.watchlists.filter(w => w.id !== watchlistId);

    this.watchlistService.deleteWatchlist(event, watchlistId).subscribe({
      next: (response: any) => {
        if (response.deleted) {
          alert(`Deleted watchlist with ID: ${watchlistId}`);
        }
      },
      error: (error: any) => {
        if (error.message) {
          console.error("Error deleting watchlist: " + error.message());
        }
        else {
          console.error("Error deleting watchlist");
        }
      }
    });
  }

}
