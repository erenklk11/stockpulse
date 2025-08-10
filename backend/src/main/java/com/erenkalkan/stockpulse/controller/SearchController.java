package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.SearchTickerResponseDTO;
import com.erenkalkan.stockpulse.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

  private final SearchService searchService;

  @GetMapping("/ticker")
  public ResponseEntity<List<SearchTickerResponseDTO>> searchTicker(@RequestParam String input) {
    List<SearchTickerResponseDTO> response = searchService.searchTicker(input);
    return ResponseEntity.ok(response);
  }
}
