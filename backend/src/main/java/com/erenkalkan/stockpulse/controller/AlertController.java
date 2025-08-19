package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.CreateAlertRequestDTO;
import com.erenkalkan.stockpulse.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/alert")
@RestController
public class AlertController {

  private final AlertService alertService;

  @PostMapping("/create")
  public ResponseEntity<Map<String, Boolean>> createAlert(@Valid @RequestBody CreateAlertRequestDTO request, Authentication authentication) {
    Map<String, Boolean> response = new HashMap<>();
    response.put("created", alertService.createAlert(request, authentication));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/delete")
  public ResponseEntity<Map<String, Boolean>> deleteAlert(@PathVariable Long id, Authentication authentication) {
    Map<String, Boolean> response = new HashMap<>();
    response.put("deleted", alertService.deleteAlert(id, authentication));
    return ResponseEntity.ok(response);
  }
}
