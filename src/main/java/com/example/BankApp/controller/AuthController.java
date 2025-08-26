package com.example.BankApp.controller;

import com.example.BankApp.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;

  /**
   * ログイン認証をします。
   *
   * @return　ユーザー情報
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request,
      HttpServletResponse response) {
    try {
      Authentication auth = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(req.getLoginId(), req.getPassword())
      );

      org.springframework.security.core.context.SecurityContext context =
          org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
      context.setAuthentication(auth);
      org.springframework.security.core.context.SecurityContextHolder.setContext(context);
      new org.springframework.security.web.context.HttpSessionSecurityContextRepository()
          .saveContext(context, request, response);

      var principal = (UserDetails) auth.getPrincipal();
      return ResponseEntity.ok(Map.of(
          "message", "ログイン成功",
          "username", principal.getUsername(),
          "roles", principal.getAuthorities().stream()
              .map(a -> a.getAuthority()).toList()
      ));
    } catch (org.springframework.security.core.AuthenticationException ex) {
      return ResponseEntity.status(401).body(Map.of("error", "IDまたはパスワードが違います"));
    }
  }

  /**
   * ログイン中のユーザー情報を表示します。
   *
   * @return　ユーザー情報
   */
  @GetMapping("/me")
  public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails user) {
    if (user == null) {
      return ResponseEntity.status(401).body(Map.of("error", "未ログイン"));
    }
    return ResponseEntity.ok(Map.of(
        "username", user.getUsername(),
        "roles", user.getAuthorities().stream()
            .map(a -> a.getAuthority()).toList()
    ));
  }

  /**
   * ログアウトします。
   *
   * @return　ログアウトメッセージ
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(
      jakarta.servlet.http.HttpServletRequest req,
      jakarta.servlet.http.HttpServletResponse res
  ) {
    new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
        .logout(req, res, null);
    return ResponseEntity.ok(Map.of("message", "ログアウトしました"));
  }
}
