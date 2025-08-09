import { Routes } from '@angular/router';
import {HomePageComponent} from './features/home/home-page-component/home-page-component';
import {AuthGuard} from './core/auth/auth.guard';
import {SettingsComponent} from './features/settings/settings-component';

export const routes: Routes = [
  { path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AuthRoutes) },
  { path: 'home', component: HomePageComponent},
  { path: 'notifications', loadComponent: () => import ('./features/notifications/notifications-component').then(m => m.NotificationsComponent)},
  { path: 'settings', loadComponent: () => import ('./features/settings/settings-component').then(m => m.SettingsComponent)},
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/auth/login' }
];
