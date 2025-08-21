import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {Watchlist} from './model/watchlist';
import {Alert} from './model/alert';
import {ActivatedRoute, Router} from '@angular/router';
import {WatchlistService} from '../../core/services/watchlist-service';
import {TriggerType} from '../stocks/alert-popup/model/trigger-type';
import {CommonModule} from '@angular/common';
import {AlertService} from '../../core/services/alert-service';
import {StocksService} from '../../core/services/stocks-service';
import {StocksWebsocketService} from '../../core/services/stocks-websocket-service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-watchlist-component',
  imports: [CommonModule],
  templateUrl: './watchlist-component.html',
  styleUrl: './watchlist-component.css'
})
export class WatchlistComponent implements OnInit {

  watchlist: Watchlist | null = null;
  alerts: Alert[] = [];
  isLoading: boolean = true;
  private subscriptions: Subscription[] = [];


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private watchlistService: WatchlistService,
    private alertService: AlertService,
    private stocksService: StocksService,
    private stocksWebsocketService: StocksWebsocketService
  ) {}

  ngOnInit(): void {
    const watchlistId = this.route.snapshot.paramMap.get('id');
    if (watchlistId) {
      this.loadWatchlist(+watchlistId);
    }
  }

  loadWatchlist(watchlistId: number): void {
    this.isLoading = true;

    this.watchlistService.getWatchlist(watchlistId).subscribe({
      next: (watchlist: Watchlist) => {
        this.watchlist = watchlist;
        this.loadAlerts(this.watchlist);
      },
      error: (err: any) => {
        console.error('Error loading watchlist:', err);
        this.router.navigate(['/home']);
      }
    });
  }

  loadAlerts(watchlist: Watchlist): void {

    this.alerts = watchlist.alerts || [];
    this.isLoading = false;

    // Getting last close price just in case the market is closed and the websocket connection doesn't receive any live prices
    this.setStockClosePrices();
    this.getLiveStockPrices();

    this.cdr.detectChanges();
  }

  setStockClosePrices(): void {
    this.alerts.forEach(alert => {
      this.stocksService.getStockClosePrice(alert.stock.symbol).subscribe({
        next: (response) => {
          if (response.price) {
            alert.stock.currentPrice = response.price;
            console.log(`CLOSE PRICE: ${response.price}`)
          }
          return 0;
        },
        error: (error) => {
          console.error(`Error fetching stock close price for: ${alert.stock.symbol}`);
        }
      });
    });
  }

  getLiveStockPrices(): void {
    this.stocksWebsocketService.connect('ws://localhost:8080/live-prices');

    const connectionSub = this.stocksWebsocketService.connectionState$.subscribe(state => {
      console.log('Connection state:', state);

      if (state === 'CONNECTED') {
        const symbols: string[] = [];
        this.alerts.forEach(a => symbols.push(a.stock.symbol));
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
    this.alerts.forEach(a => {
      if (a.stock.symbol === data.symbol) {
        a.stock.currentPrice = data.price;
      }
    });
    this.cdr.detectChanges();
  }

  deleteAlert(event: Event, alertId: number): void {
    event.stopPropagation();

    if (!confirm('Are you sure you want to delete this alert?')) {
      return;
    }

    this.alertService.deleteAlert(alertId).subscribe({
      next: (response: any) => {
        if (response.deleted) {
          alert(`Alert has been deleted`);
          this.alerts.filter(a => a.id !== alertId);
        }
      },
      error: (error: any) => {
        console.error('Error deleting alert:', error);
      }
    });
  }

  calculateProgress(targetValue: number, currentStockPrice: number): number {
    const absDifference = Math.abs(targetValue - currentStockPrice);
    return 100 - (absDifference / targetValue) * 100;
  }

  goBack(): void {
    this.router.navigate(['/home']);
  }

  getAlertStatusLabel(alert: Alert):string {
    return alert.isTriggered ? 'Triggered' : 'Active';
  }

  getAlertStatusClass(alert: Alert): string {
    return alert.isTriggered ? 'triggered' : 'active';
  }

  getTriggeredCount(): number {
    return this.alerts.filter(a => a.isTriggered).length;
  }

  getNotTriggeredCount(): number {
    return this.alerts.filter(a => !a.isTriggered).length;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  protected readonly TriggerType = TriggerType;
}
