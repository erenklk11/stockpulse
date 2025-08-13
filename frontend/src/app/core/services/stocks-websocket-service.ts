import { Injectable } from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StocksWebsocketService {
  private ws: WebSocket | null = null;
  private messagesSubject = new Subject<any>();
  public messages$ = this.messagesSubject.asObservable();
  private connectionState = new BehaviorSubject<'CONNECTING' | 'CONNECTED' | 'DISCONNECTED'>('DISCONNECTED');
  public connectionState$ = this.connectionState.asObservable();

  connect(url: string): void {
    if (this.ws) {
      this.ws.close();
    }

    this.connectionState.next('CONNECTING');
    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      console.log('WebSocket connected');
      this.connectionState.next('CONNECTED');
    };

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log('WebSocket message received:', data);
      this.messagesSubject.next(data);
    };

    this.ws.onerror = (event) => {
      console.error('WebSocket error:', event);
      this.messagesSubject.error(event);
      this.connectionState.next('DISCONNECTED');
    };

    this.ws.onclose = (event) => {
      console.log('WebSocket closed:', event);
      this.ws = null;
      this.connectionState.next('DISCONNECTED');
    };
  }

  subscribeToSymbols(symbols: string[]): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('WebSocket is not connected');
      return;
    }

    const message = {
      type: 'subscribe',
      symbols: symbols
    };

    this.ws.send(JSON.stringify(message));
    console.log('Sent subscription request:', message);
  }

  subscribeToSymbol(symbol: string): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('WebSocket is not connected');
      return;
    }

    const message = {
      type: 'subscribe',
      symbol: symbol
    };

    this.ws.send(JSON.stringify(message));
    console.log('Sent subscription request:', message);
  }

  sendPing(): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return;
    }

    const message = {
      type: 'ping'
    };

    this.ws.send(JSON.stringify(message));
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}
