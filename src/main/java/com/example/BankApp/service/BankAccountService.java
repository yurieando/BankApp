package com.example.BankApp.service;

import com.example.BankApp.Mapper.BankAccountMapper;
import com.example.BankApp.dto.AccountCreationRequest;
import com.example.BankApp.dto.AdminBankAccountResponse;
import com.example.BankApp.dto.AmountRequest;
import com.example.BankApp.dto.BankAccountResponse;
import com.example.BankApp.exception.ResourceNotFoundException;
import com.example.BankApp.model.AccountLog;
import com.example.BankApp.model.AccountLog.AccountLogStatus;
import com.example.BankApp.model.AccountLog.AccountLogType;
import com.example.BankApp.model.BankAccount;
import com.example.BankApp.model.BankAccount.Role;
import com.example.BankApp.repository.AccountLogRepository;
import com.example.BankApp.repository.BankAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BankAccountService {

  private final BankAccountRepository bankAccountRepository;
  private final AccountLogRepository accountLogRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * すべての口座情報を取得します。
   *
   * @return 口座のリスト
   */
  @Transactional
  public List<AdminBankAccountResponse> getAllAccountsForAdmin() {
    List<BankAccount> accounts = bankAccountRepository.findAll(
        Sort.by(Sort.Direction.DESC, "accountNumber"));
    return accounts.stream()
        .map(BankAccountMapper::toAdminResponse)
        .toList();
  }

  /**
   * 新しい口座を作成します。
   *
   * @param request 口座情報を含むリクエスト
   * @return 作成された口座の情報
   */
  @Transactional
  public BankAccountResponse createAccount(AccountCreationRequest request) {
    String accountNumber = generateSequentialAccountNumber();
    String encoded = passwordEncoder.encode(request.getPassword());

    BankAccount account = new BankAccount(
        accountNumber,
        encoded,
        request.getAccountHolderName(),
        0,
        true,
        Role.ACCOUNT_USER
    );

    bankAccountRepository.save(account);
    AccountLog accountLog = AccountLog.builder()
        .accountLogId(UUID.randomUUID().toString())
        .accountNumber(account.getAccountNumber())
        .accountLogType(AccountLogType.OPEN)
        .amount(0)
        .balanceAfterTransaction(account.getBalance())
        .timestamp(LocalDateTime.now())
        .accountLogStatus(AccountLogStatus.SUCCESS)
        .build();
    accountLogRepository.save(accountLog);
    return BankAccountMapper.toResponse(account);

  }

  /**
   * 口座番号を自動生成します。 このメソッドは、既存の口座番号の最大値を取得し、それに1を加えた値を新しい口座番号として返します。 もし既存の口座がない場合は、"0000001"を返します。
   *
   * @return 新しい口座番号
   */
  @Transactional
  private String generateSequentialAccountNumber() {
    List<BankAccount> allAccounts = bankAccountRepository.findAll(
        Sort.by(Sort.Direction.DESC, "accountNumber"));

    long nextAccountNumber = 1;
    if (!allAccounts.isEmpty()) {
      long max = Long.parseLong(allAccounts.get(0).getAccountNumber());
      nextAccountNumber = max + 1;
    }

    if (nextAccountNumber > 9999999) {
      throw new IllegalStateException("口座番号の上限に達しました。");
    }
    return String.format("%07d", nextAccountNumber);
  }

  /**
   * 口座情報を取得します。
   *
   * @param accountNumber 口座番号
   * @return 指定された口座の情報
   */
  @Transactional
  public BankAccountResponse getBalance(String accountNumber) {
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("口座が存在しません。"));
    return BankAccountMapper.toResponse(account);
  }

  /**
   * 口座に入金を行います。
   *
   * @param accountNumber 口座番号
   * @param amountRequest 入金金額を含むリクエスト
   * @return 入金後の口座情報
   */
  @Transactional
  public BankAccountResponse deposit(String accountNumber, AmountRequest amountRequest) {
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("口座が存在しません。"));

    if (!account.isActive()) {
      throw new IllegalArgumentException("この口座は既に解約されています。");
    }

    account.setBalance(account.getBalance() + amountRequest.getAmount());
    bankAccountRepository.save(account);

    AccountLog accountLog = AccountLog.builder()
        .accountLogId(UUID.randomUUID().toString())
        .accountNumber(account.getAccountNumber())
        .accountLogType(AccountLogType.DEPOSIT)
        .amount(amountRequest.getAmount())
        .balanceAfterTransaction(account.getBalance())
        .timestamp(LocalDateTime.now())
        .accountLogStatus(AccountLogStatus.SUCCESS)
        .build();
    accountLogRepository.save(accountLog);
    return BankAccountMapper.toResponse(account);
  }

  /**
   * 口座から出金を行います。
   *
   * @param accountNumber 口座番号
   * @param amountrequest 出金金額を含むリクエスト
   * @return 出金後の口座情報
   */
  @Transactional
  public BankAccountResponse withdraw(String accountNumber, AmountRequest amountrequest) {
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("口座が存在しません。"));

    if (!account.isActive()) {
      throw new IllegalArgumentException("この口座は既に解約されています。");
    }

    if (amountrequest.getAmount() <= account.getBalance()) {
      account.setBalance(account.getBalance() - amountrequest.getAmount());

      AccountLog accountLog = AccountLog.builder()
          .accountLogId(UUID.randomUUID().toString())
          .accountNumber(account.getAccountNumber())
          .accountLogType(AccountLogType.WITHDRAW)
          .amount(amountrequest.getAmount())
          .balanceAfterTransaction(account.getBalance())
          .timestamp(LocalDateTime.now())
          .accountLogStatus(AccountLogStatus.SUCCESS)
          .build();
      accountLogRepository.save(accountLog);
      bankAccountRepository.save(account);
    } else {
      AccountLog accountLog = AccountLog.builder()
          .accountLogId(UUID.randomUUID().toString())
          .accountNumber(account.getAccountNumber())
          .accountLogType(AccountLogType.WITHDRAW)
          .amount(amountrequest.getAmount())
          .balanceAfterTransaction(account.getBalance())
          .timestamp(LocalDateTime.now())
          .accountLogStatus(AccountLogStatus.FAILED)
          .build();
      accountLogRepository.save(accountLog);
      throw new IllegalArgumentException("残高が不足しています。");
    }
    return BankAccountMapper.toResponse(account);
  }

  /**
   * 口座の解約を行います。
   *
   * @param accountNumber 口座番号
   * @return 口座解約の結果メッセージ
   */
  @Transactional
  public String closeAccount(String accountNumber) {
    BankAccount account = bankAccountRepository.findById(accountNumber)
        .orElseThrow(() -> new ResourceNotFoundException("口座が存在しません。"));
    if (account.getBalance() > 0) {
      throw new IllegalArgumentException("残高があるため、口座を解約できません。");
    }
    if (!account.isActive()) {
      ;
      throw new IllegalArgumentException("既に解約済みの口座です。");
    }
    account.setActive(false);
    bankAccountRepository.save(account);

    AccountLog accountLog = AccountLog.builder()
        .accountLogId(UUID.randomUUID().toString())
        .accountNumber(accountNumber)
        .accountLogType(AccountLogType.CLOSE)
        .amount(0)
        .balanceAfterTransaction(0)
        .timestamp(LocalDateTime.now())
        .accountLogStatus(AccountLogStatus.SUCCESS)
        .build();
    accountLogRepository.save(accountLog);

    return "口座解約が完了しました。口座番号：" + accountNumber;
  }
}
