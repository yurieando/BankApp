package com.example.BankApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

  @NotBlank(message = "ログインIDは必須です")
  private String loginId;   // adminId または accountNumber
  @NotBlank(message = "パスワードは必須です")
  private String password;
}
