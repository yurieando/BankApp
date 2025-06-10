package com.example.BankApp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class BankAccount {
    private String accountNumber;
    private String accountHolderName;
    private double balance;
}
