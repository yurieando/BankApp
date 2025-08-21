package com.example.BankApp.service;

import com.example.BankApp.model.AdminUser;
import com.example.BankApp.model.BankAccount;
import com.example.BankApp.repository.AdminUserRepository;
import com.example.BankApp.repository.BankAccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

  private final AdminUserRepository adminUserRepository;
  private final BankAccountRepository bankAccountRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1) 管理者(adminId)として検索
    Optional<AdminUser> foundAdmin = adminUserRepository.findByAdminId(username);
    if (foundAdmin.isPresent()) {
      AdminUser a = foundAdmin.get();
      return org.springframework.security.core.userdetails.User
          .withUsername(a.getAdminId())
          .password(a.getPassword())
          .roles(a.getRole().name())
          .build();
    }

    // 2) 口座ユーザ(accountNumber=ログインID)として検索
    Optional<BankAccount> foundAccount = bankAccountRepository.findById(username);
    if (foundAccount.isPresent()) {
      BankAccount u = foundAccount.get();
      return org.springframework.security.core.userdetails.User
          .withUsername(u.getAccountNumber())
          .password(u.getPassword())
          .roles(u.getRole().name())
          .build();
    }

    throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
  }
}
