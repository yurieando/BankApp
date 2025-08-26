package com.example.BankApp.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.BankApp.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

  @MockitoBean
  AuthenticationManager authenticationManager;
  @Autowired
  private MockMvc mockMvc;

  @Test
  void ログイン_正常系_認証に成功するとメッセージとユーザー情報が返されること() throws Exception {
    var principal = new User("testuser", "N/A", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    Authentication authResult =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

    when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authResult);

    String json = """
        {"loginId":"testuser","password":"password123"}
        """;

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("ログイン成功"))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
  }


  @Test
  void ログイン_異常系_認証失敗時は401エラーが返されること() throws Exception {
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenThrow(new org.springframework.security.core.AuthenticationException("bad") {
        });

    String json = """
        {"loginId":"wrong","password":"wrong"}
        """;

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("IDまたはパスワードが違います"));
  }

  @Test
  void ログイン_異常系_必須項目が未入力の場合は400エラーが返されること() throws Exception {
    String json = """
        {
          "loginId": "",
          "password": ""
        }
        """;

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.loginId").value("ログインIDは必須です"))
        .andExpect(jsonPath("$.password").value("パスワードは必須です"));
  }


  @Test
  @WithMockUser(username = "alice", roles = {"ADMIN"})
  void ログイン中のユーザー情報_ログイン済の場合はユーザー情報が返されること() throws Exception {
    mockMvc.perform(get("/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("alice"))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
  }

  @Test
  void ログイン中のユーザー情報_未ログインの場合は401エラーが返されること() throws Exception {
    mockMvc.perform(get("/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("未ログイン"));
  }

  @Test
  @WithMockUser
  void ログアウト_正常系_ログアウトが完了してメッセージが返されること() throws Exception {
    mockMvc.perform(post("/logout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("ログアウトしました"));
  }
}
