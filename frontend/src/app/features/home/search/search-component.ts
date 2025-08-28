import {ChangeDetectorRef, Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {SearchTickerResponse} from './model/api-response';
import {Router} from '@angular/router';

@Component({
  selector: 'app-search-component',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-component.html',
  styleUrl: './search-component.css'
})
export class SearchComponent {

  stockInput: string = '';
  results: SearchTickerResponse[] = [];
  private searchTimeout: any;
  isLoading: boolean = false;

  constructor (private http: HttpClient,
               private cdr: ChangeDetectorRef,
               private router: Router) {}

  getBestMatches(): any {
    // Clear any existing timeout
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    // Clear results immediately when user starts typing
    this.results = [];

    // Don't search for empty input
    if (!this.stockInput.trim()) {
      this.isLoading = false;
      return;
    }

    // Set loading state
    this.isLoading = true;

    // Set a new timeout for 2 seconds
    this.searchTimeout = setTimeout(() => {

      this.http.get<any>(environment.apiUrl + environment.endpoints.api.search + `?input=${this.stockInput.trim()}`, {
        withCredentials: true // Include HTTP-only cookies for authentication
      }).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response && response.length > 0) {
            this.results = response;
            this.cdr.detectChanges();
          }
          else {
            this.results = [];
            if (response && response.length === 0) {
              console.log('No search results found');
            } else {
              alert("External API limit has unfortunately been reached");
            }
          }
        },
        error: (error) => {
          this.isLoading = false;
          if (error.error && error.message) {
            alert(error.message);
          } else {
            console.error('Error fetching search results: ' + error);
          }
        }
      });
    }, 2000);
  }

  onResultClicked(symbol: string):void {
    this.router.navigate([`/stocks`], { queryParams: { symbol } });
  }

}
