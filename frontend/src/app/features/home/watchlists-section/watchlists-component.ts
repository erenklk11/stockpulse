import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {WatchlistDTO} from './model/watchlist';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {WatchlistService} from '../../../core/services/watchlist-service';
import {Router} from '@angular/router';

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

  watchlists: WatchlistDTO[] = [];
  newWatchlistName: string = '';
  isCreating: boolean = false;

  constructor(private cdr: ChangeDetectorRef,
              private watchlistService: WatchlistService,
              private router: Router) {}

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
          const newWatchlist: WatchlistDTO = response;
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

  onWatchlistClick(watchlist: WatchlistDTO): void {
    this.router.navigate([`/watchlist/${watchlist.id}`]);
    console.log('Clicked watchlist:', watchlist);
  }

  deleteWatchlist(event: Event, watchlistId: number): void {

    event.stopPropagation();
    this.watchlists = this.watchlists.filter(w => w.id !== watchlistId);

    this.watchlistService.deleteWatchlist(watchlistId).subscribe({
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
