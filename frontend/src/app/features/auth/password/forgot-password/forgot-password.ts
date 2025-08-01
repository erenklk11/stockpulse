import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../../core/auth/auth-service';
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
  isSuccess = false;
  responseMessage = '';

  formGroup : FormGroup = new FormGroup(
    {email : new FormControl('', [Validators.email, Validators.required])}
  );

  get responseClass() : string {
    return this.isSuccess ? 'success-message' : 'error-message';
  }

  constructor(private authService : AuthService) {}

  submit() : void {
    this.firstAttemptMade = true;

    if(this.formGroup.valid) {
      var email = this.formGroup.controls['email'].value.toString().trim();
      this.authService.forgotPassword(email).subscribe({
        next: (response) => {
          if(response.mailSent) {
            this.isSuccess = true;
            this.responseMessage = 'Reset password mail has been sent to:' + email;
          }
        },
        error: (err) => {
          console.log(err);
          this.responseMessage = err.message;
        }
      });
    }
  }
}
