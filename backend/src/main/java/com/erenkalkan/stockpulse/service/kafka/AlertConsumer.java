package com.erenkalkan.stockpulse.service.kafka;

import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.service.EmailService;
import com.erenkalkan.stockpulse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class AlertConsumer {

  private final NotificationService notificationService;
  private final EmailService emailService;

  @KafkaListener(topics = "${app.kafka.topics.alertTriggers:alert-triggers}", groupId = "notification-group")
  public void consumeAlertTrigger(Alert alert) {

    log.info("Consumed triggered alert for user {}: {}", alert.getWatchlist().getUser(), alert.getStock().getSymbol());

    notificationService.sendAlertNotification(alert);
    emailService.sendAlertEmail(alert);
  }
}

