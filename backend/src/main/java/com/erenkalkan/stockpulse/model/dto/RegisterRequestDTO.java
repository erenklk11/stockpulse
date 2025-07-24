package com.erenkalkan.stockpulse.model.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "password")
public class RegisterRequestDTO {

  private String firstName;
  private String email;
  private String password;
}
