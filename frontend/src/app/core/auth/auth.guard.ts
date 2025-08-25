import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth-service';
import {map, Observable} from 'rxjs';
import { filter, take } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean> {
    console.log('AuthGuard activated');

    return this.authService.isAuthenticated$.pipe(
      // Wait until we get a definitive answer (not null)
      filter(status => status !== null),
      take(1),
      map(isAuthenticated => {
        console.log('AuthGuard - isAuthenticated:', isAuthenticated);
        if (!isAuthenticated) {
          console.log('Not authenticated, redirecting to login');
          this.router.navigate(['/auth/login']);
          return false;
        }
        return true;
      })
    );
  }
}
