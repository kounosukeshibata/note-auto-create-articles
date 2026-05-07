# インフラ層

## 概要

インフラ層は技術的な実装の詳細を隔離する。ドメイン層が定義したインターフェースを実装し、
外部システム（DB・AI API・アフィリエイトAPI・note API）との通信を担う。

**パッケージ:** `com.example.infrastructure`

## 責務

- ドメインのリポジトリインターフェースの実装
- 外部APIクライアントの実装
- JWT生成・検証
- Spring Beanの設定・DI
- エラーハンドリングとリトライ処理

## 永続化

### `InMemoryArticleRepository`（開発・テスト用）

`storage.type=memory`（デフォルト）のとき有効。ConcurrentHashMap でインメモリ管理。

```kotlin
@Repository
@ConditionalOnProperty(name = ["storage.type"], havingValue = "memory", matchIfMissing = true)
class InMemoryArticleRepository : ArticleRepository {
    private val store = ConcurrentHashMap<ArticleId, Article>()

    override fun save(article: Article) = article.also { store[it.id] = it }
    override fun findById(id: ArticleId) = store[id]
    override fun findAllByUserId(userId: UserId) = store.values.filter { it.userId == userId }
    override fun delete(id: ArticleId) { store.remove(id) }
}
```

### `InMemoryUserRepository`

ユーザー情報のインメモリ管理。email と id の両方で O(1) 検索できるよう2つのマップを保持。

```kotlin
@Repository
class InMemoryUserRepository : UserRepository {
    private val byId = ConcurrentHashMap<UserId, User>()
    private val byEmail = ConcurrentHashMap<String, User>()

    override fun save(user: User) = user.also { byId[it.id] = it; byEmail[it.email] = it }
    override fun findByEmail(email: String) = byEmail[email]
    override fun findById(id: UserId) = byId[id]
}
```

> **Supabase（本番）への切り替え:**  
> `storage.type=supabase` に設定し、`spring.datasource.url` に Supabase の JDBC URL を設定することで Spring Data JPA 実装に切り替える。

## 認証（JWT）

### `JwtTokenProvider`

`TokenProvider` インターフェース（アプリケーション層定義）の実装。JJWT 0.12.x を使用。

```kotlin
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long,
) : TokenProvider {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    override fun generateToken(userId: String): String =
        Jwts.builder().subject(userId).issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key).compact()

    override fun validateToken(token: String): String? = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject
    } catch (_: JwtException) { null }
}
```

### `JwtAuthFilter`

各リクエストの `Authorization: Bearer {token}` ヘッダーを検証し、userId を SecurityContext にセットする。

## 外部クライアント

### `VertexAiClient`

Vertex AI (Gemini 1.5 Pro / Imagen 3) との通信を担う。

| メソッド | 説明 |
|---|---|
| `extractKeywords(theme)` | テーマからSEOキーワードを抽出 |
| `generateContent(theme, keywords, products, targetPainPoint, targetIdealState, storyTrigger, uniqueInsight, articleType, ctaInfo)` | SEO最適化プロンプトで記事本文を生成 |
| `generateImage(prompt)` | アイキャッチ画像を生成 |

**プロンプト管理:** AIに渡すプロンプトは `src/main/resources/prompts/` 配下のMarkdownファイルで管理し、`PromptLoader` でクラスパスから読み込む。

| プロンプトファイル | 用途 |
|---|---|
| `prompts/vertex/content-generation.md` | 記事生成プロンプト（SEO特化） |
| `prompts/vertex/content-generation-product-item.md` | 商品紹介テンプレート |
| `prompts/vertex/keyword-extraction.md` | キーワード抽出プロンプト |
| `prompts/gemini/amazon-product-suggestion.md` | Amazon商品提案プロンプト |

### `AmazonAffiliateClient`

Amazon Product Advertising API との通信を担う。`AffiliateApiClient` インターフェースを実装。

```kotlin
@Component
@ConditionalOnProperty(name = ["amazon.stub"], havingValue = "false", matchIfMissing = false)
class AmazonAffiliateClient(...) : AffiliateApiClient {
    override fun searchProducts(keywords: List<SeoKeyword>): List<ProductInfo> { ... }
}
```

### `NoteClient`

note API との通信を担う。記事の一時保存（下書き）を実行する。

## 認証・設定管理

- すべての APIキー・シークレットは **Google Cloud Secret Manager** で管理し、ハードコード厳禁
- `JWT_SECRET` は Secret Manager から環境変数として Cloud Run に渡す
- Supabase の JDBC URL・ユーザー・パスワードも Secret Manager で管理

```yaml
# application.yml（本番設定例）
jwt:
  secret: ${JWT_SECRET}
  expiration-ms: 86400000

spring:
  datasource:
    url: ${SUPABASE_DB_URL}
    username: ${SUPABASE_DB_USER}
    password: ${SUPABASE_DB_PASSWORD}
```

## エラーハンドリング方針

| 状況 | 対応 |
|---|---|
| 外部APIの一時的な障害（5xx） | Exponential Backoff でリトライ（最大3回） |
| 外部APIの認証エラー（401/403） | リトライせず即時例外をスロー |
| JWT検証失敗 | `null` を返し、Spring Security が 401 を返す |
| Vertex AIのレスポンス不正 | `InfrastructureException` をスローし、アプリケーション層でハンドル |

## 設計ルール

### 含めてよいもの

| 種別 | 例 |
|---|---|
| リポジトリ実装 | `InMemoryArticleRepository`, JPA実装 |
| 外部APIクライアント | `VertexAiClient`, `AmazonAffiliateClient` |
| JWT実装 | `JwtTokenProvider`, `JwtAuthFilter` |
| Spring設定クラス | `SecurityConfig`, `DomainConfig` |

### 含めてはいけないもの

| 禁止事項 | 理由 |
|---|---|
| ビジネスルールの実装 | ドメイン層の責務 |
| ユースケースの組み合わせロジック | アプリケーション層の責務 |
| HTTPリクエスト/レスポンス処理 | プレゼンテーション層の責務 |
