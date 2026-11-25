-- ===========================================
-- Clostino サンプル商品データ
--
-- data.sqlがアプリ再起動で複数回実行されても
-- productsテーブルが空のときだけ投入されます。
-- ===========================================

-- INSERT INTO: SELECT結果の各行をproductsテーブルへ追加する。
INSERT INTO products (name, price, description, image_url, stock)
-- SELECT: 追加する列を、下のseedという仮テーブルから取り出す。
SELECT name, price, description, image_url, stock
FROM (
    -- 商品サンプルデータ
    VALUES
    ('クラシックTシャツ', 2980, 'シンプルで着回しやすい定番Tシャツ', 'https://placehold.jp/400x300.jpg?text=Tシャツ', 100),
    ('デニムジャケット', 8980, 'カジュアルからきれいめまで使えるデニムジャケット', 'https://placehold.jp/400x300.jpg?text=デニムジャケット', 50),
    ('スリムフィットパンツ', 5980, 'シルエットがきれいなスリムパンツ', 'https://placehold.jp/400x300.jpg?text=パンツ', 75),
    ('ニットセーター', 4980, '柔らかな肌触りのニットセーター', 'https://placehold.jp/400x300.jpg?text=ニット', 60),
    ('チェックシャツ', 3980, '定番のチェック柄シャツ', 'https://placehold.jp/400x300.jpg?text=シャツ', 80),
    ('ジーンズ', 6980, 'スタンダードなストレートジーンズ', 'https://placehold.jp/400x300.jpg?text=ジーンズ', 45),
    ('パーカー', 4480, 'リラックスフィットのプルオーバーパーカー', 'https://placehold.jp/400x300.jpg?text=パーカー', 90),
    ('レザーベルト', 2480, '上質な本革ベルト', 'https://placehold.jp/400x300.jpg?text=ベルト', 120),
    ('スニーカー', 5480, 'どんなスタイルにも合わせやすいカジュアルスニーカー', 'https://placehold.jp/400x300.jpg?text=スニーカー', 70),
    ('ウールコート', 15980, '上質なウール素材のロングコート', 'https://placehold.jp/400x300.jpg?text=コート', 30),
    ('ボーダーTシャツ', 3280, 'マリンスタイルのボーダーTシャツ', 'https://placehold.jp/400x300.jpg?text=ボーダー', 85),
    ('チノパンツ', 4980, 'ビジネスカジュアルにも使えるチノパン', 'https://placehold.jp/400x300.jpg?text=チノパン', 65)
-- AS seed(...) で、 FROM (VALUES...) で作った仮テーブルにseedという名前と列名を付ける。
) AS seed(name, price, description, image_url, stock)
-- WHERE NOT EXISTS: productsに1件でも存在する場合はSELECT結果を0件にして、INSERTをスキップする。
WHERE NOT EXISTS (SELECT 1 FROM products);
-- テストユーザーの追加
-- パスワードは「password123」をBCryptでハッシュ化したもの
INSERT INTO users (email, password, name, address, created_at) VALUES
('test@example.com', '$2a$10$4eN6fRgGeuFnj3iJRcS64eaNiOgteHr5A0YO3LmTlEyHfSL2aVu72', 'テストユーザー', '東京都渋谷区1-1-1', CURRENT_TIMESTAMP),
('tanaka@example.com', '$2a$10$4eN6fRgGeuFnj3iJRcS64eaNiOgteHr5A0YO3LmTlEyHfSL2aVu72', '田中太郎', '大阪府大阪市2-2-2', CURRENT_TIMESTAMP);