package com.example.BankApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

  @NotBlank
  private String loginId; // adminId または accountNumber
  @NotBlank
  private String password;
}
