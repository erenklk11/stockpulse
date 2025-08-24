package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.dto.NewsResponseDTO;
import com.erenkalkan.stockpulse.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

  @Mock
  private RestClient restClient;

  @Mock
  private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock
  private RestClient.RequestHeadersSpec requestHeadersSpec;

  @Mock
  private RestClient.ResponseSpec responseSpec;

  @InjectMocks
  private NewsService newsService;

  private final String TEST_URL = "https://finnhub.io/api/v1/";
  private final String TEST_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    // Arrange - Set up configuration properties using reflection
    ReflectionTestUtils.setField(newsService, "url", TEST_URL);
    ReflectionTestUtils.setField(newsService, "key", TEST_KEY);
  }

  @Test
  void getMarketNews_ShouldReturnNewsResponseDTOList_WhenApiReturnsValidData() {
    // Arrange
    List<Map<String, Object>> mockResponse = createMockNewsResponse();
    String expectedUrl = TEST_URL + "news?category=general&token=" + TEST_KEY;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenReturn(mockResponse);

    // Act
    List<NewsResponseDTO> result = newsService.getMarketNews();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Test Headline 1", result.get(0).getHeadline());
    assertEquals("https://example.com/image1.jpg", result.get(0).getImageUrl());
    assertEquals("https://example.com/article1", result.get(0).getArticleUrl());

    verify(requestHeadersUriSpec).uri(expectedUrl);
  }

  @Test
  void getMarketNews_ShouldReturnEmptyList_WhenApiReturnsEmptyResponse() {
    // Arrange
    List<Map<String, Object>> emptyResponse = Collections.emptyList();
    String expectedUrl = TEST_URL + "news?category=general&token=" + TEST_KEY;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenReturn(emptyResponse);

    // Act
    List<NewsResponseDTO> result = newsService.getMarketNews();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getMarketNews_ShouldThrowRestClientException_WhenApiCallFails() {
    // Arrange
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenThrow(new RuntimeException("API Error"));

    // Act & Assert
    RestClientException exception = assertThrows(RestClientException.class,
            () -> newsService.getMarketNews());

    assertEquals("Failed to fetch news from Finnhub API", exception.getMessage());
    assertNotNull(exception.getCause());
  }

  @Test
  void getCompanyNews_ShouldReturnNewsResponseDTOList_WhenValidTickerProvided() {
    // Arrange
    List<Map<String, Object>> mockResponse = createMockNewsResponse();
    String ticker = "AAPL";
    String fromDate = LocalDate.now().minusMonths(1).toString();
    String toDate = LocalDate.now().toString();
    String expectedUrl = TEST_URL + "company-news?symbol=" + ticker + "&from=" + fromDate + "&to=" + toDate + "&token=" + TEST_KEY;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenReturn(mockResponse);

    // Act
    List<NewsResponseDTO> result = newsService.getCompanyNews(ticker);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Test Headline 1", result.get(0).getHeadline());
    verify(requestHeadersUriSpec).uri(expectedUrl);
  }

  @Test
  void getCompanyNews_ShouldThrowInvalidInputException_WhenTickerIsNull() {
    // Arrange
    String ticker = null;

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> newsService.getCompanyNews(ticker));

    assertEquals("Stock cannot be null or empty", exception.getMessage());
  }

  @Test
  void getCompanyNews_ShouldThrowInvalidInputException_WhenTickerIsEmpty() {
    // Arrange
    String ticker = "";

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> newsService.getCompanyNews(ticker));

    assertEquals("Stock cannot be null or empty", exception.getMessage());
  }

  @Test
  void getCompanyNews_ShouldThrowInvalidInputException_WhenTickerIsWhitespace() {
    // Arrange
    String ticker = "   ";

    // Act & Assert
    InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> newsService.getCompanyNews(ticker));

    assertEquals("Stock cannot be null or empty", exception.getMessage());
  }

  @Test
  void getCompanyNews_ShouldThrowRestClientException_WhenApiCallFails() {
    // Arrange
    String ticker = "AAPL";
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenThrow(new RuntimeException("Network Error"));

    // Act & Assert
    RestClientException exception = assertThrows(RestClientException.class,
            () -> newsService.getCompanyNews(ticker));

    assertEquals("Failed to fetch news from Finnhub API", exception.getMessage());
    assertNotNull(exception.getCause());
  }

  @Test
  void fetchNews_ShouldLimitResultsToSixItems_WhenMoreThanSixItemsReturned() {
    // Arrange
    List<Map<String, Object>> mockResponse = createMockNewsResponseWithMultipleItems(10);
    String expectedUrl = TEST_URL + "news?category=general&token=" + TEST_KEY;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenReturn(mockResponse);

    // Act
    List<NewsResponseDTO> result = newsService.getMarketNews();

    // Assert
    assertNotNull(result);
    assertEquals(6, result.size());
  }

  @Test
  void fetchNews_ShouldSkipNullItems_WhenResponseContainsNullElements() {
    // Arrange
    List<Map<String, Object>> mockResponse = new ArrayList<>();
    mockResponse.add(createNewsItem("Headline 1", "image1.jpg", "url1"));
    mockResponse.add(null); // Null item should be skipped
    mockResponse.add(createNewsItem("Headline 2", "image2.jpg", "url2"));

    String expectedUrl = TEST_URL + "news?category=general&token=" + TEST_KEY;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenReturn(mockResponse);

    // Act
    List<NewsResponseDTO> result = newsService.getMarketNews();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size()); // Should contain only non-null items
    assertEquals("Headline 1", result.get(0).getHeadline());
    assertEquals("Headline 2", result.get(1).getHeadline());
  }

  @Test
  void fetchNews_ShouldReturnEmptyList_WhenApiReturnsNull() {
    // Arrange
    String expectedUrl = TEST_URL + "news?category=general&token=" + TEST_KEY;

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(List.class)).thenReturn(null);

    // Act
    List<NewsResponseDTO> result = newsService.getMarketNews();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // Helper methods for creating test data
  private List<Map<String, Object>> createMockNewsResponse() {
    List<Map<String, Object>> response = new ArrayList<>();
    response.add(createNewsItem("Test Headline 1", "https://example.com/image1.jpg", "https://example.com/article1"));
    response.add(createNewsItem("Test Headline 2", "https://example.com/image2.jpg", "https://example.com/article2"));
    return response;
  }

  private List<Map<String, Object>> createMockNewsResponseWithMultipleItems(int count) {
    List<Map<String, Object>> response = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      response.add(createNewsItem("Headline " + i, "image" + i + ".jpg", "url" + i));
    }
    return response;
  }

  private Map<String, Object> createNewsItem(String headline, String image, String url) {
    Map<String, Object> newsItem = new HashMap<>();
    newsItem.put("headline", headline);
    newsItem.put("image", image);
    newsItem.put("url", url);
    return newsItem;
  }
}