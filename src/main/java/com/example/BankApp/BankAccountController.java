package com.example.BankApp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class BankAccountController {

  Map<String, BankAccount> accountMap = new HashMap<>();
  List<Transaction> transactions = new ArrayList<>();
  // アカウント番号をキーに、トランザクションリストをマップで保持
  private Map<String, List<Transaction>> transactionMap = new HashMap<>();

  /*
   *　口座の検索をします。
   * @param accountNumber 口座番号
   * @return 指定された口座の情報
   */
  @GetMapping("/account")
  public BankAccount getAccount(@RequestParam String accountNumber) {
    BankAccount account = accountMap.get(accountNumber);
    if (account == null) {
      throw new IllegalArgumentException("指定された口座は存在しません。");
    }
    return account;
  }

  /*
   * 口座に入金をします。
   * @param accountNumber 口座番号
   * @param request 入金金額を含むリクエストボディ
   * @return 入金後の口座情報
   */

  @PostMapping("/deposit")
  public BankAccount deposit(@RequestParam String accountNumber,
      @RequestBody AmountRequest request) {
    BankAccount account = accountMap.get(accountNumber);

    if (account == null) {
      throw new IllegalArgumentException("口座が存在しません。");
    }

    account.setBalance(account.getBalance() + request.getAmount());

    Transaction transaction = new Transaction(
        UUID.randomUUID().toString(),
        account.getAccountNumber(),
        Transaction.TransactionType.DEPOSIT,
        request.getAmount(),
        account.getBalance(),
        LocalDateTime.now(),
        Transaction.TransactionStatus.SUCCESS
    );
    transactions.add(transaction);
    // トランザクションをアカウント番号でマップに追加
    transactionMap.computeIfAbsent(account.getAccountNumber(), k -> new ArrayList<>())
        .add(transaction);
    return account;
  }

  /*
   * 口座から出金をします。
   * @param accountNumber 口座番号
   * @param request 出金金額を含むリクエストボディ
   * @return 出金後の口座情報
   */
  @PostMapping("/withdraw")
  public BankAccount withdraw(@RequestParam String accountNumber,
      @RequestBody AmountRequest request) {
    BankAccount account = accountMap.get(accountNumber);

    if (account == null) {
      throw new IllegalArgumentException("口座が存在しません。");
    }

    if (request.getAmount() <= account.getBalance()) {
      account.setBalance(account.getBalance() - request.getAmount());

      Transaction transaction = new Transaction(
          UUID.randomUUID().toString(),
          account.getAccountNumber(),
          Transaction.TransactionType.WITHDRAW,
          request.getAmount(),
          account.getBalance(),
          LocalDateTime.now(),
          Transaction.TransactionStatus.SUCCESS
      );
      transactions.add(transaction);
      transactionMap.computeIfAbsent(account.getAccountNumber(), k -> new ArrayList<>())
          .add(transaction);

    } else {
      Transaction transaction = new Transaction(
          UUID.randomUUID().toString(),
          account.getAccountNumber(),
          Transaction.TransactionType.WITHDRAW,
          request.getAmount(),
          account.getBalance(),
          LocalDateTime.now(),
          Transaction.TransactionStatus.FAILED
      );
      transactions.add(transaction);
      transactionMap.computeIfAbsent(account.getAccountNumber(), k -> new ArrayList<>())
          .add(transaction);

      throw new IllegalArgumentException("残高が不足しています。");
    }
    return account;
  }

  /*
   * 全ての取引履歴を取得します。
   * @return 取引履歴のリスト
   */
  @GetMapping("/allTransactions")
  public List<Transaction> getAllTransactions() {
    return transactions;
  }

  /*
   * 指定された口座の取引履歴を取得します。
   * @param accountNumber 口座番号
   * @return 指定された口座の取引履歴のリスト
   */
  @GetMapping("/accountTransactions")
  public List<Transaction> getAccountTransactions(@RequestParam String accountNumber) {
    return transactionMap.getOrDefault(accountNumber, new ArrayList<>());
  }
}
