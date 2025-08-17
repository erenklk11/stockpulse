import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {TickerTape} from '../ticker-tape/ticker-tape';
import {Stock} from './model/stock';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environments';
import {HeaderComponent} from '../../shared/header-component/header-component';
import {DescriptionComponent} from './description/description-component';
import {FinancialsComponent} from './financials/financials-component';
import {RecommendationsComponent} from './recommendations/recommendations-component';
import {NewsComponent} from '../news/news-component';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-stocks-component',
  standalone: true,
  imports: [
    TickerTape,
    HeaderComponent,
    DescriptionComponent,
    FinancialsComponent,
    RecommendationsComponent,
    NewsComponent,
    CommonModule
  ],
  templateUrl: './stocks-component.html',
  styleUrl: './stocks-component.css'
})
export class StocksComponent implements OnInit {

  stock!: Stock;
  stockTicker: string = '';
  selectedSection: string = 'description';

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  getFirstName(): string {
    return sessionStorage.getItem("firstName") || "";
  }

  ngOnInit(): void {

    this.stockTicker = new URLSearchParams(window.location.search).get('symbol') ?? '';
    if (this.stockTicker != null && this.stockTicker.trim() !== '') {

      this.http.get<any>(environment.apiUrl + environment.endpoints.api.stock + `?symbol=${this.stockTicker}`).subscribe({
        next: (response) => {

          if (response != null) {
            this.stock = response;
            this.cdr.detectChanges();
          }
          else {
            console.log("Stock is null");
          }
        },
        error: () => {
          console.error("Could not get stock data");
          return;
        }
      });
    }
  }

  selectDescription(): void {
    this.selectedSection = 'description';
  }

  selectFinancials(): void {
    this.selectedSection = 'financials';
  }

  selectRecommendations(): void {
    this.selectedSection = 'recommendations';
  }
}
