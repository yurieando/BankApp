package com.example.BankApp.repository;

import com.example.BankApp.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

  List<Transaction> findByAccountNumber(String accountNumber);

  List<Transaction> findByAccountNumberAndTransactionType(String accountNumber,
      Transaction.TransactionType transactionType);
}
