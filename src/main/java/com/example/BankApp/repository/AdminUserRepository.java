package com.example.BankApp.repository;

import com.example.BankApp.model.AdminUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface AdminUserRepository extends JpaRepository<AdminUser, String> {

  Optional<AdminUser> findByAdminId(String adminId);

  @Query("SELECT a.adminId FROM AdminUser a")
  List<String> findAdminIds();
}
