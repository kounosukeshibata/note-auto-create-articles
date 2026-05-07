---
name: Project Note Auto-Post Overview
description: アフィリエイト記事自動生成サービスのバックエンド実装の概要（集約、レイヤー構成、スタブ戦略、現在の実装状態）
type: project
---

アフィリエイト記事自動生成サービス。ユーザーがテーマを入力すると Gemini 2.5 Flash で記事を生成し、Amazon アフィリエイトリンクを自動挿入して DB に保存する。

**Why:** バックエンドは Kotlin/Spring Boot 3.x + DDD で構築。インフラ依存（DB/Gemini API/Amazon API）はスタブで差し替え可能にして開発効率を確保。

**How to apply:**

## ドメイン構造
- `Article` が唯一の集約ルート（`UserId`, `Content`, `Image`, `SeoKeyword[]`, `ProductLinks`, `ArticleStatus` を内包）
- ドメイン層に Spring アノテーション禁止（`@Service`, `@Repository` 不可）
- `LinkReplacementService` は Spring Bean だが、`DomainConfig`（インフラ config）で `@Bean` 登録することでドメイン層への Spring 依存を排除

## スタブ切替（@ConditionalOnProperty）
| プロパティ | true（デフォルト） | false（本番） |
|---|---|---|
| `vertex.ai.stub` | `StubVertexAiClient` | `RealVertexAiClient`（Gemini 2.5 Flash） |
| `amazon.stub` | `StubAmazonAffiliateClient` | `RealAmazonAffiliateClient` |
| `storage.type=memory`（デフォルト） | `InMemoryArticleRepository` | 将来: Firestore実装 |

## 重要な実装パターン
- `AffiliateApiClient.searchProducts(keywords)` は `ProductInfo` リストを返す
- ユースケース内部では `PlatformProduct(platform, productInfo)` ペアとして管理してプラットフォーム情報を保持
- 還元率降順ソートして上位 5 件のみ使用（`MAX_PRODUCTS = 5`）
- 両 affiliate API が全て失敗した場合のみ `AffiliateApiUnavailableException` をスロー（フォールバック設計）

## 現在の UseCase フロー（GenerateAffiliateArticleUseCaseImpl）
1. `extractKeywords(theme)` → `List<SeoKeyword>`
2. `searchProductsWithFallback(clients, keywords)` → `List<PlatformProduct>`
3. `generateContent(theme, keywords, products, ..., wordCount)` → `Content`（`{{product_link_N}}` 含む）
4. `generateImage(theme)` → `Image`
5. `buildProductLinks(topProducts)` → `ProductLinks`
6. `article.injectLinks(linkReplacementService)` → リンク置換済み `Article`
7. `articleRepository.save(article.markAsSaved())` → `status=SAVED` で返却

> **note投稿は未実装**。`NoteClient` は将来の自動投稿で使用予定。現在は DB 保存（`status=SAVED`）で完了。

## API 入力項目（GenerateArticleInput）
- `theme`: 必須
- `affiliatePlatforms`: デフォルト AMAZON（RAKUTEN はドメイン定義済みだが未実装）
- `targetPainPoint`, `targetIdealState`, `storyTrigger`, `uniqueInsight`, `articleType`, `ctaInfo`: 任意
- `wordCount`: `Int?`（1000/2000/3000/4000）→ Gemini プロンプトへ指示、maxTokens を動的調整

## RealVertexAiClient の重要な挙動
- `wordCount` に応じた動的 maxTokens: 4000字→12000, 3000字→9000, 2000字→6000, 未指定→4000
- `thinkingBudget: 0` で Gemini の thinking モードを無効化（レスポンス速度優先）
- レスポンスから `[タイトル]` 行を探してタイトルを抽出（`parseContent`）
- API 接続失敗時はフォールバックテキストの `Content` を返す（例外を外部に出さない）
