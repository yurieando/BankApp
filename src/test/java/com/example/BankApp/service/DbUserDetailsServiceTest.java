package com.example.BankApp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.BankApp.model.AdminUser;
import com.example.BankApp.model.BankAccount;
import com.example.BankApp.repository.AdminUserRepository;
import com.example.BankApp.repository.BankAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class DbUserDetailsServiceTest {

  @Mock
  AdminUserRepository adminUserRepository;

  @Mock
  BankAccountRepository bankAccountRepository;

  @InjectMocks
  DbUserDetailsService service;

  @Test
  void ユーザー検索_正常系_管理者が見つかった場合は管理者情報を返すこと() {
    AdminUser admin = new AdminUser();
    admin.setAdminId("admin001");
    admin.setPassword("ENC_ADMIN");
    admin.setRole(AdminUser.Role.ADMIN);
    when(adminUserRepository.findByAdminId("admin001"))
        .thenReturn(Optional.of(admin));

    UserDetails ud = service.loadUserByUsername("admin001");

    assertThat(ud.getUsername()).isEqualTo("admin001");
    assertThat(ud.getPassword()).isEqualTo("ENC_ADMIN");
    assertThat(ud.getAuthorities()).extracting("authority")
        .containsExactly("ROLE_ADMIN");

    verify(adminUserRepository).findByAdminId("admin001");

    verifyNoInteractions(bankAccountRepository);
  }

  @Test
  void ユーザー検索_正常系_口座保有者が見つかった場合は口座情報を返すこと() {
    when(adminUserRepository.findByAdminId("0000001"))
        .thenReturn(Optional.empty());

    BankAccount acct = new BankAccount();
    acct.setAccountNumber("0000001");
    acct.setPassword("ENC_USER");
    acct.setRole(BankAccount.Role.ACCOUNT_USER);

    when(bankAccountRepository.findById("0000001"))
        .thenReturn(Optional.of(acct));

    UserDetails ud = service.loadUserByUsername("0000001");

    assertThat(ud.getUsername()).isEqualTo("0000001");
    assertThat(ud.getPassword()).isEqualTo("ENC_USER");
    assertThat(ud.getAuthorities()).extracting("authority")
        .containsExactly("ROLE_ACCOUNT_USER");

    verify(adminUserRepository).findByAdminId("0000001");
    verify(bankAccountRepository).findById("0000001");
  }

  @Test
  void ユーザー検索_異常系_管理者にも口座保有者にも該当しない場合は例外を送出すること() {
    when(adminUserRepository.findByAdminId("ghost")).thenReturn(Optional.empty());
    when(bankAccountRepository.findById("ghost")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class,
        () -> service.loadUserByUsername("ghost"));

    verify(adminUserRepository).findByAdminId("ghost");
    verify(bankAccountRepository).findById("ghost");
  }
}
