package com.erenkalkan.stockpulse.model.entity;

import com.erenkalkan.stockpulse.model.enums.TokenType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "token_type", nullable = false)
  @NotNull
  private TokenType tokenType;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  @NotNull
  private User user;

  @Column(name = "token", nullable = false)
  @NotNull
  private String token;

  @Transient
  @NotNull
  private Long expirationMs;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "expires_on", nullable = false)
  private LocalDateTime expiresOn;

  @Column(name = "used", nullable = false)
  @Builder.Default
  private Boolean used = false;

  @PrePersist
  public void onCreate() {
    createdAt = LocalDateTime.now();
    expiresOn = createdAt.plus(Duration.ofMillis(expirationMs));
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresOn);
  }

  public boolean isValid() {
    return !used && !isExpired();
  }

}
