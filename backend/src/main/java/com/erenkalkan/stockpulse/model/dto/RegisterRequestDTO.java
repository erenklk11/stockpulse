package com.erenkalkan.stockpulse.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "password")
public class RegisterRequestDTO {

  @NotNull
  @NotBlank
  private String firstName;
  @NotNull
  @NotBlank
  private String email;
  @NotNull
  @NotBlank
  private String password;
}
