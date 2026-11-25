package com.example.clostino.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ユーザー登録フォーム用DTO
 */
@Data
public class UserRegisterForm {

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "正しいメールアドレスの形式で入力してください")
    private String email;

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String password;

    @NotBlank(message = "確認用パスワードは必須です")
    private String passwordConfirm;

    private String address;

    /**
     * パスワードと確認用パスワードが一致するかチェック
     * （Lombokの@Dataはこのようなカスタムメソッドに干渉しない）
     */
    public boolean isPasswordMatch() {
        return password != null && password.equals(passwordConfirm);
    }
}