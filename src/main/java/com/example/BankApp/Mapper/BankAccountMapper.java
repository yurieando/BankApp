package com.example.BankApp.Mapper;

import static com.example.BankApp.util.MoneyFormat.yen;

import com.example.BankApp.dto.AdminBankAccountResponse;
import com.example.BankApp.dto.BankAccountResponse;
import com.example.BankApp.model.BankAccount;

public class BankAccountMapper {

  // 通常表示（メッセージなし）
  public static BankAccountResponse toResponse(BankAccount a) {
    return BankAccountResponse.builder()
        .accountNumber(a.getAccountNumber())
        .accountHolderName(a.getAccountHolderName())
        .balance(yen(a.getBalance()))
        .build();
  }

  // メッセージ付き
  public static BankAccountResponse toResponse(BankAccount a, String message) {
    return BankAccountResponse.builder()
        .message(message)
        .accountNumber(a.getAccountNumber())
        .accountHolderName(a.getAccountHolderName())
        .balance(yen(a.getBalance()))
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
