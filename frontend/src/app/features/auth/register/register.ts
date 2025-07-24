import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../core/auth/auth';
import {RegisterDTO} from './model/register-dto';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';

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


  registerForm : FormGroup = new FormGroup({
    firstName : new FormControl('', [
      Validators.required
    ]),
    email : new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password : new FormControl('', [
      Validators.required,
      Validators.minLength(RegisterComponent.MIN_PASSWORD_LENGTH),
    ])
  });

  get passwordMinLength(): number {
    return RegisterComponent.MIN_PASSWORD_LENGTH;
  }

  constructor(private authService: AuthService) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    this.firstAttemptMade = true;

    if (this.registerForm.valid) {
      const registerData: RegisterDTO = {
        firstName: this.registerForm.get('firstName')?.value,
        email: this.registerForm.get('email')?.value,
        password: this.registerForm.get('password')?.value
      };

      this.authService.register(registerData).subscribe({
        next: () => {
          // TODO: Handle successful register
        },
        error: (error) => {
          // TODO: Handle register error
          console.error('Register failed:', error);
        }
      });
    }
  }

}
