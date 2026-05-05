# ドメイン概要

## このドメインについて

本プロジェクトは「**収益化記事生成・投稿ドメイン**」を中心に設計されている。
ユーザーが入力した情報をもとに、AIとアフィリエイトエコシステムを組み合わせて高品質な収益化記事を自動生成し、noteへ投稿するまでのビジネスロジックを管理する。

## 主要な集約（Aggregate）

| 集約 | 説明 |
|---|---|
| `Article` | ドメインの中心集約。本文・画像・SEOキーワード・商品リンクを内包する |

## エンティティ（Entity）

| エンティティ | 説明 |
|---|---|
| `Article` | 記事集約のルートエンティティ。`ArticleId` で一意に識別される |

## 値オブジェクト（Value Object）

| 値オブジェクト | 説明 |
|---|---|
| `Content` | 記事の本文テキスト。不変であり同一性は値そのもので判断 |
| `Image` | AIが生成したアイキャッチ画像（URL または バイナリ参照） |
| `SeoKeyword` | SEO最適化されたキーワード。Geminiが抽出する |
| `ProductInfo` | 商品名・価格・カテゴリ等の商品情報 |
| `AffiliateLink` | 楽天またはAmazonのアフィリエイトトラッキングURL |
| `ProductLinks` | `AffiliateLink` の集合（記事1件に含まれる商品リンク群） |
| `ArticleId` | 記事の識別子（UUID） |

## ドメインサービス（Domain Service）

| ドメインサービス | 説明 |
|---|---|
| `LinkReplacementService` | 生成テキスト内のプレースホルダーをアフィリエイトリンクへ置換するロジック。複数の集約にまたがるため、ドメインサービスとして定義 |

## リポジトリインターフェース（Repository Interface）

| インターフェース | 説明 |
|---|---|
| `ArticleRepository` | `Article` 集約の永続化・取得を抽象化。実装は Infrastructure 層に持つ |

## 関連ドキュメント

- [ユビキタス言語辞書](./domain/ubiquitous.md)
- [ドメインモデル図](./domain/domain-model-diagram.md)
- [ドメイン層の実装ルール](./layer/domain.md)
- [アプリケーション層の実装ルール](./layer/application.md)
- [インフラ層の実装ルール](./layer/infrastructure.md)
- [プレゼンテーション層の実装ルール](./layer/presentation.md)
