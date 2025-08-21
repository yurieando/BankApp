package com.example.BankApp.controller;

import com.example.BankApp.dto.AdminUserRegisterRequest;
import com.example.BankApp.dto.AdminUserResponse;
import com.example.BankApp.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminUserController {

  private final AdminUserService adminUserService;
  private final PasswordEncoder passwordEncoder;


  /**
   * 新しい管理者ユーザーを登録します。
   *
   * @return 登録された管理者ユーザー情報
   */
  @PostMapping("/registerAdmin")
  public AdminUserResponse registerAdminUser(@Valid @RequestBody AdminUserRegisterRequest request) {
    return adminUserService.register(request.getRawPassword(), request.getAdminUserName(),
        request.getRegisterPassword());
  }

  /**
   * 管理者一覧を取得します。
   *
   * @return 管理者一覧情報
   */
  @GetMapping("/admin/adminUsers")
  public List<AdminUserResponse> getAdminUsers() {
    return adminUserService.getAdminUsers();
  }
}
