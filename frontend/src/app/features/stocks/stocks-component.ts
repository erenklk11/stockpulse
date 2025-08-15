import { Component } from '@angular/core';
import {TickerTape} from '../home/ticker-tape/ticker-tape';
import {Stock} from './model/stock';

@Component({
  selector: 'app-stocks-component',
  imports: [
    TickerTape
  ],
  templateUrl: './stocks-component.html',
  styleUrl: './stocks-component.css'
})
export class StocksComponent {

stock!: Stock;



}
