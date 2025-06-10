package com.example.BankApp;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {
    private String transactionId;
    private String accountNumber;
    private TransactionType transactionType;
    private double amount;
    private double balanceAfterTransaction;
    private LocalDateTime timestamp;
    public TransactionStatus transactionStatus;

    public enum TransactionType {
      DEPOSIT,
      WITHDRAW
    }
    public enum TransactionStatus {
      SUCCESS,
      FAILED
    }
}
