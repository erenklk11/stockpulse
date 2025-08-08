import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormControl, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import {AuthService} from '../../../core/auth/auth-service';
import {LoginRequestDTO} from './model/login-request-dto';
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
  isGoogleLoading = false;

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
      this.registrationMessage = sessionStorage["message"];
      this.loginForm.get('email')?.setValue(sessionStorage["email"]);
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    this.firstAttemptMade = true;

    if (this.loginForm.valid) {
      const loginData: LoginRequestDTO = {
        email: this.loginForm.get('email')?.value?.trim(),
        password: this.loginForm.get('password')?.value?.trim()
      };

      this.authService.login(loginData).subscribe({
        next: (response) => {
          // No need to manually store tokens - they're in HTTP-only cookies
          alert("Login successful!");
          sessionStorage.setItem("firstName", response.firstName);
          sessionStorage.setItem("email", response.email);
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          alert("Login failed! " + error.message);
          console.error('Login failed:', error.message);
        }
      });
    }
  }

  loginWithGoogle(): void {
    this.isGoogleLoading = true;
    try {
      this.authService.loginWithGoogle();
    } catch (error) {
      this.isGoogleLoading = false;
      console.error('Google login failed:', error);
      alert('Google login failed. Please try again.');
    }
  }
}
