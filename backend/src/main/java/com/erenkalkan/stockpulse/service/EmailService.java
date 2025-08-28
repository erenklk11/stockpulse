package com.erenkalkan.stockpulse.service;

import com.erenkalkan.stockpulse.model.entity.Alert;
import com.erenkalkan.stockpulse.model.entity.User;
import com.erenkalkan.stockpulse.model.entity.VerificationToken;
import com.erenkalkan.stockpulse.model.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender sender;

  @Value("${app.email.from}")
  private String from;

  @Value("${app.url}")
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

  public void sendAlertEmail(Alert alert) {

    User user = alert.getWatchlist().getUser();
    String to = user.getEmail();

    try {
      SimpleMailMessage mail = new SimpleMailMessage();
      mail.setFrom(from);
      mail.setTo(to);

      mail.setSubject(String.format("Your Alert for %s has been triggered!", alert.getStock().getSymbol()));

      mail.setText(String.format(Locale.US,"""
              Hey %s,
              We would like to inform you about your Alert for the stock: %s
              Price is now %s your target of %.2f
              """, user.getFirstName(),
              alert.getStock().getSymbol(),
              alert.getCondition().toString().toLowerCase(), alert.getTargetValue().doubleValue()));

      sender.send(mail);
    }
    catch(Exception e) {
      throw new MailSendException("Failed to send alert trigger email", e);
    }
  }


}
