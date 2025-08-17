import { Component, AfterViewInit } from '@angular/core';

@Component({
  selector: 'app-ticker-tape',
  imports: [],
  templateUrl: './ticker-tape.html',
  styleUrl: './ticker-tape.css'
})
export class TickerTape implements AfterViewInit {

  ngAfterViewInit(): void {
    // Create and append the TradingView ticker tape script
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = 'https://s3.tradingview.com/external-embedding/embed-widget-ticker-tape.js';
    script.async = true;

    // Set the configuration as script content (this is how TradingView expects it)
    script.textContent = JSON.stringify({
      "symbols": [
        {
          "proName": "FOREXCOM:SPXUSD",
          "title": "S&P 500"
        },
        {
          "proName": "SIX:SMI",
          "title": "Swiss Market Index"
        },
        {
          "proName": "FX_IDC:EURUSD",
          "title": "EUR to USD"
        },
        {
          "proName": "FX:GBPUSD",
          "title": "GBP to USD"
        },
        {
          "proName": "IBKR:CHFUSD",
          "title": "CHF to USD"
        },
        {
          "proName": "BITSTAMP:BTCUSD",
          "title": "Bitcoin"
        },
        {
          "proName": "BITSTAMP:ETHUSD",
          "title": "Ethereum"
        },
        {
          "proName": "NCDEX:GOLD",
          "title": "Gold"
        },
        {
          "proName": "NCDEX:SILVERINTL",
          "title": "Silver"
        }
      ],
      "showSymbolLogo": true,
      "colorTheme": "dark",
      "isTransparent": false,
      "displayMode": "compact",
      "locale": "en"
    });

    // Find the widget container and append the script
    const widgetContainer = document.querySelector('.tradingview-widget-container__widget');
    if (widgetContainer) {
      widgetContainer.appendChild(script);
    }
  }
}
