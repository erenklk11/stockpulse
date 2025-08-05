package com.erenkalkan.stockpulse.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginRequestDTO {

  @NotNull
  @NotBlank
  private String email;
  @NotNull
  @NotBlank
  private String password;

}
