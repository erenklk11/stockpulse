package com.erenkalkan.stockpulse.model.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LoginResponseDTO {

  private String firstName;
  private String email;
  private String token;
}
