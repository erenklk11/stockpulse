import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {LoginDTO} from '../../features/auth/login/model/login-dto';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environments';
import {RegisterDTO} from '../../features/auth/register/model/register-dto';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http : HttpClient) {}

  login(loginData : LoginDTO) : Observable<any> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.login, loginData);
  }

  register(registerData : RegisterDTO) : Observable<any> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.register, registerData);
  }

  forgotPassword(email : string) : Observable<any> {
    return this.http.post<any>(environment.apiUrl + environment.endpoints.forgotPassword, email);
  }
}
