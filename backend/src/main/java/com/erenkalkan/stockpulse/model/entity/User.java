package com.erenkalkan.stockpulse.model.entity;

import com.erenkalkan.stockpulse.model.enums.Role;
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
@Table(name="users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "watchlist"})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name="id")
  private Long id;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  @Column(name="first_name", nullable = false)
  private String firstName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  @Column(name="email", unique = true, nullable = false)
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Column(name="password", nullable = false)
  private String password;

  @NotNull(message = "Role is required")
  @Column(name="role", nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role = Role.REGULAR_USER;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Watchlist> watchlist;

  @Column(name = "is_verified")
  private boolean isVerified = false;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
