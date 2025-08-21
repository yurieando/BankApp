package com.example.BankApp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor

public class AdminUser {

  @Id
  @Column(unique = true, nullable = false)
  @Pattern(regexp = "^admin\\d{3}$", message = "ログインIDは 'admin' に続く3桁の数字である必要があります")
  private String adminId; // 例: admin001

  @Column(nullable = false)
  private String password;

  private String adminUserName;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  public enum Role {
    ADMIN,
    ACCOUNT_USER
  }
}
