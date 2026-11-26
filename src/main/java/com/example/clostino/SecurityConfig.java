package com.example.clostino;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security の設定クラス。
 * アクセス制御・フォームログイン・ログアウト・CSRF保護を設定する。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // === アクセス制御 ===
            .authorizeHttpRequests(auth -> auth
                // 未ログインでもアクセス可能なURL
                .requestMatchers(
                    "/", "/products", "/products/**",
                    "/cart", "/cart/**",
                    "/register", "/login",
                    "/error",
                    "/css/**", "/js/**", "/images/**",
                    "/h2-console/**",
                    "/api/**"
                ).permitAll()
                // それ以外はログインが必要
                .anyRequest().authenticated()
            )
            // === フォームログイン ===
            .formLogin(form -> form
                .loginPage("/login")               // ログイン画面のURL
                .usernameParameter("email")         // フォームのemail欄をusernameとして扱う
                .passwordParameter("password")
                .defaultSuccessUrl("/mypage", true) // ログイン成功後のリダイレクト先
                .failureUrl("/login?error")         // ログイン失敗時のリダイレクト先
                .permitAll()
            )
            // === ログアウト ===
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)        // セッションを破棄
                .deleteCookies("JSESSIONID")        // JSESSIONIDクッキーを削除
                .permitAll()
            )
            // === CSRF設定 ===
            .csrf(csrf -> csrf
                // REST APIとH2コンソールはCSRF除外（Postmanなどからのアクセスのため）
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            // === H2コンソール用フレーム設定 ===
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    /**
     * パスワードエンコーダーのBean定義。
     * BCryptアルゴリズムでハッシュ化・照合を行う。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}