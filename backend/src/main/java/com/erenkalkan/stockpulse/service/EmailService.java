package com.erenkalkan.stockpulse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender sender;

  @Value("${EMAIL_FROM}")
  private String fromEmail;

  @Value("${APP_URL}")
  private String appUrl;




}
