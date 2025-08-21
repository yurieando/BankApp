package com.example.BankApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AdminUserResponse {

  private String adminId;
  private String adminUserName;
}
