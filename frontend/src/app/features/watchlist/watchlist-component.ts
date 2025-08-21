import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {Watchlist} from './model/watchlist';
import {Alert} from './model/alert';
import {ActivatedRoute, Router} from '@angular/router';
import {WatchlistService} from '../../core/services/watchlist-service';
import {TriggerType} from '../stocks/alert-popup/model/trigger-type';
import {CommonModule} from '@angular/common';
import {AlertService} from '../../core/services/alert-service';

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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private watchlistService: WatchlistService,
    private alertService: AlertService
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
