package com.erenkalkan.stockpulse.model.dto;

import com.erenkalkan.stockpulse.model.entity.Stock;
import com.erenkalkan.stockpulse.model.enums.TriggerType;
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
  private Stock stock;
  @NotNull
  private TriggerType triggerType;
  private Double percentageValue;
  @NotNull
  private Double targetValue;
  @NotNull
  private Long watchlistId;
}
