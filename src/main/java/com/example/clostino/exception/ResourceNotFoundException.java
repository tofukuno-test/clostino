package com.example.clostino.exception;

/**
 * 指定されたリソースが見つからない場合にスローされる例外。
 * 商品、注文、ユーザーなど、各種リソースの検索で使用する。
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * エラーメッセージを指定して例外を作成する。
     *
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * エラーメッセージと原因となる例外を指定して例外を作成する。
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}