package com.example.BankApp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class BankAccount {

  @Id
  private String accountNumber;
  private String accountHolderName;
  private double balance;
}
