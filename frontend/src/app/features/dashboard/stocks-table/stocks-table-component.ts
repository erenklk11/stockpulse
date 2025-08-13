import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {StocksWebsocketService} from '../../../core/services/stocks-websocket-service';
import {StockData} from './model/stock-data';
import {CommonModule} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-stocks-table-component',
  imports: [CommonModule],
  templateUrl: './stocks-table-component.html',
  standalone: true,
  styleUrl: './stocks-table-component.css'
})
export class StocksTableComponent implements OnInit{

  constructor(private stocksWebsocketService: StocksWebsocketService,
              private http: HttpClient,
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
    this.stocksWebsocketService.connect('ws://localhost:8080/live-prices');

    const connectionSub = this.stocksWebsocketService.connectionState$.subscribe(state => {
      console.log('Connection state:', state);

      if (state === 'CONNECTED') {
        const symbols = Array.from(this.big6StocksTicker.keys());
        this.stocksWebsocketService.subscribeToSymbols(symbols);
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
    const stock = this.big6StocksTicker.get(data.symbol);
    if (stock) {
      stock.price = data.price;
      this.big6StocksTicker.set(data.symbol, stock);
      this.cdr.detectChanges();
    }
  }

  get stocksArray(): StockData[] {
    return Array.from(this.big6StocksTicker.values());
  }

  // Method to manually subscribe to additional symbols
  addStockSymbol(symbol: string, name: string): void {
    if (!this.big6StocksTicker.has(symbol)) {
      this.big6StocksTicker.set(symbol, { symbol, name });

      if (this.stocksWebsocketService.isConnected()) {
        this.stocksWebsocketService.subscribeToSymbol(symbol);
      }
    }
  }

}
