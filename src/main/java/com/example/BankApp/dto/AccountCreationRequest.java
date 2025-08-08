package com.example.BankApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AccountCreationRequest {

  @NotBlank(message = "口座名義は必須です。")
  private String accountHolderName;
}
