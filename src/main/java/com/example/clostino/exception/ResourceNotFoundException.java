package com.example.clostino.exception;

/**
 * 指定されたリソースが見つからない場合にスローされる例外。
 * 商品、注文、ユーザーなど、各種リソースの検索で使用する。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}