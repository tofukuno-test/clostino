package com.example.clostino.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * URLに対応するControllerが見つからない場合の例外を処理する。
     * application.properties の spring.mvc.throw-exception-if-no-handler-found=true が必要。
     * ブラウザ（Accept: text/html）からは404ページを、APIからはJSONを返す。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        logger.warn("ページが見つかりません: {}", ex.getRequestURL());

        String accept = request.getHeader("Accept") != null ? request.getHeader("Accept") : "";
        if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
            ModelAndView mav = new ModelAndView("error/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            return mav;
        }

        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "ページが見つかりません",
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * URLに対応する静的リソースが見つからない場合の例外を処理する。
     * Spring 6.x では、ResourceHttpRequestHandler がリソースを見つけられない場合に
     * NoResourceFoundException をスローする。
     * ブラウザ（Accept: text/html）からは404ページを、APIからはJSONを返す。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        logger.warn("ページが見つかりません: {}", ex.getResourcePath());

        String accept = request.getHeader("Accept") != null ? request.getHeader("Accept") : "";
        if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
            ModelAndView mav = new ModelAndView("error/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            return mav;
        }

        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "ページが見つかりません",
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * DBなどのリソースが見つからない場合の例外を処理する。
     * ブラウザ（Accept: text/html）からは404ページを、APIからはJSONを返す。
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        logger.warn("リソースが見つかりません: {}", ex.getMessage());

        String accept = request.getHeader("Accept") != null ? request.getHeader("Accept") : "";
        if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
            ModelAndView mav = new ModelAndView("error/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            return mav;
        }

        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * バリデーションエラーの例外を処理する。
     * HTTPステータス400を返す。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        logger.warn("バリデーションエラー: {}", ex.getMessage());

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("messages", errors);
        body.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * その他の予期しない例外を処理する。
     * ブラウザ（Accept: text/html）からは500ページを、APIからはJSONを返す。
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        logger.error("予期しないエラーが発生しました", ex);

        String accept = request.getHeader("Accept") != null ? request.getHeader("Accept") : "";
        if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
            ModelAndView mav = new ModelAndView("error/500");
            mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return mav;
        }

        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "サーバーエラーが発生しました",
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}