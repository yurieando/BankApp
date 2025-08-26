package com.example.BankApp.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.BankApp.dto.AdminUserResponse;
import com.example.BankApp.exception.GlobalExceptionHandler;
import com.example.BankApp.service.AdminUserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminUserController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AdminUserService adminUserService;

  @Test
  void 管理者登録_正常系_登録処理が呼び出されること() throws Exception {
    String validJson = """
        {
          "rawPassword": "password123",
          "adminUserName": "テスト名",
          "registerPassword": "admin123"
        }
        """;

    mockMvc.perform(post("/registerAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validJson))
        .andExpect(status().isOk());

    verify(adminUserService).registerAdmin(
        eq("password123"),
        eq("テスト名"),
        eq("admin123")
    );
  }

  @Test
  void 管理者登録_異常系_パスワード未入力の場合は400エラーが返されること() throws Exception {
    String json = """
        {
          "rawPassword": "",
          "adminUserName": "テスト名",
          "registerPassword": "admin123"
        }
        """;

    mockMvc.perform(post("/registerAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.rawPassword").value("パスワードは8桁以上の英数字混合で入力してください"));
  }

  @Test
  void 管理者登録_異常系_ユーザー名が未入力の場合は400エラーが返されること() throws Exception {
    String json = """
        {
          "rawPassword": "password123",
          "adminUserName": "",
          "registerPassword": "admin123"
        }
        """;

    mockMvc.perform(post("/registerAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.adminUserName").value("名前は必須です"));
  }

  @Test
  void 管理者登録_異常系_登録用パスワードが未入力の場合は400エラーが返されること()
      throws Exception {
    String json = """
        {
          "rawPassword": "password123",
          "adminUserName": "テスト名",
          "registerPassword": ""
        }
        """;

    mockMvc.perform(post("/registerAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.registerPassword").value("登録用パスワードは必須です"));
  }

  @Test
  void 管理者登録_異常系_数字を含まないパスワードの場合は400エラーが返されること()
      throws Exception {
    String json = """
        {
          "rawPassword": "Password",
          "adminUserName": "テスト名",
          "registerPassword": "admin123"
        }
        """;

    mockMvc.perform(post("/registerAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.rawPassword").value("パスワードは8桁以上の英数字混合で入力してください"));
  }

  @Test
  void 管理者登録_異常系_登録用パスワードが誤っていた場合は400エラーが返されること()
      throws Exception {
    String json = """
        {
          "rawPassword": "password123",
          "adminUserName": "テスト名",
          "registerPassword": "wrong123"
        }
        """;

    doThrow(new IllegalArgumentException("登録用パスワードが不正です"))
        .when(adminUserService)
        .registerAdmin(anyString(), anyString(), eq("wrong123"));

    mockMvc.perform(post("/registerAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("登録用パスワードが不正です"));
  }

  @Test
  void 管理者一覧取得_正常系_全ての管理者情報が返されること() throws Exception {
    List<AdminUserResponse> mockUsers = List.of(
        new AdminUserResponse("admin1", "管理者1"),
        new AdminUserResponse("admin2", "管理者2")
    );

    when(adminUserService.getAdminUsers()).thenReturn(mockUsers);

    mockMvc.perform(get("/admin/adminUsers"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].adminId").value("admin1"))
        .andExpect(jsonPath("$[0].adminUserName").value("管理者1"))
        .andExpect(jsonPath("$[1].adminId").value("admin2"))
        .andExpect(jsonPath("$[1].adminUserName").value("管理者2"));

    verify(adminUserService).getAdminUsers();
  }

  @Test
  void 管理者一覧取得_異常系_内部エラーが返されること() throws Exception {
    when(adminUserService.getAdminUsers())
        .thenThrow(new RuntimeException("DBエラー"));

    mockMvc.perform(get("/admin/adminUsers"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("予期せぬエラーが発生しました。"));

    verify(adminUserService).getAdminUsers();
  }
}
