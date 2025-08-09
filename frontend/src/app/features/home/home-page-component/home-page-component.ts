import { Component } from '@angular/core';
import { HeaderComponent } from '../../../shared/header-component/header-component';
import { TickerTape } from '../../ticker-tape/ticker-tape';
import {SearchComponent} from '../../search/search-component';
import {DashboardComponent} from '../../dashboard/dashboard-component';

@Component({
  selector: 'app-home-page-component',
  imports: [HeaderComponent, TickerTape, SearchComponent, DashboardComponent],
  templateUrl: './home-page-component.html',
  styleUrl: './home-page-component.css'
})
export class HomePageComponent {

  getFirstName(): string {
    return sessionStorage.getItem("firstName") || "";
  }

}
