import { Component, AfterViewInit } from '@angular/core';
import {StocksTableComponent} from './stocks-table/stocks-table-component';

@Component({
  selector: 'app-dashboard-component',
  imports: [StocksTableComponent],
  templateUrl: './dashboard-component.html',
  standalone: true,
  styleUrl: './dashboard-component.css'
})
export class DashboardComponent implements AfterViewInit {

  selectedStock: string = 'NASDAQ:NVDA';

  ngAfterViewInit(): void {
    // Add a small delay to avoid conflicts with ticker-tape widget
    setTimeout(() => {
      this.loadTradingViewWidget();
    }, 500);
  }

  private loadTradingViewWidget(): void {
    // Clear any existing widget first
    this.clearTradingViewWidget();

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = 'https://s3.tradingview.com/external-embedding/embed-widget-mini-symbol-overview.js';
    script.async = true;

    // Set the configuration as script content (this is how TradingView expects it)
    script.textContent = JSON.stringify({
      "symbol": `${this.selectedStock}`,
      "chartOnly": false,
      "dateRange": "12M",
      "noTimeScale": false,
      "colorTheme": "light",
      "isTransparent": false,
      "locale": "en",
      "width": "100%",
      "autosize": false,
      "height": "100%"
    });

    const widgetContainer = document.querySelector('.tradingview-chart-widget-container__widget');
    if (widgetContainer) {
      widgetContainer.appendChild(script);
    }
  }

  private clearTradingViewWidget(): void {
    const widgetContainer = document.querySelector('.tradingview-chart-widget-container__widget');
    if (widgetContainer) {
      widgetContainer.innerHTML = '';
    }
  }

  selectNvidia(): void {
    this.selectedStock = 'NASDAQ:NVDA';
    this.loadTradingViewWidget();
  }

  selectMicrosoft(): void {
    this.selectedStock = 'NASDAQ:MSFT';
    this.loadTradingViewWidget();
  }

  selectApple(): void {
    this.selectedStock = 'NASDAQ:AAPL';
    this.loadTradingViewWidget();
  }

  selectGoogle(): void {
    this.selectedStock = 'NASDAQ:GOOGL';
    this.loadTradingViewWidget();
  }

  selectAmazon(): void {
    this.selectedStock = 'NASDAQ:AMZN';
    this.loadTradingViewWidget();
  }

  selectMeta(): void {
    this.selectedStock = 'NASDAQ:META';
    this.loadTradingViewWidget();
  }


}
