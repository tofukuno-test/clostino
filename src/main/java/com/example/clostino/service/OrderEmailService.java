package com.example.clostino.service;

import com.example.clostino.entity.Order;
import com.example.clostino.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * 注文確認メールを送信するサービス。
 * Mailtrap（開発環境）または本番 SMTP サーバーを使用します。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final JavaMailSender mailSender;

    /**
     * 注文確認メールを送信します。
     *
     * @param order   作成済みの注文エンティティ
     * @param toEmail 送信先メールアドレス（ユーザーのメールアドレス）
     */
    public void sendOrderConfirmation(Order order, String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@clostino.example.com");
            message.setTo(toEmail);
            message.setSubject("【Clostino】ご注文確認 - 注文番号: " + order.getId());
            message.setText(buildEmailBody(order));

            mailSender.send(message);
            log.info("注文確認メールを送信しました。注文ID: {}, 宛先: {}", order.getId(), toEmail);

        } catch (Exception e) {
            // メール送信失敗は注文処理をブロックしない（ログのみ記録）
            log.error("注文確認メールの送信に失敗しました。注文ID: {}", order.getId(), e);
        }
    }

    /**
     * メール本文を組み立てます。
     */
    private String buildEmailBody(Order order) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.JAPAN);
        StringBuilder sb = new StringBuilder();

        sb.append(order.getUser().getName()).append(" 様\n\n");
        sb.append("このたびは Clostino をご利用いただきありがとうございます。\n");
        sb.append("以下の内容でご注文を承りました。\n\n");

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("■ 注文番号: ").append(order.getId()).append("\n");
        sb.append("■ お届け先: ").append(order.getShippingAddress()).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append("【ご注文商品】\n");
        int subtotal = 0;
        for (OrderItem item : order.getOrderItems()) {
            int lineTotal = item.getProduct().getPrice() * item.getQuantity();
            subtotal += lineTotal;
            sb.append(String.format("  %-30s × %d個   ¥%s\n",
                    item.getProduct().getName(),
                    item.getQuantity(),
                    fmt.format(lineTotal)));
        }

        int tax = (int) (subtotal * 0.1);
        int total = subtotal + tax;

        sb.append("\n");
        sb.append(String.format("  小計（税抜）: ¥%s\n", fmt.format(subtotal)));
        sb.append(String.format("  消費税（10%%）: ¥%s\n", fmt.format(tax)));
        sb.append(String.format("  合計（税込）: ¥%s\n", fmt.format(total)));

        sb.append("\n");
        sb.append("ご注文の詳細は以下の URL からご確認いただけます。\n");
        sb.append("http://localhost:8080/orders/").append(order.getId()).append("\n\n");
        sb.append("引き続き Clostino をよろしくお願いいたします。\n");
        sb.append("Clostino カスタマーサポート\n");

        return sb.toString();
    }
}