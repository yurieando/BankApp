package com.example.BankApp.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonPropertyOrder({"message", "accountNumber", "accountHolderName", "balance"})
public class BankAccountResponse {

  private String message;
  private String accountNumber;
  private String accountHolderName;
  private String balance;
}
