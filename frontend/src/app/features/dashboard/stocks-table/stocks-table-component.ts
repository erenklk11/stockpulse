import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {StocksWebsocketService} from '../../../core/services/stocks-websocket-service';
import {StockData} from './model/stock-data';
import {CommonModule} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {Subscription} from 'rxjs';
import {environment} from '../../../../environments/environments';

@Component({
  selector: 'app-stocks-table-component',
  imports: [CommonModule],
  templateUrl: './stocks-table-component.html',
  standalone: true,
  styleUrl: './stocks-table-component.css'
})
export class StocksTableComponent implements OnInit {

  constructor(private http: HttpClient,
              private cdr: ChangeDetectorRef) {

    this.big6StocksTicker.set('NVDA', { symbol: 'NVDA', name: 'NVIDIA'});
    this.big6StocksTicker.set('MSFT', { symbol: 'MSFT', name: 'Microsoft'});
    this.big6StocksTicker.set('AAPL', { symbol: 'AAPL', name: 'Apple'});
    this.big6StocksTicker.set('GOOGL', { symbol: 'GOOGL', name: 'Google'});
    this.big6StocksTicker.set('AMZN', { symbol: 'AMZN', name: 'Amazon'});
    this.big6StocksTicker.set('META', { symbol: 'META', name: 'Meta'});
  }

  big6StocksTicker: Map<string, StockData> = new Map();

  private subscriptions: Subscription[] = [];

  ngOnInit(): void {
    // Get stock data like industry and market cap
    this.getStockData();
  }

  get stocksArray(): StockData[] {
    return Array.from(this.big6StocksTicker.values());
  }

  getStockData(): any {

    for (let i = 0; i < this.stocksArray.length; i++) {

      const symbol = this.stocksArray[i].symbol;
      this.http.get<any>(environment.apiUrl + environment.endpoints.api.stockData + `?symbol=${symbol}`, {withCredentials: true}).subscribe({
        next: (response) => {
          this.stocksArray[i].industry = response.industry;
          this.stocksArray[i].exchange = response.exchange;
          this.stocksArray[i].marketCap = response.marketCap;

          this.cdr.detectChanges();
        },
        error: () => {
          console.error("Error fetching stock data")
        }
      });
    }
  }

}
