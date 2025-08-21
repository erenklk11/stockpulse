package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.StockDTO;
import com.erenkalkan.stockpulse.model.dto.StockDataDTO;
import com.erenkalkan.stockpulse.service.StocksService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stocks")
public class StocksController {

  private final StocksService stocksService;

  @GetMapping("/stock-data")
  public ResponseEntity<StockDataDTO> getStockData(@RequestParam String symbol) {
    return ResponseEntity.ok(stocksService.getStockDataForHomePage(symbol));
  }

  @GetMapping("/stock")
  public ResponseEntity<StockDTO> getStock(@RequestParam String symbol) {
    return ResponseEntity.ok(stocksService.getStock(symbol));
  }

  @GetMapping("/stock-close-price")
  public ResponseEntity<Map<String, Double>> getStockClosePrice(@RequestParam String symbol) {
    Map<String, Double> response = new HashMap<>();
    response.put("price", stocksService.fetchStockClosePrice(symbol));
    return ResponseEntity.ok(response);
  }
}
