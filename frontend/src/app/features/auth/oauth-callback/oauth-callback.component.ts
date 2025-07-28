import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth-service';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="callback-container">
      <div *ngIf="!hasError" class="loading-state">
        <div class="loading-spinner">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
        <h3>Completing Google authentication...</h3>
        <p>Please wait while we process your login.</p>
      </div>

      <div *ngIf="hasError" class="error-state">
        <div class="error-icon">
          <i class="fas fa-exclamation-triangle"></i>
        </div>
        <h3>Authentication Failed</h3>
        <p>{{ errorMessage }}</p>
        <button (click)="redirectToLogin()" class="retry-btn">
          Return to Login
        </button>
      </div>
    </div>
  `,
  styles: [`
    .callback-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      text-align: center;
      padding: 20px;
    }

    .loading-spinner {
      font-size: 2rem;
      color: #4285f4;
      margin-bottom: 20px;
    }

    .error-icon {
      font-size: 2rem;
      color: #dc3545;
      margin-bottom: 20px;
    }

    h3 {
      color: #333;
      margin-bottom: 10px;
    }

    p {
      color: #666;
      font-size: 14px;
      margin-bottom: 20px;
    }

    .retry-btn {
      background-color: #4285f4;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .retry-btn:hover {
      background-color: #3367d6;
    }
  `]
})
export class OAuthCallbackComponent implements OnInit {
  hasError = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.handleCallback();
  }

  private handleCallback(): void {
    try {
      // Check if there's an error parameter in the URL first
      const urlParams = new URLSearchParams(window.location.search);
      const error = urlParams.get('error');

      if (error) {
        this.handleError(`Google authentication was cancelled or failed: ${error}`);
        return;
      }

      this.authService.handleGoogleAuthCallback().subscribe({
        next: (response) => {
          // Clean up URL parameters before navigation
          window.history.replaceState({}, document.title, window.location.pathname);
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          console.error('Google authentication failed:', error);
          this.handleError('Google authentication failed. Please try again.');
        }
      });
    } catch (error) {
      console.error('Error handling Google callback:', error);
      this.handleError('Authentication error occurred. Please try again.');
    }
  }

  private handleError(message: string): void {
    this.hasError = true;
    this.errorMessage = message;
  }

  redirectToLogin(): void {
    this.router.navigate(['/auth/login']);
  }
}
