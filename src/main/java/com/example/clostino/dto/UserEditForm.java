package com.example.clostino.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ユーザー情報編集フォーム用DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEditForm {

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "正しいメールアドレスの形式で入力してください")
    private String email;

    private String address;
}