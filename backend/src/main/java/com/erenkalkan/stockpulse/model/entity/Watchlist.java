package com.erenkalkan.stockpulse.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "watchlists")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
public class Watchlist {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "watchlist_seq")
  @SequenceGenerator(name = "watchlist_seq", sequenceName = "watchlists_seq", allocationSize = 1)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Watchlist name is required")
  @Size(min = 3, message = "Minimum watchlist name size is 3 characters")
  @Column(name = "watchlist_name")
  private String watchlistName;

  @NotNull(message = "User is required")
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
  @Size(max = 50, message = "Maximum 50 stock tickers allowed per watchlist")
  private List<Alert> alerts;

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
