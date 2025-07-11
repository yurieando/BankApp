package com.example.BankApp.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.BankApp.dto.AmountRequest;
import com.example.BankApp.exception.ResourceNotFoundException;
import com.example.BankApp.model.AccountLog;
import com.example.BankApp.model.AccountLog.AccountLogType;
import com.example.BankApp.repository.AccountLogRepository;
import com.example.BankApp.service.BankAccountService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BankAccountController.class)
class BankAccountControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BankAccountService bankAccountService;

  @MockitoBean
  private AccountLogRepository accountLogRepository;


  @Test
  void 口座一覧取得_正常系_口座一覧を取得できること() throws Exception {
    mockMvc.perform(get("/accountsForAdmin"))
        .andExpect(status().isOk());
    verify(bankAccountService).getAllAccountsForAdmin();
  }

  @Test
  void 口座一覧取得_正常系_口座が存在しない場合でも空リストが返ること() throws Exception {
    mockMvc.perform(get("/accountsForAdmin"))
        .andExpect(status().isOk());
    verify(bankAccountService).getAllAccountsForAdmin();
  }

  @Test
  void 口座一覧取得_異常系_口座一覧取得時に例外が発生した場合_500エラーが返ること()
      throws Exception {
    when(bankAccountService.getAllAccountsForAdmin())
        .thenThrow(new RuntimeException("内部サーバーエラー"));
    mockMvc.perform(get("/accountsForAdmin"))
        .andExpect(status().isInternalServerError());
    verify(bankAccountService).getAllAccountsForAdmin();
  }

  @Test
  void 口座作成_正常系_口座を作成できること() throws Exception {
    String validJson = """
        {
          "accountHolderName": "テスト氏名"
        }
        """;

    mockMvc.perform(post("/createAccount")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isOk());

    verify(bankAccountService).createAccount(
        argThat(request -> "テスト氏名".equals(request.getAccountHolderName()))
    );
  }

  @Test
  void 口座作成_必須項目が未入力の場合_400エラーが返ること() throws Exception {
    String invalidJson = """
         {
           "accountHolderName": ""
        }
        """;

    mockMvc.perform(post("/createAccount")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.accountHolderName").value(
                "口座名義は必須です。"));
  }

  @Test
  void 個別の口座情報取得_正常系_口座情報を取得できること() throws Exception {
    String accountNumber = "0000001";
    mockMvc.perform(get("/account/{accountNumber}", accountNumber))
        .andExpect(status().isOk());
    verify(bankAccountService).getBalance(accountNumber);
  }

  @Test
  void 個別の口座情報取得_異常系_口座番号が存在しない場合_404エラーが返ること() throws Exception {
    String accountNumber = "0000000";
    when(bankAccountService.getBalance(accountNumber))
        .thenThrow(new ResourceNotFoundException("口座が存在しません。"));
    mockMvc.perform(get("/account/{accountNumber}", accountNumber))
        .andExpect(status().isNotFound());
    verify(bankAccountService).getBalance(accountNumber);
  }

  @Test
  void 個別の口座情報取得_異常系_口座番号の形式が不正な場合_400エラーが返ること() throws Exception {
    mockMvc.perform(get("/account/abc123"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.field").value("口座番号は7桁の数字である必要があります"));
  }

  @Test
  void 口座入金_正常系_入金が成功すること() throws Exception {
    String accountNumber = "0000001";
    String validJson = """
        {
          "amount": 1000
        }
        """;

    mockMvc.perform(post("/deposit/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isOk());

    verify(bankAccountService).deposit(
        argThat(account -> account.equals(accountNumber)),
        argThat(request -> request.getAmount() == 1000)
    );
  }

  @Test
  void 口座入金_異常系_入金金額が0円の場合_400エラーが返ること() throws Exception {
    String accountNumber = "0000001";
    String invalidJson = """
        {
          "amount": 0
        }
        """;

    mockMvc.perform(post("/deposit/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.amount").value("金額は1以上でなければなりません。"));
  }

  @Test
  void 口座入金_異常系_口座番号が存在しない場合_404エラーが返ること() throws Exception {
    String accountNumber = "0000000";
    String validJson = """
        {
          "amount": 1000
        }
        """;

    when(bankAccountService.deposit(accountNumber, new AmountRequest(1000)))
        .thenThrow(new ResourceNotFoundException("口座が存在しません。"));

    mockMvc.perform(post("/deposit/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isNotFound());

    verify(bankAccountService).deposit(accountNumber, new AmountRequest(1000));
  }

  @Test
  void 口座入金_異常系_口座番号の形式が不正な場合_400エラーが返ること() throws Exception {
    mockMvc.perform(post("/deposit/abc123")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "amount": 1000
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.field").value("口座番号は7桁の数字である必要があります"));
  }

  @Test
  void 口座出金_正常系_出金が成功すること() throws Exception {
    String accountNumber = "0000001";
    String validJson = """
        {
          "amount": 500
        }
        """;

    mockMvc.perform(post("/withdraw/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isOk());

    verify(bankAccountService).withdraw(
        argThat(account -> account.equals(accountNumber)),
        argThat(request -> request.getAmount() == 500)
    );
  }

  @Test
  void 口座出金_異常系_出金金額が0円の場合_400エラーが返ること() throws Exception {
    String accountNumber = "0000001";
    String invalidJson = """
        {
          "amount": 0
        }
        """;

    mockMvc.perform(post("/withdraw/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.amount").value("金額は1以上でなければなりません。"));
  }

  @Test
  void 口座出金_異常系_口座番号が存在しない場合_404エラーが返ること() throws Exception {
    String accountNumber = "0000000";
    String validJson = """
        {
          "amount": 500
        }
        """;

    when(bankAccountService.withdraw(accountNumber, new AmountRequest(500)))
        .thenThrow(new ResourceNotFoundException("口座が存在しません。"));

    mockMvc.perform(post("/withdraw/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isNotFound());

    verify(bankAccountService).withdraw(accountNumber, new AmountRequest(500));
  }

  @Test
  void 口座出金_異常系_口座番号の形式が不正な場合_400エラーが返ること() throws Exception {
    mockMvc.perform(post("/withdraw/abc123")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "amount": 500
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.field").value("口座番号は7桁の数字である必要があります"));
  }

  @Test
  void 口座出金_異常系_出金後の残高がマイナスになる場合_400エラーが返ること() throws Exception {
    String accountNumber = "0000001";
    String validJson = """
        {
          "amount": 10000
        }
        """;

    when(bankAccountService.withdraw(accountNumber, new AmountRequest(10000)))
        .thenThrow(new IllegalArgumentException("残高不足です。"));

    mockMvc.perform(post("/withdraw/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("残高不足です。"));

    verify(bankAccountService).withdraw(accountNumber, new AmountRequest(10000));
  }

  @Test
  void 口座出金_異常系_口座が解約されている場合_400エラーが返ること() throws Exception {
    String accountNumber = "0000001";
    String validJson = """
        {
          "amount": 500
        }
        """;

    when(bankAccountService.withdraw(accountNumber, new AmountRequest(500)))
        .thenThrow(new IllegalArgumentException("この口座は既に解約されています。"));

    mockMvc.perform(post("/withdraw/{accountNumber}", accountNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("この口座は既に解約されています。"));

    verify(bankAccountService).withdraw(accountNumber, new AmountRequest(500));
  }

  @Test
  void 取引履歴取得_正常系_口座の取引履歴を取得できること() throws Exception {
    String accountNumber = "0000001";

    List<AccountLog> dummyAccountLogs = List.of(new AccountLog());
    when(accountLogRepository.findByAccountNumber(accountNumber))
        .thenReturn(dummyAccountLogs);

    mockMvc.perform(get("/accountLog/{accountNumber}", accountNumber))
        .andExpect(status().isOk());

    verify(accountLogRepository).findByAccountNumber(accountNumber);
  }

  @Test
  void 取引履歴取得_正常系_取引タイプ指定でフィルタ取得できること() throws Exception {
    String accountNumber = "0000001";

    List<AccountLog> dummyAccountLogs = List.of(new AccountLog());
    when(accountLogRepository.findByAccountNumberAndAccountLogType(accountNumber,
        AccountLogType.DEPOSIT))
        .thenReturn(dummyAccountLogs);

    mockMvc.perform(get("/accountLog/{accountNumber}", accountNumber)
            .param("accountLogType", "DEPOSIT"))
        .andExpect(status().isOk());

    verify(accountLogRepository).findByAccountNumberAndAccountLogType(accountNumber,
        AccountLogType.DEPOSIT);
  }

  @Test
  void 取引履歴取得_異常系_口座番号が存在しない場合_404エラーが返ること() throws Exception {
    String accountNumber = "0000000";

    when(accountLogRepository.findByAccountNumber(accountNumber))
        .thenThrow(new ResourceNotFoundException("口座が存在しません。"));

    mockMvc.perform(get("/accountLog/{accountNumber}", accountNumber))
        .andExpect(status().isNotFound());

    verify(accountLogRepository).findByAccountNumber(accountNumber);
  }

  @Test
  void 取引履歴取得_異常系_口座番号の形式が不正な場合_400エラーが返ること() throws Exception {
    mockMvc.perform(get("/accountLog/abc123"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.field").value("口座番号は7桁の数字である必要があります"));
  }

  @Test
  void 取引履歴取得_異常系_口座が解約されている場合_400エラーが返ること() throws Exception {
    String accountNumber = "0000001";

    when(accountLogRepository.findByAccountNumber(accountNumber))
        .thenThrow(new IllegalArgumentException("この口座は既に解約されています。"));

    mockMvc.perform(get("/accountLog/{accountNumber}", accountNumber))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("この口座は既に解約されています。"));

    verify(accountLogRepository).findByAccountNumber(accountNumber);
  }

  @Test
  void 取引履歴取得_異常系_口座に取引履歴がない場合_404エラーが返ること() throws Exception {
    String accountNumber = "0000001";

    when(accountLogRepository.findByAccountNumber(accountNumber))
        .thenThrow(new ResourceNotFoundException("取引履歴が存在しません。"));

    mockMvc.perform(get("/accountLog/{accountNumber}", accountNumber))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("取引履歴が存在しません。"));

    verify(accountLogRepository).findByAccountNumber(accountNumber);
  }


  @Test
  void 口座解約_正常系_口座を解約できること() throws Exception {
    String accountNumber = "0000001";

    mockMvc.perform(post("/closeAccount/{accountNumber}", accountNumber))
        .andExpect(status().isOk());

    verify(bankAccountService).closeAccount(accountNumber);
  }

  @Test
  void 口座解約_異常系_口座番号が存在しない場合_404エラーが返ること() throws Exception {
    String accountNumber = "0000000";

    when(bankAccountService.closeAccount(accountNumber))
        .thenThrow(new ResourceNotFoundException("口座が存在しません。"));

    mockMvc.perform(post("/closeAccount/{accountNumber}", accountNumber))
        .andExpect(status().isNotFound());

    verify(bankAccountService).closeAccount(accountNumber);
  }

  @Test
  void 口座解約_異常系_口座番号の形式が不正な場合_400エラーが返ること() throws Exception {
    mockMvc.perform(post("/closeAccount/abc123"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.field").value("口座番号は7桁の数字である必要があります"));
  }

  @Test
  void 口座解約_異常系_口座が既に解約されている場合_400エラーが返ること() throws Exception {
    String accountNumber = "0000001";

    when(bankAccountService.closeAccount(accountNumber))
        .thenThrow(new IllegalArgumentException("この口座は既に解約されています。"));

    mockMvc.perform(post("/closeAccount/{accountNumber}", accountNumber))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("この口座は既に解約されています。"));

    verify(bankAccountService).closeAccount(accountNumber);
  }

  @Test
  void 口座解約_異常系_口座に残高がある場合_400エラーが返ること() throws Exception {
    String accountNumber = "0000001";

    when(bankAccountService.closeAccount(accountNumber))
        .thenThrow(new IllegalArgumentException("口座に残高があるため解約できません。"));

    mockMvc.perform(post("/closeAccount/{accountNumber}", accountNumber))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("口座に残高があるため解約できません。"));

    verify(bankAccountService).closeAccount(accountNumber);
  }
}
