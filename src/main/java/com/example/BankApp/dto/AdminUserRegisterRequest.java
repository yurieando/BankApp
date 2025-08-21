package com.example.BankApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AdminUserRegisterRequest {

  @NotBlank(message = "パスワードは必須です")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
      message = "パスワードは8桁以上の英数字混合で入力してください"
  )
  private String rawPassword;

  @NotBlank(message = "名前は必須です")
  private String adminUserName;

  @NotBlank(message = "登録パスワードは必須です")
  private String registerPassword; //admin123
}
