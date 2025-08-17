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
import {StocksWebsocketService} from '../../core/services/stocks-websocket-service';
import {Subscription} from 'rxjs';

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
  stockPrice: number = 0;
  selectedSection: string = 'description';
  private subscriptions: Subscription[] = [];

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef,
              private stocksWebsocketService: StocksWebsocketService) {}

  getFirstName(): string {
    return sessionStorage.getItem("firstName") || "";
  }

  ngOnInit(): void {

    this.getStockData();
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

  getLiveStockPrice(): void {
    this.stocksWebsocketService.connect('ws://localhost:8080/live-prices');

    const connectionSub = this.stocksWebsocketService.connectionState$.subscribe(state => {
      console.log('Connection state:', state);

      if (state === 'CONNECTED') {
        const symbol = this.stockTicker;
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
}
