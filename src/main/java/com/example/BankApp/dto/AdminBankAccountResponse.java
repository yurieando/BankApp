package com.example.BankApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AdminBankAccountResponse {

  private String accountNumber;
  private String accountHolderName;
  private double balance;
  private boolean isActive;
}
