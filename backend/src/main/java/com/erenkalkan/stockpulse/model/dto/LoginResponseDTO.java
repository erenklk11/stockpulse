package com.erenkalkan.stockpulse.model.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponseDTO {

  private String firstName;
  private String email;
  private String token;
}
