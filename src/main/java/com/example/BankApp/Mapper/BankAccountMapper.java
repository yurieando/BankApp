package com.example.BankApp.Mapper;

import com.example.BankApp.dto.AdminBankAccountResponse;
import com.example.BankApp.dto.BankAccountResponse;
import com.example.BankApp.model.BankAccount;

public class BankAccountMapper {

  public static BankAccountResponse toResponse(BankAccount account) {
    return BankAccountResponse.builder()
        .accountNumber(account.getAccountNumber())
        .accountHolderName(account.getAccountHolderName())
        .balance(account.getBalance())
        .build();
  }

  public static AdminBankAccountResponse toAdminResponse(BankAccount account) {
    return AdminBankAccountResponse.builder()
        .accountNumber(account.getAccountNumber())
        .accountHolderName(account.getAccountHolderName())
        .balance(account.getBalance())
        .isActive(account.isActive())
        .build();
  }
}
