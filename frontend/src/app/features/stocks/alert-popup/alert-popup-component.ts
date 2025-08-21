import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AlertDTO} from './model/alert';
import {TriggerType} from './model/trigger-type';
import {WatchlistDTO} from '../../home/watchlists-section/model/watchlist';
import {WatchlistService} from '../../../core/services/watchlist-service';
import {AlertService} from '../../../core/services/alert-service';

@Component({
  selector: 'app-alert-popup',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './alert-popup-component.html',
  styleUrl: './alert-popup-component.css'
})
export class AlertPopupComponent implements OnInit {

  @Input() symbol: string = '';
  @Input() companyName: string = '';
  @Input() currentStockPrice!: number;
  @Output() selectedWatchlist = new EventEmitter<WatchlistDTO>();
  @Output() closeModal = new EventEmitter<void>();

  triggerType: TriggerType = TriggerType.TO_PRICE;
  alertValue!: number;

  watchlists: WatchlistDTO[] = [];
  selectedWatchlistId: number | null = null;

  public TriggerType = TriggerType;

  constructor(
    private cdr: ChangeDetectorRef,
    private watchlistService: WatchlistService,
    private alertService: AlertService
  ) {}

  ngOnInit(): void {
    this.getAllWatchlists();
  }

  selectWatchlist(watchlist: any): void {
    this.selectedWatchlistId = watchlist.id;
  }

  isFormValid(): boolean {
    if (this.triggerType === TriggerType.TO_PRICE) {
      return !!(
        this.alertValue &&
        this.alertValue > 0 &&
        this.selectedWatchlistId &&
        this.symbol
      );
    }
    return !!(
      this.alertValue &&
      this.alertValue > -100 &&
      this.selectedWatchlistId &&
      this.symbol
    );
  }

  getAllWatchlists(): void {
    this.watchlistService.getAllWatchlists().subscribe({
      next: (watchlists) => {
        this.watchlists = watchlists ?? []; // fallback if null
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Error fetching watchlists:", err);
        this.watchlists = [];
      }
    });
  }

  createAlert(): void {
    if (!this.isFormValid()) return;

    const alertDTO: AlertDTO = {
      stock: {
        symbol: this.symbol,
        companyName: this.companyName
      },
      triggerType: this.triggerType,
      alertValue: this.alertValue,
      targetValue: this.triggerType === TriggerType.PERCENTAGE_CHANGE_PRICE ? this.calculateTargetValue(this.alertValue): this.alertValue,
      watchlistId: this.selectedWatchlistId!
    };

    this.alertService.createAlert(alertDTO).subscribe({
      next: (response: any) => {
        if (response.created) {
          alert("Alert for " + this.symbol + " has successfully been created");
          this.cdr.detectChanges();
          this.closePopup();
        }
      },
      error: (error: any) => {
        if (error.message) {
          console.error("Error creating watchlist: " + error.message());
        }
        else {
          console.error("Error creating watchlist");
        }
      }
    });
  }

  calculateTargetValue(percentage: number): number {

    const multiplier = percentage / 100;
    return this.currentStockPrice + (this.currentStockPrice * multiplier);
  }

  closePopup(): void {
    this.closeModal.emit();
  }
}
