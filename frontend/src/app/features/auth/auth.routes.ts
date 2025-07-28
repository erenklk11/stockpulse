import { Routes } from '@angular/router';
import {LoginComponent} from './login/login';
import {RegisterComponent} from './register/register';
import {ForgotPasswordComponent} from './forgot-password/forgot-password';
import {OAuthCallbackComponent} from './oauth-callback/oauth-callback.component';



export const AuthRoutes : Routes = [
  { path: 'login', component: LoginComponent},
  { path: 'register', component: RegisterComponent},
  { path: 'forgot-password', component: ForgotPasswordComponent},
  { path: 'callback', component: OAuthCallbackComponent},
  { path: '**', redirectTo: 'login', pathMatch: 'full' }
]
