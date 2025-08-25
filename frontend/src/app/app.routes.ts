import { Routes } from '@angular/router';
import {AuthGuard} from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AuthRoutes) },
  { path: 'home',
    loadComponent: () => import('./features/home/home-page-component').then(m => m.HomePageComponent),
    canActivate: [AuthGuard]},
  { path: 'notifications',
    loadComponent: () => import('./features/notifications/notifications-component').then(m => m.NotificationsComponent),
    canActivate: [AuthGuard]},
  { path: 'settings',
    loadComponent: () => import('./features/settings/settings-component').then(m => m.SettingsComponent),
    canActivate: [AuthGuard]},
  { path: 'stocks',
    loadComponent: () => import('./features/stocks/stocks-component').then(m => m.StocksComponent),
    canActivate: [AuthGuard]},
  { path: 'watchlist/:id',
    loadComponent: () => import('./features/watchlist/watchlist-component').then(m => m.WatchlistComponent),
    canActivate: [AuthGuard]},
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/auth/login' }
];
