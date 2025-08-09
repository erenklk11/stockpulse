import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environments';
import {SearchTickerResponse} from './model/api-response';

@Component({
  selector: 'app-search-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './search-component.html',
  styleUrl: './search-component.css'
})
export class SearchComponent {

  stockInput: string = '';
  results: SearchTickerResponse[] = [];
  private searchTimeout: any;
  isLoading: boolean = false;

  constructor (private http: HttpClient) {}

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
      var apiUrl = environment.api.alphaVantage.url +
        `function=SYMBOL_SEARCH&keywords=${this.stockInput}&apikey=${environment.api.alphaVantage.key}`;

      this.http.get<any>(apiUrl).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.bestMatches && Array.isArray(response.bestMatches)) {
            // Only showing the first 5 results since rest would be irrelevant for the user
            this.results = response.bestMatches.slice(0, 5).map((match: any) => ({
              symbol: match['1. symbol'],
              name: match['2. name']
            }));
          }
          else {
            alert("External API limit has unfortunately been reached");
          }
        },
        error: () => {
          this.isLoading = false;
          console.error('Error fetching search results');
        }
      });
    }, 2000);
  }


}
