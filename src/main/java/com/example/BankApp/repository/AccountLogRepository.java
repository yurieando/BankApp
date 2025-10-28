package com.example.BankApp.repository;

import com.example.BankApp.model.AccountLog;
import com.example.BankApp.model.AccountLog.AccountLogType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountLogRepository extends JpaRepository<AccountLog, String> {

  List<AccountLog> findByAccountNumberOrderByTimestampDesc(String accountNumber);

  List<AccountLog> findByAccountNumberAndAccountLogTypeOrderByTimestampDesc(
      String accountNumber, AccountLogType type);
}
