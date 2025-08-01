export interface ResetPasswordRequestDto {
  verificationToken: string;
  newPassword: string;
}
