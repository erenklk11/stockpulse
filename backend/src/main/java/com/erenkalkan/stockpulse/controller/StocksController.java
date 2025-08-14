package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.StockDataDTO;
import com.erenkalkan.stockpulse.service.StocksService;
import com.google.errorprone.annotations.RequiredModifiers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stocks")
public class StocksController {

  private final StocksService stocksService;

  @GetMapping("/stock-data")
  public ResponseEntity<StockDataDTO> getStockData(@RequestParam String symbol) {
    return ResponseEntity.ok(stocksService.getStockData(symbol));
  }
}
