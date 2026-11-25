package com.example.clostino.controller;

import com.example.clostino.dto.UserRegisterForm;
import com.example.clostino.entity.User;
import com.example.clostino.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ユーザーコントローラー。
 * ログイン画面の表示・会員登録を担当する。
 *
 * ログイン・ログアウト処理は Spring Security が自動的に行うため、
 * このコントローラーでは実装しない。マイページ機能は次の「マイページを実装する」パートで追加する。
 */
@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // === ログイン画面 ===

    /**
     * ログイン画面の表示。
     * ログイン処理自体は Spring Security が POST /login で自動的に行う。
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // === 会員登録 ===

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userForm", new UserRegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("userForm") UserRegisterForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!form.isPasswordMatch()) {
            model.addAttribute("error", "パスワードが一致しません");
            return "register";
        }

        if (userService.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "このメールアドレスは既に登録されています");
            return "register";
        }

        try {
            User user = new User();
            user.setEmail(form.getEmail());
            user.setPassword(form.getPassword());
            user.setName(form.getName());
            user.setAddress(form.getAddress());
            userService.register(user);

            redirectAttributes.addFlashAttribute("message", "会員登録が完了しました。ログインしてください。");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("ユーザー登録エラー", e);
            model.addAttribute("error", "登録処理中にエラーが発生しました。");
            return "register";
        }
    }
}