package com.example.BankApp.model;

import jakarta.persistence.Column;
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

public class AccountLog {

  @Id
  private String accountLogId;

  private String accountNumber;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private AccountLogType accountLogType;

  private int amount;

  private int balanceAfterTransaction;

  private LocalDateTime timestamp;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private AccountLogStatus accountLogStatus;

  public enum AccountLogType {
    DEPOSIT, WITHDRAW, OPEN, CLOSE
  }

  public enum AccountLogStatus {
    SUCCESS, FAILED
  }
}
