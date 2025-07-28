import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../core/auth/auth-service';
import {RegisterRequestDto} from './model/register-request-dto';
import {CommonModule} from '@angular/common';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {

  private static readonly MIN_PASSWORD_LENGTH = 8;

  firstAttemptMade : boolean = false;
  showPassword = false;
  isGoogleLoading = false;


  registerForm : FormGroup = new FormGroup({
    firstName : new FormControl('', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(30)
    ]),
    email : new FormControl('', [
      Validators.required,
      Validators.email,
      Validators.maxLength(50)
    ]),
    password : new FormControl('', [
      Validators.required,
      Validators.minLength(RegisterComponent.MIN_PASSWORD_LENGTH),
      Validators.maxLength(50)
    ])
  });

  get passwordMinLength(): number {
    return RegisterComponent.MIN_PASSWORD_LENGTH;
  }

  constructor(private authService: AuthService,
              private  router: Router) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    this.firstAttemptMade = true;

    if (this.registerForm.valid) {
      const registerData: RegisterRequestDto = {
        firstName: this.registerForm.get('firstName')?.value?.trim(),
        email: this.registerForm.get('email')?.value?.trim(),
        password: this.registerForm.get('password')?.value?.trim()
      };

      this.authService.register(registerData).subscribe({
        next: (response) => {
          alert("Registration successful!")
          this.registerForm.reset();
          this.firstAttemptMade = false;

          this.router.navigate(['/auth/login'], {
            state: {
              registrationData: response,
              message: 'Registration successful! Please log in with your credentials.'
            }
          });
        },
        error: (error) => {
          alert("Registration failed!")
          console.log(error);
        }
      });
    }
  }

  registerWithGoogle(): void {
    this.isGoogleLoading = true;
    try {
      this.authService.loginWithGoogle();
    } catch (error) {
      this.isGoogleLoading = false;
      console.error('Google registration failed:', error);
      alert('Google registration failed. Please try again.');
    }
  }
}
