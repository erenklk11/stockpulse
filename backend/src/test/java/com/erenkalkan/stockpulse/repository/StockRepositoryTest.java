package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StockRepositoryTest {

  @Autowired
  private StockRepository stockRepository;

  private Stock stock;
  private final String symbol = "AAPL";

  @BeforeEach
  void setUp() {
    stockRepository.deleteAll();

    stock = Stock.builder()
            .companyName("Apple Inc.")
            .symbol(symbol)
            .build();
  }


  @Test
  void findBySymbol_WhenSymbolExists_ShouldReturnStock() {
    // Arrange
    stockRepository.save(stock);

    // Act
    Optional<Stock> result = stockRepository.findBySymbol(symbol);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(symbol, result.get().getSymbol());
  }

  @Test
  void findBySymbol_WhenSymbolDoesNotExist_ShouldReturnEmpty() {
    // Arrange
    stockRepository.save(stock);
    String notExistingSymbol = "INVALID";

    // Act
    Optional<Stock> result = stockRepository.findBySymbol(notExistingSymbol);

    // Assert
    assertTrue(result.isEmpty());
  }


}