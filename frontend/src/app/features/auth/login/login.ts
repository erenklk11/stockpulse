import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {AuthService} from '../../../core/auth/auth';
import { LoginDTO } from './model/login-dto';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [RouterLink, CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  standalone: true,
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  private static readonly MIN_PASSWORD_LENGTH = 8;

  firstAttemptMade = false;
  showPassword = false;
  registrationMessage: string | null = null;
  registrationData: any = null;

  loginForm: FormGroup = new FormGroup({
    email: new FormControl('', [
      Validators.email,
      Validators.required,
      Validators.maxLength(50)
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(LoginComponent.MIN_PASSWORD_LENGTH),
      Validators.maxLength(50)
    ])
  });


  get passwordMinLength(): number {
    return LoginComponent.MIN_PASSWORD_LENGTH;
  }

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    // Check if we received registration data from router state
    const state = window.history.state;
    if (state && state.registrationData) {
      this.registrationData = state.registrationData;
      this.registrationMessage = state.message;

      if (this.registrationData?.email) {
        this.loginForm.get('email')?.setValue(this.registrationData.email);
      }
    }
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    this.firstAttemptMade = true;

    if (this.loginForm.valid) {
      const loginData: LoginDTO = {
        email: this.loginForm.get('email')?.value?.trim(),
        password: this.loginForm.get('password')?.value?.trim()
      };

      this.authService.login(loginData).subscribe({
        next: (response) => {
          alert("Login successful!")
        },
        error: (error) => {
          alert("Login failed!" + error)
          console.error('Login failed:', error);
        }
      });
    }
  }
}
