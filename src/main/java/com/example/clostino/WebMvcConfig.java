package com.example.clostino;

import com.example.clostino.interceptor.AccessLogInterceptor;
import com.example.clostino.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVCの設定クラス。
 * InterceptorをDispatcherServletに登録する。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AccessLogInterceptor accessLogInterceptor;

    public WebMvcConfig(AccessLogInterceptor accessLogInterceptor) {
        this.accessLogInterceptor = accessLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // アクセスログInterceptor（全URLに適用）
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**", "/js/**", "/images/**",  // 静的ファイルは除外
                        "/favicon.ico"
                );

        // ログインチェックInterceptor
        // ※Spring Security採用済みのため通常は不要。学習目的での実装例として掲載。
        // registry.addInterceptor(new LoginCheckInterceptor())
        //         .addPathPatterns("/**")
        //         .excludePathPatterns(
        //                 "/login", "/register",          // 認証不要ページ
        //                 "/", "/products", "/products/**", // 公開ページ
        //                 "/css/**", "/js/**", "/images/**",
        //                 "/favicon.ico"
        //         );
    }
}