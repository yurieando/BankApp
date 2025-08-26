package com.example.BankApp.service;

import com.example.BankApp.dto.AdminUserResponse;
import com.example.BankApp.model.AdminUser;
import com.example.BankApp.repository.AdminUserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final AdminUserRepository adminUserRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 　管理者ユーザーの新規登録を行います。
   *
   * @param rawPassword
   * @param adminUserName
   * @param registerPassword
   * @return
   */
  public AdminUserResponse registerAdmin(String rawPassword, String adminUserName,
      String registerPassword) {
    if (!"admin123".equals(registerPassword)) {
      throw new IllegalArgumentException("管理者登録には正しいパスワードが必要です");
    }

    String adminId = generateAdminId();
    String encodedPassword = passwordEncoder.encode(rawPassword);

    AdminUser admin = new AdminUser();
    admin.setAdminId(adminId);
    admin.setPassword(encodedPassword);
    admin.setAdminUserName(adminUserName);
    admin.setRole(AdminUser.Role.ADMIN);

    AdminUser saved = adminUserRepository.save(admin);

    return AdminUserResponse.builder()
        .adminId(saved.getAdminId())
        .adminUserName(saved.getAdminUserName())
        .build();
  }

  /**
   * 管理者IDを生成します。
   *
   * @return　生成された管理者ID
   */

  private String generateAdminId() {
    List<String> ids = adminUserRepository.findAdminIds(); // 要カスタムJPQL
    int max = ids.stream()
        .filter(id -> id.matches("^admin\\d{3}$"))
        .mapToInt(id -> Integer.parseInt(id.substring(5)))
        .max()
        .orElse(0);
    return String.format("admin%03d", max + 1);
  }

  /**
   * 管理者一覧を取得します。
   *
   * @return　管理者一覧
   */
  public List<AdminUserResponse> getAdminUsers() {
    List<AdminUser> adminUsers = adminUserRepository.findAll(
        Sort.by(Sort.Direction.ASC, "adminId"));
    return adminUsers.stream()
        .map(a -> AdminUserResponse.builder()
            .adminId(a.getAdminId())
            .adminUserName(a.getAdminUserName())
            .build())
        .toList();
  }
}
