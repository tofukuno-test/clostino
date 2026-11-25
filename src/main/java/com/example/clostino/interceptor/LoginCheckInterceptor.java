package com.example.clostino.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ログインチェックを行うInterceptor。
 *
 * <p>セッションにログインユーザーが存在しない場合、ログインページへリダイレクトする。</p>
 *
 * <p>Spring Security採用済みの場合は、SecurityConfigのauthorizeHttpRequestsで
 * アクセス制御を管理するため、このInterceptorによるセッションチェックは原則不要。
 * APIキー認証など、Spring Securityの標準フロー外の認証が必要なケースで活用する。</p>
 */
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginCheckInterceptor.class);

    /**
     * Controllerの実行前に呼ばれる。
     * セッションにログインユーザーが存在しない場合はログインページへリダイレクトし、
     * falseを返してController処理を中断する。
     *
     * @return trueなら処理を続行、falseなら処理を中断
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false); // セッションが存在しない場合はnullを返す

        if (session == null || session.getAttribute("loginUser") == null) {
            logger.warn("未ログインアクセス: {}", request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/login");
            return false; // Controllerの処理を中断
        }

        return true; // ログイン済みなら処理を続行
    }
}