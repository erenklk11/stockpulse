import {Component, Input} from '@angular/core';
import {StockFinancialsDTO} from '../model/stock-financials';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-financials-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './financials-component.html',
  styleUrl: './financials-component.css'
})
export class FinancialsComponent {

  @Input() financials!: StockFinancialsDTO;

  formatCurrency(value: number): string {
    if (Math.abs(value) >= 1e12) {
      return (value / 1e12).toFixed(2) + 'T';
    } else if (Math.abs(value) >= 1e9) {
      return (value / 1e9).toFixed(2) + 'B';
    } else if (Math.abs(value) >= 1e6) {
      return (value / 1e6).toFixed(2) + 'M';
    } else if (Math.abs(value) >= 1e3) {
      return (value / 1e3).toFixed(2) + 'K';
    } else {
      return value.toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    }
  }

  getValueClass(value: number): string {
    return value >= 0 ? 'positive' : 'negative';
  }

}
