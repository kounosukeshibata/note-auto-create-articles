---
name: design.md の設計判断記録
description: docs/design.md に記録した主要な設計判断とその根拠の要約
type: project
---

docs/design.md（478行）に以下の設計判断を記録した。

**Article集約をルートにした理由:** Content/Image/SeoKeyword/ProductLinksはすべて同一トランザクション内で生まれ消滅する。「プレースホルダー数とリンク数の一致」という不変条件をArticle内で管理するため。

**値オブジェクトの設計判断:**
- ProductInfo: 商品スナップショットであり独自アイデンティティを持たないため値オブジェクト
- Content: 変更ではなく再生成が自然な操作モデルのため値オブジェクト
- SeoKeyword: @JvmInline value classで空文字不変条件を型レベルで表現

**LinkReplacementServiceをドメインサービスにした理由:** ContentとProductLinksの両方を参照するため、どちらか一方の値オブジェクトに責務を持たせられない。

**アフィリエイト選択ロジック:** 楽天・Amazon両方検索し還元率が高い方を採用。UseCaseでマージ・ソートする（ドメインではなくUseCaseで行う理由: 選択基準の変更に柔軟対応）。

**フォールバック方針:**
- 片方のアフィリエイトAPI障害 → もう一方で継続
- Imagen障害 → デフォルト画像で継続
- note API障害 → DB保存のみでstatus=SAVEDとして返す

**Why:** 実装前の設計合意ドキュメントとして作成。

**How to apply:** 設計相談時はまずdocs/design.mdを参照し、矛盾がないか確認してから回答する。
