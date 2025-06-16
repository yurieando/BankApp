package com.example.BankApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountCreationRequest {

  @NotBlank(message = "口座名義は必須です。")
  private String accountHolderName;
}
