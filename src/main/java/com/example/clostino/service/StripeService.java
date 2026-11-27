package com.example.clostino.service;

import com.example.clostino.dto.PaymentIntentRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Stripe PaymentIntent の作成・確認を行うサービス。
 * テストモードの API キーを使用するため、実際の課金は発生しません。
 */
@Service
public class StripeService {

    public StripeService(@Value("${stripe.api.key}") String apiKey) {
        // アプリ起動時に Stripe SDK へ API キーを設定する
        Stripe.apiKey = apiKey;
    }

    // NOTE: Stripe.apiKey は Stripe SDK の static フィールドです。
    // Spring の @Service はデフォルトでシングルトンであるため、
    // コンストラクタは起動時に1回だけ呼ばれます。
    // 単一テナントのアプリでは問題ありませんが、
    // マルチテナント（テナントごとに異なる API キーを使い分けたい）の場合は、
    // リクエストごとに Stripe SDK に apiKey を渡す設計（RequestOptions）に変更が必要です。

    /**
     * PaymentIntent を作成して client_secret を返します。
     * フロントエンドの Stripe.js がこの client_secret を使って
     * カード情報のトークン化と支払い確認を行います。
     *
     * @param request 決済リクエスト（金額・通貨・説明）
     * @return 作成された PaymentIntent
     * @throws StripeException Stripe API との通信に失敗した場合
     */
    public PaymentIntent createPaymentIntent(PaymentIntentRequest request) throws StripeException {
        // BigDecimal → long（円単位）に変換する
        long amountInYen = request.getAmount().longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInYen)
                .setCurrency(request.getCurrency())
                .setDescription(request.getDescription())
                // カード決済を有効にする
                .addPaymentMethodType("card")
                .build();

        return PaymentIntent.create(params);
    }

    /**
     * PaymentIntent ID から最新の状態を取得します。
     * フロントエンドで支払い確認が完了した後、サーバーサイドで状態を検証するために使います。
     *
     * @param paymentIntentId PaymentIntent の ID（"pi_..." 形式）
     * @return 最新の PaymentIntent
     * @throws StripeException Stripe API との通信に失敗した場合
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}