package com.erenkalkan.stockpulse.model.dto;

import com.erenkalkan.stockpulse.model.enums.TriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAlertRequestDTO {

  @NotNull
  @NotBlank
  private String symbol;
  @NotNull
  private TriggerType triggerType;
  @NotNull
  private Long alertValue;
  @NotNull
  private Long watchlistId;
}
