import {Component, Input} from '@angular/core';
import {StockDataDTO} from '../model/stock-data';

@Component({
  selector: 'app-description-component',
  standalone: true,
  imports: [],
  templateUrl: './description-component.html',
  styleUrl: './description-component.css'
})
export class DescriptionComponent {

  @Input() description!: string;
}
