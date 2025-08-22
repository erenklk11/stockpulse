package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

  Optional<Stock> findBySymbol(String symbol);
}
