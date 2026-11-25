package com.example.clostino.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * アクセスログを記録するInterceptor。
 * リクエストのURL・HTTPメソッド・処理時間をINFOレベルでログ出力する。
 *
 * <p>Spring Securityのフィルター処理後、Controllerの前後で動作するため、
 * 認証済みリクエストのアクセスログを記録するのに適している。</p>
 */
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogInterceptor.class);

    /** リクエストの開始時刻を格納するリクエスト属性のキー */
    private static final String START_TIME_ATTR = "requestStartTime";

    /**
     * Controllerの実行前：リクエストの開始時刻を記録する。
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true; // 処理を続行
    }

    /**
     * Controllerの実行後（View描画前）：モデルへの追加などが必要なければ何もしない。
     */
    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) throws Exception {
        // このInterceptorではpostHandleは使用しない
    }

    /**
     * レスポンス送信後：処理時間を計算してアクセスログを出力する。
     * 例外が発生した場合も呼ばれるため、ログを残しやすい。
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) throws Exception {

        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) {
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        logger.info("[ACCESS] {} {} - {} ({}ms)",
                request.getMethod(),      // HTTPメソッド（GET, POST等）
                request.getRequestURI(),  // リクエストURL
                status,                   // HTTPステータスコード
                duration                  // 処理時間（ミリ秒）
        );

        // 処理時間が長いリクエストは警告レベルでも出力
        if (duration > 1000) {
            logger.warn("[SLOW REQUEST] {} {} took {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    duration);
        }
    }
}