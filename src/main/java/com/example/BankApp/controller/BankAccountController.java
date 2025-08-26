package com.example.BankApp.controller;

import com.example.BankApp.dto.AccountCreationRequest;
import com.example.BankApp.dto.AdminBankAccountResponse;
import com.example.BankApp.dto.AmountRequest;
import com.example.BankApp.dto.BankAccountResponse;
import com.example.BankApp.exception.ResourceNotFoundException;
import com.example.BankApp.model.AccountLog;
import com.example.BankApp.repository.AccountLogRepository;
import com.example.BankApp.service.BankAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Validated

public class BankAccountController {

  private final AccountLogRepository accountLogRepository;
  private final BankAccountService bankAccountService;

  /*
   * 口座の一覧を取得します。
   * @return 口座のリスト
   */
  @GetMapping("/admin/accounts")
  public List<AdminBankAccountResponse> getAllAccounts() {
    return bankAccountService.getAllAccountsForAdmin();
  }

  /*
   * 新しい口座を開設します。
   * @param request 口座情報を含むリクエストボディ
   * @return 開設された口座の情報
   */
  @PostMapping("/createAccount")
  public BankAccountResponse createAccount(@Valid @RequestBody AccountCreationRequest request) {
    return bankAccountService.createAccount(request);
  }

  /*
   *　残高照会をします。
   * @param accountNumber 口座番号
   * @return 指定された口座の情報(残高を含む)
   */
  @GetMapping("/balance/{accountNumber}")
  public BankAccountResponse getBalance(
      @PathVariable @Pattern(regexp = "\\d{7}", message = "口座番号は7桁の数字である必要があります")
      String accountNumber) {
    return bankAccountService.getBalance(accountNumber);
  }

  /*
   * 口座に入金をします。
   * @param accountNumber 口座番号
   * @param amountRequest 入金金額を含むリクエストボディ
   * @return 入金後の口座情報
   */
  @PostMapping("/deposit/{accountNumber}")
  public BankAccountResponse deposit(
      @PathVariable @Pattern(regexp = "\\d{7}", message = "口座番号は7桁の数字である必要があります")
      String accountNumber,
      @Valid @RequestBody AmountRequest amountRequest) {
    return bankAccountService.deposit(accountNumber, amountRequest);
  }

  /*
   * 口座から出金をします。
   * @param accountNumber 口座番号
   * @param amountRequest 出金金額を含むリクエストボディ
   * @return 出金後の口座情報
   */
  @PostMapping("/withdraw/{accountNumber}")
  public BankAccountResponse withdraw(
      @PathVariable @Pattern(regexp = "\\d{7}", message = "口座番号は7桁の数字である必要があります")
      String accountNumber,
      @Valid @RequestBody AmountRequest amountRequest) {
    return bankAccountService.withdraw(accountNumber, amountRequest);
  }

  /*

   * 指定された口座の取引履歴を取引タイプでフィルタリングして取得します。
   * @param accountNumber 口座番号
   * @param accountLogType 取引タイプ（入金、出金）, nullの場合は全ての取引を取得
   * @return 指定された口座の取引履歴のリスト
   */
  @GetMapping("/accountLog/{accountNumber}")

  public List<AccountLog> getAccountLog(
      @PathVariable @Pattern(regexp = "\\d{7}", message = "口座番号は7桁の数字である必要があります")
      String accountNumber,
      @RequestParam(required = false) AccountLogType accountLogType) {

    List<AccountLog> accountLogs;

    if (accountLogType != null) {
      accountLogs = accountLogRepository.findByAccountNumberAndAccountLogType(accountNumber,
          accountLogType);

    } else {
      accountLogs = accountLogRepository.findByAccountNumber(accountNumber);
    }

    if (accountLogs.isEmpty()) {

      throw new ResourceNotFoundException("指定された口座のログが存在しません。");
    }
    return accountLogs;
  }

  /*
   * 口座解約を行います。
   * @param accountNumber 口座番号
   * @return 解約された口座の情報
   */
  @PostMapping("/closeAccount/{accountNumber}")
  public String closeAccount(
      @PathVariable @Pattern(regexp = "\\d{7}", message = "口座番号は7桁の数字である必要があります")
      String accountNumber) {
    return bankAccountService.closeAccount(accountNumber);
  }
}
