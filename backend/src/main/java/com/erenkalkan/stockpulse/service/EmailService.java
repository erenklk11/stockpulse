package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import com.erenkalkan.stockpulse.model.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender sender;

  @Value("${EMAIL_FROM}")
  private String from;

  @Value("${APP_URL}")
  private String appUrl;


  public void sendVerificationEmail(String to, VerificationToken verificationToken) {

    try {
      SimpleMailMessage mail = new SimpleMailMessage();
      mail.setFrom(from);
      mail.setTo(to);

      if (TokenType.PASSWORD_RESET.equals(verificationToken.getTokenType())) {
        mail.setSubject("Reset Your Password");
      }

      mail.setText("Please click the link below to verify your email address:\n\n" +
              appUrl + "/verify-email?token=" + verificationToken.getToken() + "\n\n" +
              "This link will expire in 1 hours.");

      sender.send(mail);
    }
    catch(Exception e) {
      throw new MailSendException("Failed to send verification email", e);
    }
  }




}
