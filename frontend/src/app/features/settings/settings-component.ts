import {ChangeDetectorRef, Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../core/auth/auth-service';
import {ChangePasswordRequestDTO} from './model/change-password-request-dto';

@Component({
  selector: 'app-settings-component',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './settings-component.html',
  styleUrl: './settings-component.css'
})
export class SettingsComponent {
  firstAttemptMade = false;
  showCurrentPassword = false;
  showNewPassword = false;
  isLoading = false;
  isSuccess = false;
  errorMessage = '';
  responseMessage = '';

  private static readonly MIN_PASSWORD_LENGTH = 8;

  formGroup: FormGroup = new FormGroup({
    currentPassword: new FormControl('', [Validators.required]),
    newPassword: new FormControl('', [
      Validators.required,
      Validators.minLength(SettingsComponent.MIN_PASSWORD_LENGTH),
      Validators.maxLength(50)
    ])
  });

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  submit(): void {
    this.firstAttemptMade = true;
    this.isLoading = true;
    this.errorMessage = '';
    this.responseMessage = '';

    if (this.formGroup.invalid) {
      this.isLoading = false;
      return;
    }

    const request: ChangePasswordRequestDTO = {
      currentPassword: this.formGroup.controls['currentPassword'].value.toString().trim(),
      newPassword: this.formGroup.controls['newPassword'].value.toString().trim()
    };

    this.authService.changePassword(request).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        if (response.passwordChanged) {
          this.isSuccess = true;
          this.responseMessage = "Password changed successfully.";
          this.formGroup.reset();
          this.firstAttemptMade = false;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.log(err);
        this.isLoading = false;
        this.isSuccess = false;
        this.errorMessage = err.error?.message || 'Password change failed. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }

  get passwordMinLength(): number {
    return SettingsComponent.MIN_PASSWORD_LENGTH;
  }

  get responseClass(): string {
    return this.isSuccess ? 'success-message' : 'error-message';
  }

  toggleCurrentPasswordVisibility(): void {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleNewPasswordVisibility(): void {
    this.showNewPassword = !this.showNewPassword;
  }
}
