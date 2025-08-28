import {Component, Input, OnInit, ChangeDetectorRef} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NewsResponse} from './model/api-response';
import {environment} from '../../../environments/environment';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-news-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './news-component.html',
  styleUrl: './news-component.css'
})
export class NewsComponent implements OnInit{

  @Input() ticker: string = '';
  url: string = '';
  results: NewsResponse[] = [];

  constructor (private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
        this.buildUrl();
        this.getNews();
    }


  buildUrl() {
    if (this.ticker.trim() === '') {
      // Means no ticker input -> Fetch general news
      this.url = environment.apiUrl + environment.endpoints.api.marketNews;
    }
    else {
      this.url = environment.apiUrl + environment.endpoints.api.companyNews + '?ticker=' + this.ticker;
    }
  }

  getNews(): any {
    console.log('Making request to:', this.url);
    this.http.get<any>(this.url, {withCredentials: true}).subscribe({
      next: (response) => {

        if (response && Array.isArray(response) && response.length > 0) {
          this.results = response;

          for (let i = 0; i < this.results.length; i++) {
            this.results[i].imageUrl = this.changeImageSrc(this.results[i].imageUrl);
          }

          // Manually trigger change detection
          this.cdr.detectChanges();
        }
        else {
          this.results = [];
          console.log('No news found - response was:', response);
          this.cdr.detectChanges();
        }
      },
      error: (error)=> {
        console.error('Full error object:', error);
        if (error.error && error.error.message) {
          alert(error.error.message);
        } else {
          console.error('Error fetching news: ', error);
        }
      }
    })
  }

  openArticle(url: string): void {
    if (url) {
      window.open(url, '_blank');
    }
  }

  onImageError(event: any): void {
    event.target.src = '/yahoo_finance.jpeg'; // You can add a placeholder image
    event.target.alt = 'News image unavailable';
  }

  // Change default Yahoo Finance image with our custom one for better visibility
  changeImageSrc(src: string): string {
    if (src === 'https://s.yimg.com/rz/stage/p/yahoo_finance_en-US_h_p_finance_2.png') {
      return '/yahoo_finance.jpeg';
    }
    return src;
  }
}
