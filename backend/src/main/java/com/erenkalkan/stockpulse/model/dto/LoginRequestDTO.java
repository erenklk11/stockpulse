package com.erenkalkan.stockpulse.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginRequestDTO {

  @NotNull
  private String email;
  @NotNull
  private String password;

}
