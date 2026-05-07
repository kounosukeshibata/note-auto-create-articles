---
name: design.md の設計判断記録
description: docs/design.md に記録した主要な設計判断とその根拠の要約（現在の実装と一致するよう更新済み）
type: project
---

docs/design.md に以下の設計判断を記録している。

## Article 集約設計
**Article集約をルートにした理由:** Content/Image/SeoKeyword/ProductLinksはすべて同一トランザクション内で生まれ消滅する。「`{{product_link_N}}` プレースホルダー数とリンク数の一致」という不変条件をArticle内で管理するため。

## 値オブジェクトの設計判断
- `ProductInfo`: 商品スナップショットであり独自アイデンティティを持たないため値オブジェクト。`commissionRate` フィールドを持ち還元率比較に使用
- `Content`: 変更ではなく再生成が自然な操作モデルのため値オブジェクト（`copy()` による新インスタンス生成）
- `SeoKeyword`: `@JvmInline value class` で空文字不変条件を型レベルで表現
- `AffiliateLink`: `url`, `trackingId`, `platform`, `productInfo` を内包する値オブジェクト

## LinkReplacementService をドメインサービスにした理由
ContentとProductLinksの両方を参照するため、どちらか一方の値オブジェクトに責務を持たせられない。

## アフィリエイト選択ロジック（現在の実装）
Amazon のみ実装中（RAKUTEN は将来対応予定）。`commissionRate` 降順ソートで上位5件を採用。ユースケース内でソートを行う（ドメインではなくUseCaseで行う理由: 選択基準の変更に柔軟対応）。

## フォールバック方針（現在の実装）
- 全アフィリエイトAPI障害 → `AffiliateApiUnavailableException`（503）
- Gemini API 障害 → フォールバックテキストで Content を構築（例外を外部に出さない）
- 画像生成障害 → placehold.co のプレースホルダー画像で継続
- note API（将来）→ DB保存のみで `status=SAVED` として返す

## 認証（現在の実装）
Spring Security + JWT（email/password）。Google OAuth2 ではない。

## UseCase の最終出力
`status=SAVED`。`status=NOTE_DRAFTED` は `NoteClient` 実装後に到達可能になる。

**Why:** 実装前の設計合意ドキュメントとして作成し、実装との乖離が発生した箇所も随時更新している。

**How to apply:** 設計相談時はまずdocs/design.mdを参照し、矛盾がないか確認してから回答する。ただし設計文書より実際のコードを優先する。
