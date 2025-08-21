package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
