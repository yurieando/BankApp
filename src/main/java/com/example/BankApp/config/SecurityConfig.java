package com.example.BankApp.config;

import com.example.BankApp.service.DbUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final DbUserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // AuthController で AuthenticationManager を使うために公開
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRFはSPA/純API構成なので無効化
        .csrf(AbstractHttpConfigurer::disable)
        // ルーティング毎のアクセス制御
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/login",
                "/logout",
                "/registerAdmin",  // 管理者登録は事前パスワードで保護
                "/createAccount"
            ).permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN") // 管理者専用
            .anyRequest().authenticated()                  // その他はログイン必須
        )
        // フォーム/Basicは使わない（AuthControllerで実装するため）
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(logout -> logout.disable())
        .exceptionHandling(e -> e
            // 未ログインで保護リソースにアクセス → 401
            .authenticationEntryPoint((request, response, ex) -> {
              response.setStatus(401);
              response.setCharacterEncoding(StandardCharsets.UTF_8.name());
              response.setContentType("application/json;charset=UTF-8");
              var body = Map.of(
                  "error", "認証が必要です",
                  "path", request.getRequestURI()
              );
              new ObjectMapper().writeValue(response.getWriter(), body);
            })
            // ログイン済みだが権限不足 → 403
            .accessDeniedHandler((request, response, ex) -> {
              response.setStatus(403);
              response.setCharacterEncoding(StandardCharsets.UTF_8.name());
              response.setContentType("application/json;charset=UTF-8");
              var body = Map.of(
                  "error", "権限がありません",
                  "path", request.getRequestURI()
              );
              new ObjectMapper().writeValue(response.getWriter(), body);
            })
        );

    return http.build();
  }
}
