import { Routes } from '@angular/router';
import {DashboardComponent} from './features/dashboard/dashboard-component/dashboard-component';
import {AuthGuard} from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AuthRoutes) },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard]},
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/auth/login' }
];
