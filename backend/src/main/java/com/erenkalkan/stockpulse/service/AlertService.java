package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

  private final AlertRepository alertRepository;
}
