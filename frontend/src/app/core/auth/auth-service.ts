import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { OAuthService, AuthConfig } from 'angular-oauth2-oidc';
import { LoginRequestDTO } from '../../features/auth/login/model/login-request-dto';
import {Observable, BehaviorSubject, of} from 'rxjs';
import { environment } from '../../../environments/environment';
import { RegisterRequestDto } from '../../features/auth/register/model/register-request-dto';
import { Router } from '@angular/router';
import {catchError, tap} from 'rxjs/operators';
import {ResetPasswordRequestDto} from '../../features/auth/password/model/reset-password-request-dto';
import {ChangePasswordRequestDTO} from '../../features/settings/model/change-password-request-dto';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean | null>(null);
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
    // PKCE Configuration for public clients
    disablePKCE: false,
    // Disable automatic token refresh and validation since we handle it server-side
    disableAtHashCheck: true,
    skipIssuerCheck: true,
    // Make sure we don't try to get tokens automatically
    silentRefreshRedirectUri: undefined,
    // Ensure no client secret is sent
    dummyClientSecret: undefined,
    // Add these for better debugging
    requireHttps: false, // Set to true in production
    showDebugInformation: true,
    // Clear session checks that might interfere
    clearHashAfterLogin: true,
    // Ensure proper flow
    useSilentRefresh: false,
    sessionChecksEnabled: false
  };

  constructor(
    private http: HttpClient,
    private oauthService: OAuthService,
    private router: Router
  ) {
    this.configureGoogleAuth();
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
    return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.register, registerData);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.forgotPassword, email);
  }

  verifyPasswordResetToken(token: string): Observable<any> {
    return this.http.post<boolean>(environment.apiUrl + environment.endpoints.auth.verifyToken, { token });
  }

  resetPassword(request: ResetPasswordRequestDto) : Observable<any> {
    return this.http.put<any>(environment.apiUrl + environment.endpoints.auth.resetPassword, {request});
  }

  changePassword(request: ChangePasswordRequestDTO) : Observable<any> {
    return this.http.put<any>(environment.apiUrl + environment.endpoints.auth.changePassword, request, {withCredentials: true});
  }

  // Google OAuth2 methods
  private configureGoogleAuth(): void {
    this.oauthService.configure(this.authConfig);

    this.oauthService.loadDiscoveryDocument().then(() => {
      console.log('Discovery document loaded');
      console.log('OAuth service configured with:', {
        clientId: this.authConfig.clientId,
        redirectUri: this.authConfig.redirectUri,
        scope: this.authConfig.scope
      });
    }).catch(error => {
      console.error('Error loading discovery document:', error);
    });
  }

  async loginWithGoogle(): Promise<void> {
    try {
      console.log('Starting Google OAuth flow...');

      // Clear any existing tokens first
      this.oauthService.logOut(true);

      // Make sure discovery document is loaded
      if (!this.oauthService.discoveryDocumentLoaded) {
        console.log('Loading discovery document first...');
        await this.oauthService.loadDiscoveryDocument();
      }

      console.log('Initiating login flow...');
      console.log('Current URL before redirect:', window.location.href);
      console.log('Redirect URI:', this.authConfig.redirectUri);

      // Start the OAuth flow
      this.oauthService.initLoginFlow();

    } catch (error) {
      console.error('Error in loginWithGoogle:', error);
    }
  }

  handleGoogleAuthCallback(): Observable<any> {
    // Get the authorization code from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const state = urlParams.get('state');

    if (!code) {
      throw new Error('No authorization code found in callback URL');
    }

    // Verify state parameter if present
    const storedState = sessionStorage.getItem('oauth-state') || localStorage.getItem('oauth-state');
    if (state && storedState && state !== storedState) {
      throw new Error('Invalid state parameter');
    }

    // Get the code verifier that was generated for PKCE
    let codeVerifier: string | null = null;

    // Check different possible storage locations for the code verifier
    codeVerifier = sessionStorage.getItem('PKCE_verifier') ||
      localStorage.getItem('PKCE_verifier') ||
      (this.oauthService as any).pkceCodeVerifier;

    if (!codeVerifier) {
      console.warn('No PKCE code verifier found. This might cause issues.');
    }

    return this.http.post<any>(environment.apiUrl + environment.endpoints.auth.googleAuth, {
      code: code,
      codeVerifier: codeVerifier,
      redirectUri: this.authConfig.redirectUri // Include redirect URI for verification
    }, {
      withCredentials: true // Enable HTTP-only cookies
    }).pipe(
      tap((response) => {
        sessionStorage.setItem("firstName", response.firstName);
        sessionStorage.setItem("email", response.email);
        sessionStorage.setItem("isOAuthUser", "true");
        this.handleAuthenticationSuccess(response);

        // Clean up URL parameters
        window.history.replaceState({}, document.title, window.location.pathname);

        // Clear OAuth-related storage items
        sessionStorage.removeItem('PKCE_verifier');
        localStorage.removeItem('PKCE_verifier');
        sessionStorage.removeItem('oauth-state');
        localStorage.removeItem('oauth-state');

      }),
      catchError((error) => {
        console.error('Google auth callback error:', error);
        throw error;
      })
    );
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
    // Logout from Google OAuth (don't revoke tokens since we're using server-side flow)
    this.oauthService.logOut(true);

    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    const currentStatus = this.isAuthenticatedSubject.value;
    return currentStatus === true;
  }

  checkAuthenticationStatus(): void {
    console.log('Checking authentication status...');

    this.http.post(environment.apiUrl + environment.endpoints.auth.verify, {}, {
      withCredentials: true
    }).subscribe({
      next: (response: any) => {
        console.log('Auth verification response:', response);
        this.isAuthenticatedSubject.next(response.verified);
      },
      error: (error) => {
        console.error('Auth verification error:', error);
        this.isAuthenticatedSubject.next(false);
      }
    });
  }
}
