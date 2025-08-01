package com.erenkalkan.stockpulse.model.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterResponseDTO {

  private String firstName;
  private String email;
}
