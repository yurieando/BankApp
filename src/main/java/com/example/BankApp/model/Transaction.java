package com.example.BankApp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Transaction {

  @Id
  private String transactionId;

  private String accountNumber;

  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;

  private double amount;

  private double balanceAfterTransaction;

  private LocalDateTime timestamp;

  @Enumerated(EnumType.STRING)
  private TransactionStatus transactionStatus;

  public enum TransactionType {
    DEPOSIT, WITHDRAW, OPEN, CLOSE
  }

  public enum TransactionStatus {
    SUCCESS, FAILED
  }
}
