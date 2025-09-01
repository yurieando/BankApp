package com.example.BankApp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.BankApp.dto.AdminUserResponse;
import com.example.BankApp.model.AdminUser;
import com.example.BankApp.repository.AdminUserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

  @Mock
  AdminUserRepository adminUserRepository;

  @Mock
  PasswordEncoder passwordEncoder;

  @InjectMocks
  AdminUserService adminUserService;

  @BeforeEach
  void setUp() {
  }

  @Test
  void 管理者登録_正常系_保存されたデータが正しく返ること() {
    String rawPassword = "password123";
    String encoded = "ENCODED";
    String adminUserName = "テスト名";

    // 既存IDから次IDを計算（admin003 を期待）
    when(adminUserRepository.findAdminIds())
        .thenReturn(List.of("admin001", "x-admin", "admin002"));
    when(passwordEncoder.encode(rawPassword)).thenReturn(encoded);

    // save時にリポジトリが返すエンティティ
    when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(inv -> {
      AdminUser a = inv.getArgument(0);
      return a;
    });

    AdminUserResponse res =
        adminUserService.registerAdmin("admin123", adminUserName, rawPassword);

    assertNotNull(res);
    assertEquals("admin003", res.getAdminId());
    assertEquals("テスト名", res.getAdminUserName());

    ArgumentCaptor<AdminUser> captor = ArgumentCaptor.forClass(AdminUser.class);
    verify(adminUserRepository).save(captor.capture());
    AdminUser saved = captor.getValue();

    assertEquals("admin003", saved.getAdminId());
    assertEquals(encoded, saved.getPassword());
    assertEquals(adminUserName, saved.getAdminUserName());
    assertEquals(AdminUser.Role.ADMIN, saved.getRole());

    verify(adminUserRepository).findAdminIds();
    verify(passwordEncoder).encode(rawPassword);
  }

  @Test
  void 管理者登録_異常系_登録用パスワード不一致の場合は例外メッセージが返ること() {
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> adminUserService.registerAdmin("wrong123", "テスト名", "password123")
    );
    assertEquals("管理者登録には正しいパスワードが必要です", ex.getMessage());

    verify(adminUserRepository, never()).save(any());
    verify(passwordEncoder, never()).encode(anyString());
  }

  @Test
  void 管理者一覧_正常系_全ての管理者が取得できること() {
    AdminUser a1 = new AdminUser();
    a1.setAdminId("admin001");
    a1.setAdminUserName("管理者1");
    a1.setPassword("x");
    a1.setRole(AdminUser.Role.ADMIN);

    AdminUser a2 = new AdminUser();
    a2.setAdminId("admin002");
    a2.setAdminUserName("管理者2");
    a2.setPassword("y");
    a2.setRole(AdminUser.Role.ADMIN);

    when(adminUserRepository.findAll(any(Sort.class))).thenReturn(List.of(a1, a2));

    List<AdminUserResponse> list = adminUserService.getAdminUsers();

    assertEquals(2, list.size());
    assertEquals("admin001", list.get(0).getAdminId());
    assertEquals("管理者1", list.get(0).getAdminUserName());
    assertEquals("admin002", list.get(1).getAdminId());
    assertEquals("管理者2", list.get(1).getAdminUserName());

    ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
    verify(adminUserRepository).findAll(sortCaptor.capture());
    Sort sort = sortCaptor.getValue();
    assertTrue(sort.stream().anyMatch(o -> o.getProperty().equals("adminId")));
  }
}
