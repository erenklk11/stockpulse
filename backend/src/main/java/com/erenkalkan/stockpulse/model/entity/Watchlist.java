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

  @Size(max = 50, message = "Maximum 50 stock tickers allowed per watchlist")
  @ElementCollection
  @CollectionTable(name = "watchlist_stock_tickers", joinColumns = @JoinColumn(name = "watchlist_id"))
  @Column(name = "stock_ticker")
  private List<@NotBlank @Pattern(regexp = "^[A-Z]{1,5}$", message = "Stock ticker must be 1-5 uppercase letters") String> stockTickers;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
