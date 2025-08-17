package com.erenkalkan.stockpulse.repository;

import com.erenkalkan.stockpulse.model.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
