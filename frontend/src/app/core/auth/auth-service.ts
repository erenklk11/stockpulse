import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { OAuthService, AuthConfig } from 'angular-oauth2-oidc';
import { LoginRequestDTO } from '../../features/auth/login/model/login-request-dto';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environments';
import { RegisterRequestDto } from '../../features/auth/register/model/register-request-dto';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import {ResetPasswordRequestDto} from '../../features/auth/password/model/reset-password-request-dto';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  // In-memory token storage (more secure than localStorage)
  private accessToken: string | null = null;

  private authConfig: AuthConfig = {
    issuer: 'https://accounts.google.com',
    redirectUri: environment.googleOAuth.redirectUri,
    clientId: environment.googleOAuth.clientId,
    scope: environment.googleOAuth.scope,
    responseType: 'code',
    oidc: true,
    strictDiscoveryDocumentValidation: false,
    disablePKCE: false, // Enable PKCE
    showDebugInformation: true, // Enable for debugging
  };

  constructor(
    private http: HttpClient,
    private oauthService: OAuthService,
    private router: Router
  ) {
    this.configureGoogleAuth();
    this.checkAuthenticationStatus();
  }

  login(loginData: LoginRequestDTO): Observable<any> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.login, loginData, {
      withCredentials: true // Enable HTTP-only cookies
    }).pipe(
      tap((response) => {
        this.handleAuthenticationSuccess(response);
      })
    );
  }

  register(registerData: RegisterRequestDto): Observable<any> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.register, registerData, {
      withCredentials: true
    });
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.forgotPassword, email);
  }

  verifyPasswordResetToken(token: string): Observable<boolean> {
    return this.http.post<boolean>(environment.apiUrl + environment.endpoints.auth.verifyToken, { token });
  }

  resetPassword(request: ResetPasswordRequestDto) : Observable<boolean> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.resetPassword, {request});
  }

  // Google OAuth2 methods
  private configureGoogleAuth(): void {
    this.oauthService.configure(this.authConfig);
    this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
      if (this.oauthService.hasValidAccessToken()) {
        this.isAuthenticatedSubject.next(true);
      }
    });
  }

  loginWithGoogle(): void {
    this.oauthService.initLoginFlow();
  }

  handleGoogleAuthCallback(): Observable<any> {
    // Get the authorization code from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');

    if (code) {
      // Get the code verifier from localStorage (where angular-oauth2-oidc stores it)
      const codeVerifier = localStorage.getItem('PKCE_verifier') ||
                          sessionStorage.getItem('PKCE_verifier') ||
                          (this.oauthService as any).pkceCodeVerifier;

      return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.googleAuth, {
        code: code,
        codeVerifier: codeVerifier
      }, {
        withCredentials: true // Enable HTTP-only cookies
      }).pipe(
        tap((response) => {
          this.handleAuthenticationSuccess(response);
        })
      );
    }
    throw new Error('No authorization code found in callback URL');
  }

  private handleAuthenticationSuccess(response: any): void {
    // For HTTP-only cookies, we don't need to check for accessToken in response
    // The authentication status is managed by the server via cookies
    this.isAuthenticatedSubject.next(true);
  }

  logout(): void {
    // Clear in-memory tokens
    this.accessToken = null;

    // Call logout endpoint to clear HTTP-only cookies
    this.http.post(environment.apiUrl + environment.endpoints.auth.logout, {}, {
      withCredentials: true
    }).subscribe({
      next: () => {
        this.completeLogout();
      },
      error: () => {
        // Even if logout fails, clear local state
        this.completeLogout();
      }
    });
  }

  private completeLogout(): void {
    // Logout from Google OAuth
    this.oauthService.logOut();

    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    const googleToken = this.oauthService.hasValidAccessToken();
    // For HTTP-only cookies, we'll need to check with the server
    return this.isAuthenticatedSubject.value || googleToken;
  }

  // For HTTP-only cookies, we don't expose the actual token
  getToken(): string | null {
    // Return Google token if available, otherwise null
    // HTTP-only cookies are automatically sent by the browser
    return this.oauthService.getAccessToken() || null;
  }

  getUserInfo(): any {
    const googleClaims = this.oauthService.getIdentityClaims();
    if (googleClaims) {
      return googleClaims;
    }

    // For JWT stored in HTTP-only cookies, we'll need to fetch user info from an API endpoint
    return null;
  }

  // Check authentication status on app initialization
  private checkAuthenticationStatus(): void {
    // Make a request to verify if user is authenticated via HTTP-only cookies
    this.http.post(environment.apiUrl + environment.endpoints.auth.verify, {
      withCredentials: true
    }).subscribe({
      next: (response: any) => {
        if (response.verified) {
          this.isAuthenticatedSubject.next(true);
        }
      },
      error: () => {
        this.isAuthenticatedSubject.next(false);
      }
    });
  }

}
