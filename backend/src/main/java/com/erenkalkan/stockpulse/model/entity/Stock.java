package com.erenkalkan.stockpulse.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "alert")
public class Stock {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Pattern(regexp = "^[A-Z]{1,5}$")
  @Column(name = "symbol", nullable = false)
  private String symbol;

  @Column(name = "company_name", nullable = false)
  private String companyName;
}
