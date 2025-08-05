import { Component, OnInit , ChangeDetectorRef} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth-service';
import { CommonModule } from '@angular/common';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {ResetPasswordRequestDto} from '../model/reset-password-request-dto';

@Component({
  selector: 'app-reset-password-component',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './reset-password-component.html',
  styleUrl: './reset-password-component.css'
})
export class ResetPasswordComponent implements OnInit {
  firstAttemptMade : boolean = false;
  showPassword = false;
  isLoading = true;
  isTokenValid = false;
  isSuccess = false;
  errorMessage = '';
  responseMessage = '';
  token = '';

  private static readonly MIN_PASSWORD_LENGTH = 8;

  formGroup : FormGroup = new FormGroup(
    {newPassword : new FormControl('', [
      Validators.required,
      Validators.minLength(ResetPasswordComponent.MIN_PASSWORD_LENGTH),
      Validators.maxLength(50)])
    }
  );

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (this.token) {
        this.verifyToken();
      } else {
        this.handleError('No token provided');
      }
    });
  }

  private verifyToken(): void {
    this.isLoading = true;
    this.authService.verifyPasswordResetToken(this.token).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        if (response.tokenVerified) {
          this.isSuccess = true;
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.handleError(error.error?.message || 'Token verification failed');
      }
    });
  }

  submit() : void {
    this.firstAttemptMade = true;
    const request : ResetPasswordRequestDto = {
      newPassword : this.formGroup.controls['newPassword'].value.toString().trim(),
      verificationToken : this.token
    }

    this.authService.resetPassword(request).subscribe({
      next: (response: any) => {
        if (response.passwordReset) {
          this.isSuccess = true;
          this.responseMessage = "Password Reset successful."
          this.cdr.detectChanges();
        }
      },
      error: (err)=> {
        console.log(err);
        this.errorMessage = err.message;
    }
    });
  }

  private handleError(message: string): void {
    this.errorMessage = message;
    this.isTokenValid = false;
    this.isLoading = false;
  }

  get passwordMinLength(): number {
    return ResetPasswordComponent.MIN_PASSWORD_LENGTH;
  }

  get responseClass() : string {
    return this.isSuccess ? 'success-message' : 'error-message';
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }
}
