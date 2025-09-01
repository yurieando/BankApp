package com.example.BankApp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;


@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

  @Mock
  PasswordEncoder passwordEncoder;
  @Mock
  private BankAccountRepository bankAccountRepository;
  @Mock
  private AccountLogRepository accountLogRepository;

  @InjectMocks
  private BankAccountService bankAccountService;

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void setupSecurityContext() {
    var auth = new UsernamePasswordAuthenticationToken(
        "0000001", // ログイン中のユーザーID（口座番号と同じ）
        null,
        List.of(new SimpleGrantedAuthority("ROLE_ACCOUNT_USER"))
    );
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void 口座一覧取得_正常系_リポジトリが正しく呼び出せていること() {
    List<BankAccount> accountList = new ArrayList<>();
    when(bankAccountRepository.findAll(Sort.by(Sort.Direction.DESC, "accountNumber")))
        .thenReturn(accountList);

    bankAccountService.getAllAccountsForAdmin();

    verify(bankAccountRepository).findAll(Sort.by(Sort.Direction.DESC, "accountNumber"));
  }

  @Test
  void 口座一覧取得_正常系_リポジトリからのデータが正しく変換されていること() {
    BankAccount account = new BankAccount("0000001", "password", "テスト氏名", 1000, true,
        Role.ACCOUNT_USER);
    List<BankAccount> accountList = List.of(account);

    when(bankAccountRepository.findAll(Sort.by(Sort.Direction.DESC, "accountNumber")))
        .thenReturn(accountList);

    List<AdminBankAccountResponse> response = bankAccountService.getAllAccountsForAdmin();

    assertThat(response).hasSize(1);
    AdminBankAccountResponse res = response.get(0);
    assertThat(res.getAccountNumber()).isEqualTo("0000001");
    assertThat(res.getAccountHolderName()).isEqualTo("テスト氏名");
    assertThat(res.getBalance()).isEqualTo(1000);
    assertThat(res.isActive()).isTrue();
  }

  @Test
  void 口座開設_正常系_リポジトリが正しく呼び出され保存内容も正しいこと() {
    AccountCreationRequest request = new AccountCreationRequest("テスト氏名", "password123");

    when(passwordEncoder.encode("password123")).thenReturn("ENCODED");

    when(bankAccountRepository.save(any(BankAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0)); // 保存された値をそのまま返す

    bankAccountService.createAccount(request);

    ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
    verify(bankAccountRepository).save(captor.capture());

    BankAccount savedAccount = captor.getValue();

    assertThat(savedAccount.getAccountHolderName()).isEqualTo("テスト氏名");
    assertThat(savedAccount.getBalance()).isEqualTo(0);
    assertThat(savedAccount.isActive()).isTrue();
    assertThat(savedAccount.getAccountNumber()).matches("\\d{7}");
  }

  @Test
  void 口座開設_正常系_口座番号が連番で生成されていること() {
    AccountCreationRequest request = new AccountCreationRequest("テスト氏名", "password123");

    BankAccount existingAccount = new BankAccount("0000001", "password", "既存氏名", 1000, true,
        Role.ACCOUNT_USER);

    when(passwordEncoder.encode("password123")).thenReturn("ENCODED");

    when(bankAccountRepository.findAll(Sort.by(Sort.Direction.DESC, "accountNumber")))
        .thenReturn(List.of(existingAccount));

    BankAccountResponse createdAccount = bankAccountService.createAccount(request);

    assertThat(createdAccount.getAccountNumber()).isEqualTo("0000002");
  }

  @Test
  void 口座開設_正常系_口座開設時のトランザクションが正しく保存されていること() {
    AccountCreationRequest request = new AccountCreationRequest("テスト氏名", "password123");

    when(passwordEncoder.encode("password123")).thenReturn("ENCODED");

    when(bankAccountRepository.save(any(BankAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    bankAccountService.createAccount(request);

    ArgumentCaptor<AccountLog> captor = ArgumentCaptor.forClass(AccountLog.class);
    verify(accountLogRepository).save(captor.capture());

    AccountLog savedAccountLog = captor.getValue();

    assertThat(savedAccountLog.getAccountLogType()).isEqualTo(AccountLogType.OPEN);
    assertThat(savedAccountLog.getAccountLogStatus()).isEqualTo(
        AccountLogStatus.SUCCESS);
    assertThat(savedAccountLog.getAmount()).isEqualTo(0);
    assertThat(savedAccountLog.getBalanceAfterTransaction()).isEqualTo(0);
    assertThat(savedAccountLog.getAccountNumber()).matches("\\d{7}");
  }

  @Test
  void 口座開設_異常系_口座番号が７桁を超えた場合はエラーが返されること() {
    AccountCreationRequest request = new AccountCreationRequest("テスト氏名", "password");

    when(bankAccountRepository.findAll(Sort.by(Sort.Direction.DESC, "accountNumber")))
        .thenReturn(
            List.of(new BankAccount("9999999", "password", "既存", 1000, true, Role.ACCOUNT_USER)));

    Exception e = assertThrows(IllegalStateException.class, () -> {
      bankAccountService.createAccount(request);
    });

    assertThat(e)
        .hasMessageContaining("口座番号の上限に達しました。");
  }

  @Test
  void 口座入金_正常系_リポジトリが正しく呼び出され保存内容も正しいこと() throws Exception {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 1000,
        true, Role.ACCOUNT_USER);
    AmountRequest amountRequest = new AmountRequest(500);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        Optional.of(existingAccount));
    when(bankAccountRepository.save(any(BankAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BankAccountResponse response = bankAccountService.deposit(accountNumber, amountRequest);

    ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
    verify(bankAccountRepository).save(accountCaptor.capture());
    assertThat(accountCaptor.getValue().getBalance()).isEqualTo(1500);

    ArgumentCaptor<AccountLog> transactionCaptor = ArgumentCaptor.forClass(AccountLog.class);
    verify(accountLogRepository).save(transactionCaptor.capture());
    AccountLog savedAccountLog = transactionCaptor.getValue();
    assertThat(savedAccountLog.getAccountLogType()).isEqualTo(
        AccountLogType.DEPOSIT);
    assertThat(savedAccountLog.getAccountLogStatus()).isEqualTo(
        AccountLogStatus.SUCCESS);
    assertThat(savedAccountLog.getAmount()).isEqualTo(500);
    assertThat(savedAccountLog.getBalanceAfterTransaction()).isEqualTo(1500);

    assertThat(response.getAccountNumber()).isEqualTo(accountNumber);
    assertThat(response.getBalance()).isEqualTo("1,500円");
    assertThat(response.getMessage()).isEqualTo("500円入金しました。");
  }

  @Test
  void 口座入金_異常系_存在しない口座に入金しようとした場合はエラーが返されること() {
    String accountNumber = "0000001";
    AmountRequest amountRequest = new AmountRequest(500);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(java.util.Optional.empty());

    Exception e = assertThrows(ResourceNotFoundException.class, () -> {
      bankAccountService.deposit(accountNumber, amountRequest);
    });

    assertThat(e).hasMessageContaining("口座が存在しません。");
  }

  @Test
  void 口座入金_異常系_口座が解約済である場合はエラーが返されること() {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 1000,
        false, Role.ACCOUNT_USER);
    AmountRequest amountRequest = new AmountRequest(500);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        java.util.Optional.of(existingAccount));

    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      bankAccountService.deposit(accountNumber, amountRequest);
    });

    assertThat(e).hasMessageContaining("この口座は既に解約されています。");
  }

  @Test
  void 口座出金_正常系_リポジトリが正しく呼び出され保存内容も正しいこと() throws Exception {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 1000,
        true, Role.ACCOUNT_USER);
    AmountRequest amountRequest = new AmountRequest(500);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        Optional.of(existingAccount));
    when(bankAccountRepository.save(any(BankAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BankAccountResponse response = bankAccountService.withdraw(accountNumber, amountRequest);

    ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
    verify(bankAccountRepository).save(accountCaptor.capture());
    assertThat(accountCaptor.getValue().getBalance()).isEqualTo(500);

    ArgumentCaptor<AccountLog> transactionCaptor = ArgumentCaptor.forClass(AccountLog.class);
    verify(accountLogRepository).save(transactionCaptor.capture());

    AccountLog savedAccountLog = transactionCaptor.getValue();

    assertThat(savedAccountLog.getAccountLogType()).isEqualTo(
        AccountLogType.WITHDRAW);
    assertThat(savedAccountLog.getAccountLogStatus()).isEqualTo(
        AccountLogStatus.SUCCESS);
    assertThat(savedAccountLog.getAmount()).isEqualTo(500);
    assertThat(savedAccountLog.getBalanceAfterTransaction()).isEqualTo(500);

    assertThat(response.getAccountNumber()).isEqualTo(accountNumber);
    assertThat(response.getBalance()).isEqualTo("500円");
    assertThat(response.getMessage()).isEqualTo("500円出金しました。");
  }


  @Test
  void 口座出金_異常系_残高不足の場合はエラーが返されること() {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 1000,
        true, Role.ACCOUNT_USER);
    AmountRequest amountRequest = new AmountRequest(1500);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        java.util.Optional.of(existingAccount));

    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      bankAccountService.withdraw(accountNumber, amountRequest);
    });

    assertThat(e).hasMessageContaining("残高が不足しています。");
  }

  @Test
  void 口座出金_異常系_口座が存在しない場合はエラーが返されること() {
    String accountNumber = "0000001";
    AmountRequest amountRequest = new AmountRequest(500);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(java.util.Optional.empty());

    Exception e = assertThrows(ResourceNotFoundException.class, () -> {
      bankAccountService.withdraw(accountNumber, amountRequest);
    });

    assertThat(e).hasMessageContaining("口座が存在しません。");
  }

  @Test
  void 口座出金_異常系_口座が解約済の場合はエラーが返されること() {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 1000,
        false, Role.ACCOUNT_USER);
    AmountRequest amountRequest = new AmountRequest(500);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        java.util.Optional.of(existingAccount));

    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      bankAccountService.withdraw(accountNumber, amountRequest);
    });

    assertThat(e).hasMessageContaining("この口座は既に解約されています。");
  }

  @Test
  void 口座解約_正常系_リポジトリが正しく呼び出され保存内容も正しいこと() {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 0, true,
        Role.ACCOUNT_USER);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        Optional.of(existingAccount));
    when(bankAccountRepository.save(any(BankAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    String response = bankAccountService.closeAccount(accountNumber);

    ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
    verify(bankAccountRepository).save(accountCaptor.capture());
    assertThat(accountCaptor.getValue().isActive()).isFalse();
    assertThat(response).isEqualTo("口座解約が完了しました。口座番号：" + accountNumber);

    ArgumentCaptor<AccountLog> transactionCaptor = ArgumentCaptor.forClass(AccountLog.class);
    verify(accountLogRepository).save(transactionCaptor.capture());
    AccountLog savedAccountLog = transactionCaptor.getValue();

    assertThat(savedAccountLog.getAccountLogType()).isEqualTo(AccountLogType.CLOSE);
    assertThat(savedAccountLog.getAccountLogStatus()).isEqualTo(
        AccountLogStatus.SUCCESS);
    assertThat(savedAccountLog.getAccountNumber()).isEqualTo(accountNumber);
  }

  @Test
  void 口座解約_異常系_口座が存在しない場合はエラーが返されること() {
    String accountNumber = "0000001";

    when(bankAccountRepository.findById(accountNumber)).thenReturn(java.util.Optional.empty());

    Exception e = assertThrows(ResourceNotFoundException.class, () -> {
      bankAccountService.closeAccount(accountNumber);
    });

    assertThat(e).hasMessageContaining("口座が存在しません。");
  }

  @Test
  void 口座解約_異常系_残高が残っている場合はエラーが返されること() {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 1000,
        true, Role.ACCOUNT_USER);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        java.util.Optional.of(existingAccount));

    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      bankAccountService.closeAccount(accountNumber);
    });

    assertThat(e).hasMessageContaining("残高があるため、口座を解約できません。");
  }

  @Test
  void 口座解約_異常系_既に解約済の場合はエラーが返されること() {
    String accountNumber = "0000001";
    BankAccount existingAccount = new BankAccount(accountNumber, "password", "テスト氏名", 0, false,
        Role.ACCOUNT_USER);

    when(bankAccountRepository.findById(accountNumber)).thenReturn(
        java.util.Optional.of(existingAccount));

    Exception e = assertThrows(IllegalArgumentException.class, () -> {
      bankAccountService.closeAccount(accountNumber);
    });

    assertThat(e).hasMessageContaining("既に解約済みの口座です。");
  }
}
