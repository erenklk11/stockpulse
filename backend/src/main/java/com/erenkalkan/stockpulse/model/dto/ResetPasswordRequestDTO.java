package com.erenkalkan.stockpulse.model.dto;

import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {

  @NotNull
  @NotBlank
  private String token;
  @NotNull
  @NotBlank
  private String newPassword;

}
