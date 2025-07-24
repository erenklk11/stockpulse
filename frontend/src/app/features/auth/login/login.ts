import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {AuthService} from '../../../core/auth/auth';
import { LoginDTO } from './model/login-dto';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [RouterLink, CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  standalone: true,
  styleUrl: './login.css'
})
export class LoginComponent {
  private static readonly MIN_PASSWORD_LENGTH = 8;

  firstAttemptMade = false;
  showPassword = false;

  loginForm: FormGroup = new FormGroup({
    email: new FormControl('', [
      Validators.email,
      Validators.required
    ]),
    password: new FormControl('', [
      Validators.minLength(LoginComponent.MIN_PASSWORD_LENGTH),
      Validators.required
    ])
  });


  get passwordMinLength(): number {
    return LoginComponent.MIN_PASSWORD_LENGTH;
  }

  constructor(private authService: AuthService) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    this.firstAttemptMade = true;

    if (this.loginForm.valid) {
      const loginData: LoginDTO = {
        email: this.loginForm.get('email')?.value,
        password: this.loginForm.get('password')?.value
      };

      this.authService.login(loginData).subscribe({
        next: () => {
          // TODO: Handle successful login
        },
        error: (error) => {
          // TODO: Handle login error
          console.error('Login failed:', error);
        }
      });
    }
  }
}
