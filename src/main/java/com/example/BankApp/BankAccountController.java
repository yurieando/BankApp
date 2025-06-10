package com.example.BankApp;

import BankAccountRepository.BankAccountRepository;
import TransactionRepository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class BankAccountController {

  private final BankAccountRepository bankAccountRepository;
  private final TransactionRepository transactionRepository;

  public BankAccountController(
      BankAccountRepository bankAccountRepository,
      TransactionRepository transactionRepository) {
    this.bankAccountRepository = bankAccountRepository;
    this.transactionRepository = transactionRepository;
  }

  /*
   *　口座の検索をします。
   * @param accountNumber 口座番号
   * @return 指定された口座の情報
   */
  @GetMapping("/account")
  public BankAccount getAccount(@RequestParam String accountNumber) {
    return bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new IllegalArgumentException("口座が存在しません。"));
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
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new IllegalArgumentException("口座が存在しません。"));

    account.setBalance(account.getBalance() + request.getAmount());
    bankAccountRepository.save(account);

    Transaction transaction = Transaction.builder()
        .transactionId(UUID.randomUUID().toString())
        .accountNumber(account.getAccountNumber())
        .transactionType(Transaction.TransactionType.DEPOSIT)
        .amount(request.getAmount())
        .balanceAfterTransaction(account.getBalance())
        .timestamp(LocalDateTime.now())
        .transactionStatus(Transaction.TransactionStatus.SUCCESS)
        .build();
    transactionRepository.save(transaction);

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
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new IllegalArgumentException("口座が存在しません。"));

    if (request.getAmount() <= account.getBalance()) {
      account.setBalance(account.getBalance() - request.getAmount());

      Transaction transaction = Transaction.builder()
          .transactionId(UUID.randomUUID().toString())
          .accountNumber(account.getAccountNumber())
          .transactionType(Transaction.TransactionType.WITHDRAW)
          .amount(request.getAmount())
          .balanceAfterTransaction(account.getBalance())
          .timestamp(LocalDateTime.now())
          .transactionStatus(Transaction.TransactionStatus.SUCCESS)
          .build();
      transactionRepository.save(transaction);
      bankAccountRepository.save(account);
    } else {
      Transaction transaction = Transaction.builder()
          .transactionId(UUID.randomUUID().toString())
          .accountNumber(account.getAccountNumber())
          .transactionType(Transaction.TransactionType.WITHDRAW)
          .amount(request.getAmount())
          .balanceAfterTransaction(account.getBalance())
          .timestamp(LocalDateTime.now())
          .transactionStatus(Transaction.TransactionStatus.FAILED)
          .build();
      transactionRepository.save(transaction);
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
    return transactionRepository.findAll();
  }

  /*
   * 指定された口座の取引履歴を取引タイプでフィルタリングして取得します。
   * @param accountNumber 口座番号
   * @param transactionType 取引タイプ（入金、出金）, nullの場合は全ての取引を取得
   * @return 指定された口座の取引履歴のリスト
   */
  @GetMapping("/accountTransactions")
  public List<Transaction> getAccountTransactions(
      @RequestParam String accountNumber,
      @RequestParam(required = false) Transaction.TransactionType transactionType) {

    if (transactionType != null) {
      return transactionRepository.findByAccountNumberAndTransactionType(accountNumber,
          transactionType);
    } else {
      return transactionRepository.findByAccountNumber(accountNumber);
    }
  }
}
