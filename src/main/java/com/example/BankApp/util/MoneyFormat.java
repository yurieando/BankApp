package com.example.BankApp.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormat {

  private MoneyFormat() {
  }

  public static String yen(int amount) {
    return NumberFormat.getNumberInstance(Locale.JAPAN).format(amount) + "円";
  }
}
