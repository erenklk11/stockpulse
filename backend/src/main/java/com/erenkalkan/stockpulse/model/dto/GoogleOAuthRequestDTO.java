package com.erenkalkan.stockpulse.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleOAuthRequestDTO {

    @NotNull
    @NotBlank(message = "Authorization code is required")
    private String code;
    private String codeVerifier;
}
