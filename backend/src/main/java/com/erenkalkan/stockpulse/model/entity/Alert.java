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

  @NotNull
  @ManyToOne
  @JoinColumn(name = "stock", nullable = false)
  private Stock stock;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "trigger_type", nullable = false)
  private TriggerType triggerType;

  @NotNull
  @Column(name = "alert_value", nullable = false)
  private Long alertValue;

  @Column(name = "target_value")
  private Long targetValue;

  @Builder.Default
  @Column(name = "is_triggered", nullable = false)
  private boolean isTriggered = false;

  @NotNull
  @ManyToOne
  @JsonBackReference
  @JoinColumn(name = "watchlist_id", nullable = false)
  private Watchlist watchlist;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder.Default
  @Column(name = "triggered_at")
  private LocalDateTime triggeredAt = null;

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
