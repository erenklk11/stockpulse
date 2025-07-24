package com.erenkalkan.stockpulse.model.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LoginRequestDTO {

  private String email;
  private String password;

}
