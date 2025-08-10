package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.model.dto.SearchTickerResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

  @Value("${app.api.alphaVantage.url}")
  private String url;

  @Value("${app.api.alphaVantage.key}")
  private String key;

  private final RestClient restClient;

  public List<SearchTickerResponseDTO> searchTicker(String input) {

    if (input == null || input.trim().isEmpty()) {
      throw new InvalidInputException("Input cannot be null or empty");
    }
    String apiUrl = url + "function=SYMBOL_SEARCH&keywords=" + input + "&apikey=" + key;

    try {
      Map<String, Object> response = restClient.get()
          .uri(apiUrl)
          .retrieve()
          .body(Map.class);

      if (response != null && response.containsKey("bestMatches")) {
        List<Map<String, String>> bestMatches = (List<Map<String, String>>) response.get("bestMatches");

        // Only showing the first 5 results since rest would be irrelevant for the user
        List<SearchTickerResponseDTO> processedResults = bestMatches.stream()
            .limit(5)
            .map(match -> SearchTickerResponseDTO.builder()
                .symbol(match.get("1. symbol"))
                .name(match.get("2. name"))
                .build())
            .collect(Collectors.toList());

        return processedResults;
      } else {
        // Return empty result if no bestMatches found (API limit reached or other issues)
        return List.of();
      }
    } catch (Exception e) {
      throw new RestClientException("Failed to fetch search results from Alpha Vantage API", e);
    }
  }
}
