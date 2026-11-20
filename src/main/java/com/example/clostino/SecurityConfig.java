package com.example.clostino;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * セキュリティ設定クラス。
 * 開発中はすべてのリクエストへのアクセスを許可する。
 * 本格的な認証・認可（ログイン画面・パスワードのハッシュ化など）は
 * 「ログイン・ログアウト機能を実装する」パートで実装する。
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // すべてのリクエストを許可（開発用）
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // REST API と H2コンソールは CSRF 対象から除外
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            // H2コンソールを画面内（iframe）で表示できるようにする
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );
        return http.build();
    }
}