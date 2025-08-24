package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.exception.InvalidInputException;
import com.erenkalkan.stockpulse.model.dto.NewsResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

  @Value("${app.api.finnhub.url}")
  private String url;

  @Value("${app.api.finnhub.key}")
  private String key;

  private final RestClient restClient;


  public List<NewsResponseDTO> getMarketNews() {

    String apiUrl = url + "news?category=general&token=" + key;

    return fetchNews(apiUrl);
  }


  public List<NewsResponseDTO> getCompanyNews(String ticker) {

    if (ticker == null || ticker.trim().isEmpty()) {
      throw new InvalidInputException("Stock cannot be null or empty");
    }

    String fromDate = LocalDate.now().minusMonths(1).toString();
    String toDate = LocalDate.now().toString();

    String apiUrl = url + "company-news?symbol=" + ticker + "&from=" + fromDate + "&to=" + toDate+ "&token=" + key;

    return fetchNews(apiUrl);
  }

  private List<NewsResponseDTO> fetchNews(String url) {

    try {
      List<Map<String, Object>> response = restClient.get()
              .uri(url)
              .retrieve()
              .body(List.class);

      if (response != null && !response.isEmpty()) {
        List<NewsResponseDTO> results = new ArrayList<>();
        int limit = Math.min(6, response.size());

        for (int i = 0; i < limit; i++) {
          Map<String, Object> newsItem = response.get(i);

          if (newsItem == null) {
            continue;
          }

          NewsResponseDTO newsDto = NewsResponseDTO.builder()
                  .headline((String) newsItem.get("headline"))
                  .imageUrl((String) newsItem.get("image"))
                  .articleUrl((String) newsItem.get("url"))
                  .build();

          results.add(newsDto);
        }

        return results;
      } else {
        // Return empty result if no news found
        log.error("Returning empty news list");
        return List.of();
      }
    } catch (Exception e) {
      throw new RestClientException("Failed to fetch news from Finnhub API", e);
    }
  }
}
