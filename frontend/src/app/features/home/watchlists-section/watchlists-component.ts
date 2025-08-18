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
    this.watchlists = this.watchlistService.getAllWatchlists();
    this.cdr.detectChanges();
  }

  toggleCreateForm(): void {
    this.isCreating = !this.isCreating;
    if (!this.isCreating) {
      this.newWatchlistName = '';
    }
  }

  createWatchlist(): void {
    this.watchlists.push(this.watchlistService.createWatchlist(this.newWatchlistName));
    this.cdr.detectChanges();
  }

  onWatchlistClick(watchlist: Watchlist): void {
    // TODO: Navigate to watchlist details or emit event
    console.log('Clicked watchlist:', watchlist);
  }

  deleteWatchlist(event: Event, watchlistId: number): void {
    this.watchlists = this.watchlists.filter(w => w.id !== watchlistId);
    this.watchlistService.deleteWatchlist(event, watchlistId);
  }

}
