package com.example.BankApp.service;

import com.example.BankApp.dto.AccountCreationRequest;
import com.example.BankApp.dto.AmountRequest;
import com.example.BankApp.model.BankAccount;
import com.example.BankApp.model.Transaction;
import com.example.BankApp.model.Transaction.TransactionStatus;
import com.example.BankApp.model.Transaction.TransactionType;
import com.example.BankApp.repository.BankAccountRepository;
import com.example.BankApp.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankAccountService {

  private final BankAccountRepository bankAccountRepository;
  private final TransactionRepository transactionRepository;

  /**
   * 新しい口座を作成します。
   *
   * @param request 口座情報を含むリクエスト
   * @return 作成された口座の情報
   */
  public BankAccount createAccount(AccountCreationRequest request) {
    String accountNumber = generateSequentialAccountNumber();
    BankAccount account = new BankAccount(accountNumber, request.getAccountHolderName(), 0.0, true);
    bankAccountRepository.save(account);

    Transaction transaction = Transaction.builder()
        .transactionId(UUID.randomUUID().toString())
        .accountNumber(account.getAccountNumber())
        .transactionType(TransactionType.OPEN)
        .amount(0)
        .balanceAfterTransaction(account.getBalance())
        .timestamp(LocalDateTime.now())
        .transactionStatus(TransactionStatus.SUCCESS)
        .build();
    transactionRepository.save(transaction);
    return account;
  }

  /**
   * 口座番号を自動生成します。 このメソッドは、既存の口座番号の最大値を取得し、それに1を加えた値を新しい口座番号として返します。 もし既存の口座がない場合は、"0000001"を返します。
   *
   * @return 新しい口座番号
   */
  private String generateSequentialAccountNumber() {
    List<BankAccount> allAccounts = bankAccountRepository.findAll(
        Sort.by(Sort.Direction.DESC, "accountNumber"));
    if (allAccounts.isEmpty()) {
      return "0000001";
    }
    long max = Long.parseLong(allAccounts.get(0).getAccountNumber());
    return String.format("%07d", max + 1);
  }

  /**
   * 口座情報を取得します。
   *
   * @param accountNumber 口座番号
   * @return 指定された口座の情報
   */
  public BankAccount getBalance(String accountNumber) {
    return bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new RuntimeException("口座が存在しません。"));
  }

  public BankAccount deposit(String accountNumber, AmountRequest amountRequest) {
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new IllegalArgumentException("口座が存在しません。"));

    account.setBalance(account.getBalance() + amountRequest.getAmount());
    bankAccountRepository.save(account);

    Transaction transaction = Transaction.builder()
        .transactionId(UUID.randomUUID().toString())
        .accountNumber(account.getAccountNumber())
        .transactionType(TransactionType.DEPOSIT)
        .amount(amountRequest.getAmount())
        .balanceAfterTransaction(account.getBalance())
        .timestamp(LocalDateTime.now())
        .transactionStatus(TransactionStatus.SUCCESS)
        .build();
    transactionRepository.save(transaction);
    return account;
  }

  public BankAccount withdraw(String accountNumber, AmountRequest amountrequest) {
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new IllegalArgumentException("口座が存在しません。"));

    if (amountrequest.getAmount() <= account.getBalance()) {
      account.setBalance(account.getBalance() - amountrequest.getAmount());

      Transaction transaction = Transaction.builder()
          .transactionId(UUID.randomUUID().toString())
          .accountNumber(account.getAccountNumber())
          .transactionType(TransactionType.WITHDRAW)
          .amount(amountrequest.getAmount())
          .balanceAfterTransaction(account.getBalance())
          .timestamp(LocalDateTime.now())
          .transactionStatus(TransactionStatus.SUCCESS)
          .build();
      transactionRepository.save(transaction);
      bankAccountRepository.save(account);
    } else {
      Transaction transaction = Transaction.builder()
          .transactionId(UUID.randomUUID().toString())
          .accountNumber(account.getAccountNumber())
          .transactionType(TransactionType.WITHDRAW)
          .amount(amountrequest.getAmount())
          .balanceAfterTransaction(account.getBalance())
          .timestamp(LocalDateTime.now())
          .transactionStatus(TransactionStatus.FAILED)
          .build();
      transactionRepository.save(transaction);
      throw new IllegalArgumentException("残高不足です。");
    }
    return account;
  }

  /**
   * 口座の削除を行います。
   *
   * @param accountNumber 口座番号
   * @return 口座削除の結果メッセージ
   */
  public String closeAccount(String accountNumber) {
    if (accountNumber == null || accountNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("口座番号が無効です。");
    }
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new IllegalArgumentException("口座が存在しません。"));
    if (account.getBalance() > 0) {
      throw new IllegalArgumentException("残高があるため、口座を解約できません。");
    }
    if (!account.isActive()) {
      ;
      throw new IllegalArgumentException("既に解約済みの口座です。");
    }
    account.setActive(false);
    bankAccountRepository.save(account);

    Transaction transaction = Transaction.builder()
        .transactionId(UUID.randomUUID().toString())
        .accountNumber(accountNumber)
        .transactionType(TransactionType.CLOSE)
        .amount(0)
        .balanceAfterTransaction(0)
        .timestamp(LocalDateTime.now())
        .transactionStatus(TransactionStatus.SUCCESS)
        .build();
    transactionRepository.save(transaction);

    return "口座解約が完了しました。口座番号：" + accountNumber;
  }
}
