import {Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy} from '@angular/core';
import {TickerTape} from '../../shared/ticker-tape/ticker-tape';
import {Stock} from './model/stock';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environments';
import {HeaderComponent} from '../../shared/header-component/header-component';
import {DescriptionComponent} from './description/description-component';
import {FinancialsComponent} from './financials/financials-component';
import {RecommendationsComponent} from './recommendations/recommendations-component';
import {NewsComponent} from '../news/news-component';
import {CommonModule} from '@angular/common';
import {StocksWebsocketService} from '../../core/services/stocks-websocket-service';
import {Subscription} from 'rxjs';
import {AlertPopupComponent} from './alert-popup/alert-popup-component';
import {StocksService} from '../../core/services/stocks-service';

@Component({
  selector: 'app-stocks-component',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    TickerTape,
    HeaderComponent,
    DescriptionComponent,
    FinancialsComponent,
    RecommendationsComponent,
    NewsComponent,
    CommonModule,
    AlertPopupComponent
  ],
  templateUrl: './stocks-component.html',
  styleUrl: './stocks-component.css'
})
export class StocksComponent implements OnInit {

  stock!: Stock;
  symbol: string = '';
  stockPrice: number = 0;
  isLoading: boolean = false;
  showWatchlistPopup: boolean = false;
  selectedSection: string = 'description';
  private subscriptions: Subscription[] = [];

  constructor(private http: HttpClient,
              private cdr: ChangeDetectorRef,
              private stocksWebsocketService: StocksWebsocketService,
              private stocksService: StocksService) {}

  getFirstName(): string {
    return sessionStorage.getItem("firstName") || "";
  }

  ngOnInit(): void {

    this.getStockData();
    // Getting last close price just in case the market is closed and the websocket connection doesn't receive any live prices
    this.getStockClosePrice(this.symbol);
    this.getLiveStockPrice();
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

  getStockData(): void {

    this.symbol = new URLSearchParams(window.location.search).get('symbol') ?? '';
    if (this.symbol != null && this.symbol.trim() !== '') {
      this.isLoading = true;
      this.http.get<any>(environment.apiUrl + environment.endpoints.api.stock + `?symbol=${this.symbol}`,
        {withCredentials: true}).subscribe({
        next: (response) => {

          if (response != null) {
            this.stock = response;
            this.isLoading = false;
            this.cdr.detectChanges();
          }
          else {
            console.log("Stock is null");
          }
        },
        error: (error) => {
          if (error.message()) {
            console.error("Could not get stock data: " + error.message());
          }
          else {
            console.error("Could not get stock data");
          }
        }
      });
    }
  }

  getStockClosePrice(symbol: string): void {
    this.stocksService.getStockClosePrice(symbol).subscribe({
      next: (response) => {
        if (response.price) {
          this.stockPrice = response.price;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error(`Error fetching stock close price for: ${this.symbol}`);
      }
    });
  }

  getLiveStockPrice(): void {
    this.stocksWebsocketService.connect('ws://localhost:8080/live-prices');

    const connectionSub = this.stocksWebsocketService.connectionState$.subscribe(state => {
      console.log('Connection state:', state);

      if (state === 'CONNECTED') {
        const symbol = this.symbol;
        this.stocksWebsocketService.subscribeToSymbol(symbol);
      }
    });
    this.subscriptions.push(connectionSub);

    const messagesSub = this.stocksWebsocketService.messages$.subscribe(data => {
      console.log('Received data:', data);
      this.handleWebSocketMessage(data);
    });
    this.subscriptions.push(messagesSub);

    // Optional: Set up ping interval to keep connection alive
    const pingInterval = setInterval(() => {
      if (this.stocksWebsocketService.isConnected()) {
        this.stocksWebsocketService.sendPing();
      }
    }, 30000); // Ping every 30 seconds

    // Store interval reference for cleanup
    this.subscriptions.push(new Subscription(() => clearInterval(pingInterval)));
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.stocksWebsocketService.disconnect();
  }

  private handleWebSocketMessage(data: any): void {
    switch (data.type) {
      case 'connected':
        console.log('Connection established:', data.message);
        break;

      case 'subscribed':
        console.log('Successfully subscribed to:', data.symbols);
        break;

      case 'price_update':
        this.updateStockPrice(data.data);
        break;

      case 'pong':
        console.log('Received pong at:', new Date(data.timestamp));
        break;

      case 'error':
        console.error('WebSocket error:', data.message);
        break;

      default:
        console.log('Unknown message type:', data);
    }
  }

  updateStockPrice(data: any) {
      this.stockPrice = data.price;
      this.cdr.detectChanges();
  }

  openWatchlistsSection(): void {
    this.showWatchlistPopup = true;
  }

  closeWatchlistPopup(): void {
    this.showWatchlistPopup = false;
  }

  onWatchlistSelected(watchlist: any): void {
    console.log('Stock added to watchlist:', watchlist);
    alert(this.stock.data.name + " added to watchlist: " + watchlist);
    this.showWatchlistPopup = false;
  }
}
