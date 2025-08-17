import {Component, Input} from '@angular/core';
import {StockRecommendationsDTO} from '../model/stock-recommendations';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-recommendations-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recommendations-component.html',
  styleUrl: './recommendations-component.css'
})
export class RecommendationsComponent {

  @Input() recommendations!: StockRecommendationsDTO;

  getConsensusText(): string {
    const strongBuy = parseInt(this.recommendations.strongBuy);
    const buy = parseInt(this.recommendations.buy);
    const hold = parseInt(this.recommendations.hold);
    const sell = parseInt(this.recommendations.sell);
    const strongSell = parseInt(this.recommendations.strongSell);

    const bullish = strongBuy + buy;
    const bearish = sell + strongSell;

    if (bullish > bearish + hold) {
      return 'BULLISH';
    } else if (bearish > bullish + hold) {
      return 'BEARISH';
    } else {
      return 'NEUTRAL';
    }
  }

  getConsensusClass(): string {
    const consensus = this.getConsensusText();
    return `consensus-${consensus.toLowerCase()}`;
  }

  isHighestRecommendation(type: keyof StockRecommendationsDTO): boolean {
    const values = {
      strongBuy: parseInt(this.recommendations.strongBuy),
      buy: parseInt(this.recommendations.buy),
      hold: parseInt(this.recommendations.hold),
      sell: parseInt(this.recommendations.sell),
      strongSell: parseInt(this.recommendations.strongSell)
    };

    const maxValue = Math.max(...Object.values(values));
    return values[type] === maxValue && maxValue > 0;
  }

}
