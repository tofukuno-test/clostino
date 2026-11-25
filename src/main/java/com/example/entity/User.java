package com.example.clostino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティ
 * 会員情報を管理するエンティティクラス
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "正しいメールアドレスの形式で入力してください")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "名前は必須です")
    @Column(nullable = false)
    private String name;

    @Column
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * エンティティ保存前に createdAt を自動設定する。
     * コンストラクタで初期化すると @NoArgsConstructor と競合するため、
     * @PrePersist を使用する。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}