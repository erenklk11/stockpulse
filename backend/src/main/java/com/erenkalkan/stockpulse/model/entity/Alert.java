package com.erenkalkan.stockpulse.model.entity;

import com.erenkalkan.stockpulse.model.enums.TriggerType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_stocks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "watchlist")
public class Alert {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank(message = "Stock ticker is required")
  @Pattern(regexp = "^[A-Z]{1,5}$")
  @Column(name = "stock_ticker", nullable = false)
  private String stockTicker;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "trigger_type", nullable = false)
  private TriggerType triggerType;

  @NotNull
  @ManyToOne
  @JsonBackReference
  @JoinColumn(name = "watchlist_id", nullable = false)
  private Watchlist watchlist;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
