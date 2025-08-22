package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

  public List<Alert> findAllByStock(Stock stock);
}
