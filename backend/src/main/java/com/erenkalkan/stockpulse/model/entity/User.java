package com.erenkalkan.stockpulse.model.entity;

import com.erenkalkan.stockpulse.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "watchlists"})
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
  @SequenceGenerator(name = "user_seq", sequenceName = "users_seq", allocationSize = 1)
  @Column(name="id")
  private Long id;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
  @Column(name="first_name", nullable = false)
  private String firstName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Size(max = 50, message = "Email must not exceed 50 characters")
  @Column(name="email", unique = true, nullable = false)
  private String email;

  @Size(max = 100, message = "Password must not exceed 100 characters")
  @Column(name="password", nullable = false)
  private String password;

  @Column(name = "profile_picture")
  private String profilePicture;

  @Builder.Default
  @Column(name = "is_oauth_user", nullable = false)
  private Boolean isOAuthUser = false;

  @NotNull(message = "Role is required")
  @Column(name="role", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Role role = Role.REGULAR_USER;

  @JsonManagedReference
  @Builder.Default
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Watchlist> watchlists = new ArrayList<>();

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

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @Override
  public String getUsername() {
    return email;
  }
}
