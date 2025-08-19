import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {AlertDTO} from './model/alert';
import {TriggerType} from './model/trigger-type';
import {Watchlist} from '../../home/watchlists-section/model/watchlist';
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
  @Input() currentStockPrice: number | null = null;
  @Output() selectedWatchlist = new EventEmitter<Watchlist>();
  @Output() closeModal = new EventEmitter<void>();

  triggerType: TriggerType = TriggerType.TO_PRICE;
  alertValue!: number;

  watchlists: Watchlist[] = [];
  selectedWatchlistId: number | null = null;

  public TriggerType = TriggerType;

  constructor(
    private http: HttpClient,
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
    return !!(
      this.alertValue &&
      this.alertValue > 0 &&
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
      symbol: this.symbol,
      triggerType: this.triggerType,
      alertValue: this.alertValue,
      watchlistId: this.selectedWatchlistId!
    };

    console.log(alertDTO);

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

  closePopup(): void {
    this.closeModal.emit();
  }
}
