package com.example.BankApp.controller;

import com.example.BankApp.dto.AccountCreationRequest;
import com.example.BankApp.dto.AmountRequest;
import com.example.BankApp.dto.BankAccountResponse;
import com.example.BankApp.model.Transaction;
import com.example.BankApp.repository.TransactionRepository;
import com.example.BankApp.service.BankAccountService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController

public class BankAccountController {

  private final TransactionRepository transactionRepository;
  private final BankAccountService bankAccountService;

  /*
   * 新しい口座を作成します。
   * @param request 口座情報を含むリクエストボディ
   * @return 作成された口座の情報
   */
  @PostMapping("/createAccount")
  public BankAccountResponse createAccount(@RequestBody @Valid AccountCreationRequest request) {
    return bankAccountService.createAccount(request);
  }

  /*
   *　残高の照会をします。
   * @param accountNumber 口座番号
   * @return 指定された口座の情報(残高を含む)
   */
  @GetMapping("/balance/{accountNumber}")
  public BankAccountResponse getBalance(@PathVariable String accountNumber) {
    return bankAccountService.getBalance(accountNumber);
  }

  /*
   * 口座に入金をします。
   * @param accountNumber 口座番号
   * @param amountRequest 入金金額を含むリクエストボディ
   * @return 入金後の口座情報
   */
  @PostMapping("/deposit/{accountNumber}")
  public BankAccountResponse deposit(@PathVariable String accountNumber,
      @RequestBody AmountRequest amountRequest) {
    return bankAccountService.deposit(accountNumber, amountRequest);
  }

  /*
   * 口座から出金をします。
   * @param accountNumber 口座番号
   * @param amountRequest 出金金額を含むリクエストボディ
   * @return 出金後の口座情報
   */
  @PostMapping("/withdraw/{accountNumber}")
  public BankAccountResponse withdraw(@PathVariable String accountNumber,
      @RequestBody AmountRequest amountRequest) {
    return bankAccountService.withdraw(accountNumber, amountRequest);
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
  @GetMapping("/accountTransactions/{accountNumber}")
  public List<Transaction> getAccountTransactions(
      @PathVariable String accountNumber,
      @RequestParam(required = false) Transaction.TransactionType transactionType) {

    if (transactionType != null) {
      return transactionRepository.findByAccountNumberAndTransactionType(accountNumber,
          transactionType);
    } else {
      return transactionRepository.findByAccountNumber(accountNumber);
    }
  }

  /*
   * 口座解約を行います。
   * @param accountNumber 口座番号
   * @return 解約された口座の情報
   */
  @PostMapping("/closeAccount/{accountNumber}")
  public String closeAccount(@PathVariable String accountNumber) {
    return bankAccountService.closeAccount(accountNumber);
  }
}
