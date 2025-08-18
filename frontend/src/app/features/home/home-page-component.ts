import { Component } from '@angular/core';
import { TickerTape } from '../../shared/ticker-tape/ticker-tape';
import {SearchComponent} from './search/search-component';
import {DashboardComponent} from './dashboard/dashboard-component';
import {WatchlistsComponent} from './watchlists-section/watchlists-component';
import {CommonModule} from '@angular/common';
import {NewsComponent} from '../news/news-component';
import {HeaderComponent} from '../../shared/header-component/header-component';

@Component({
  selector: 'app-home-page-component',
  standalone: true,
  imports: [TickerTape, SearchComponent, DashboardComponent, WatchlistsComponent, CommonModule, NewsComponent, HeaderComponent],
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
