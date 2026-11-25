package com.example.clostino.controller;

import com.example.clostino.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全コントローラーに共通のモデル属性を追加する。
 * ログインユーザーの表示名をすべての画面（Thymeleafフラグメントを含む）に提供する。
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final UserService userService;

    public GlobalModelAdvice(UserService userService) {
        this.userService = userService;
    }

    /**
     * ログインユーザーの表示名をモデルに追加する。
     * 未ログインの場合は null を返す。
     */
    @ModelAttribute("loggedInUserName")
    public String loggedInUserName(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return userService.findByEmail(authentication.getName())
                    .map(user -> user.getName())
                    .orElse(null);
        }
        return null;
    }
}