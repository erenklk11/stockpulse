import { Component } from '@angular/core';
import { TickerTape } from '../ticker-tape/ticker-tape';
import {SearchComponent} from '../search/search-component';
import {DashboardComponent} from '../dashboard/dashboard-component';
import {WishlistComponent} from '../../wishlist/wishlist-component';
import {CommonModule} from '@angular/common';
import {NewsComponent} from '../../news/news-component';

@Component({
  selector: 'app-home-page-component',
  imports: [TickerTape, SearchComponent, DashboardComponent, WishlistComponent, CommonModule, NewsComponent],
  templateUrl: './home-page-component.html',
  styleUrl: './home-page-component.css'
})
export class HomePageComponent {

  dashboardSelected: boolean = true;

  selectDashboard(): void {
    this.dashboardSelected = true;
  }

  selectWishlist(): void {
    this.dashboardSelected = false;
  }

}
