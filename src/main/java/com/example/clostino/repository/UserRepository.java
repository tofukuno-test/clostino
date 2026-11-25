package com.example.clostino.repository;

import com.example.clostino.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ユーザーリポジトリ
 * ユーザーエンティティのデータアクセスを担当する
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * メールアドレスでユーザーを検索する
     * ログイン時の認証に使用
     */
    Optional<User> findByEmail(String email);

    /**
     * メールアドレスの重複チェック
     * 新規登録時に使用
     */
    boolean existsByEmail(String email);
}