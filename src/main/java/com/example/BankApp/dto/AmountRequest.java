package com.example.BankApp.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class AmountRequest {

  @Min(value = 1, message = "金額は1以上でなければなりません。")
  private int amount;
}
