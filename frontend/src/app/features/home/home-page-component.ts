import { Component } from '@angular/core';
import { TickerTape } from '../ticker-tape/ticker-tape';
import {SearchComponent} from './search/search-component';
import {DashboardComponent} from './dashboard/dashboard-component';
import {WatchlistComponent} from '../watchlist/watchlist-component';
import {CommonModule} from '@angular/common';
import {NewsComponent} from '../news/news-component';
import {HeaderComponent} from '../../shared/header-component/header-component';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-home-page-component',
  standalone: true,
  imports: [TickerTape, SearchComponent, DashboardComponent, WatchlistComponent, CommonModule, NewsComponent, HeaderComponent],
  templateUrl: './home-page-component.html',
  styleUrl: './home-page-component.css'
})
export class HomePageComponent {

  dashboardSelected: boolean = true;

  getFirstName(): string {
    return sessionStorage.getItem("firstName") || "";
  }

  selectDashboard(): void {
    this.dashboardSelected = true;
  }

  selectWatchlist(): void {
    this.dashboardSelected = false;
  }

}
