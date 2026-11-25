package com.example.clostino.service;

import com.example.clostino.entity.User;
import com.example.clostino.exception.ResourceNotFoundException;
import com.example.clostino.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ユーザーサービス
 * ユーザー関連のビジネスロジックを担当する
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * IDでユーザーを検索する
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません（ID: " + id + "）"));
    }

    /**
     * メールアドレスでユーザーを検索する
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * メールアドレスの重複チェック
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 新規ユーザーを登録する
     * パスワードは自動的にハッシュ化される
     */
    @Transactional
    public User register(User user) {
        // メールアドレスの重複チェック
        if (existsByEmail(user.getEmail())) {
            throw new IllegalStateException("このメールアドレスは既に登録されています");
        }

        // パスワードをハッシュ化
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        User savedUser = userRepository.save(user);
        logger.info("新規ユーザーを登録しました: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * ユーザー情報を更新する
     */
    @Transactional
    public User update(Long id, User updatedUser) {
        User existingUser = findById(id);

        existingUser.setName(updatedUser.getName());
        existingUser.setAddress(updatedUser.getAddress());

        // メールアドレスの変更がある場合は重複チェック
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (existsByEmail(updatedUser.getEmail())) {
                throw new IllegalStateException("このメールアドレスは既に使用されています");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        User savedUser = userRepository.save(existingUser);
        logger.info("ユーザー情報を更新しました: userId={}", savedUser.getId());

        return savedUser;
    }

    /**
     * パスワードを照合する
     * ログイン認証に使用
     */
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * パスワードを変更する
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = findById(userId);
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        logger.info("パスワードを変更しました: userId={}", userId);
    }
}