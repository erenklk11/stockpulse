package com.erenkalkan.stockpulse.controller;

import com.erenkalkan.stockpulse.model.dto.NewsResponseDTO;
import com.erenkalkan.stockpulse.model.dto.SearchTickerResponseDTO;
import com.erenkalkan.stockpulse.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

  private final NewsService newsService;

  @GetMapping("/market-news")
  public ResponseEntity<List<NewsResponseDTO>> getMarketNews() {
    List<NewsResponseDTO> response = newsService.getMarketNews();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/company-news")
  public ResponseEntity<List<NewsResponseDTO>> getCompanyNews(@RequestParam String ticker) {
    List<NewsResponseDTO> response = newsService.getCompanyNews(ticker);
    return ResponseEntity.ok(response);
  }
}
