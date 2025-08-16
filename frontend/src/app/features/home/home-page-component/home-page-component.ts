import { Component } from '@angular/core';
import { TickerTape } from '../ticker-tape/ticker-tape';
import {SearchComponent} from '../search/search-component';
import {DashboardComponent} from '../dashboard/dashboard-component';
import {WishlistComponent} from '../../wishlist/wishlist-component';
import {CommonModule} from '@angular/common';
import {NewsComponent} from '../../news/news-component';
import {HeaderComponent} from '../../../shared/header-component/header-component';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-home-page-component',
  imports: [TickerTape, SearchComponent, DashboardComponent, WishlistComponent, CommonModule, NewsComponent, HeaderComponent, RouterOutlet],
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

  selectWishlist(): void {
    this.dashboardSelected = false;
  }

}
