import { Routes } from '@angular/router';
import {HomePageComponent} from './features/home/home-page-component';

export const routes: Routes = [
  { path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AuthRoutes) },
  { path: 'home',
    loadComponent: () => import('./features/home/home-page-component').then(m => m.HomePageComponent) },
  { path: 'notifications',
    loadComponent: () => import('./features/notifications/notifications-component').then(m => m.NotificationsComponent) },
  { path: 'settings',
    loadComponent: () => import('./features/settings/settings-component').then(m => m.SettingsComponent) },
  { path: 'stocks',
    loadComponent: () => import('./features/stocks/stocks-component').then(m => m.StocksComponent) },
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/auth/login' }
];
