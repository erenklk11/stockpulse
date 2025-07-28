import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../core/auth/auth-service';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPasswordComponent {

  firstAttemptMade = false;

  formGroup : FormGroup = new FormGroup(
    {email : new FormControl('', [Validators.email, Validators.required])}
  );

  constructor(private authService : AuthService) {}

  submit() : void {
    this.firstAttemptMade = true;

    if(this.formGroup.valid) {
      this.authService.forgotPassword(this.formGroup.controls['email'].value).subscribe({
        next: () => {
          // TODO: Handle forgot password
        },
        error: () => {
          // TODO: Handle forgot password error
        }
      });
    }
  }
}
