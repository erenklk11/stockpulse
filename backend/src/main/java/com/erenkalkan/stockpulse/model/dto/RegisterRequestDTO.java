package com.erenkalkan.stockpulse.model.dto;

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
  private String firstName;
  @NotNull
  private String email;
  @NotNull
  private String password;
}
