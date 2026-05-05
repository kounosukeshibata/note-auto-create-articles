# インフラ層

## 概要

インフラ層は技術的な実装の詳細を隔離する。ドメイン層が定義したインターフェースを実装し、
外部システム（DB・AI API・アフィリエイトAPI・note API）との通信を担う。

**パッケージ:** `com.example.infrastructure`

## 責務

- ドメインのリポジトリインターフェースの実装
- 外部APIクライアントの実装
- Spring Beanの設定・DI
- エラーハンドリングとリトライ処理

## 永続化

### `FirestoreArticleRepository`

`ArticleRepository` インターフェースのFirestore実装。

```kotlin
@Repository
class FirestoreArticleRepository(
    private val firestore: Firestore,
) : ArticleRepository {

    private val collection = firestore.collection("articles")

    override fun save(article: Article): Article {
        val doc = article.toDocument()
        collection.document(article.id.value.toString()).set(doc).get()
        return article
    }

    override fun findById(id: ArticleId): Article? {
        val snapshot = collection.document(id.value.toString()).get().get()
        return if (snapshot.exists()) snapshot.toArticle() else null
    }

    override fun findAll(): List<Article> =
        collection.get().get().documents.map { it.toArticle() }

    override fun delete(id: ArticleId) {
        collection.document(id.value.toString()).delete().get()
    }
}
```

## 外部クライアント

### `VertexAiClient`

Vertex AI (Gemini 1.5 Pro / Imagen 3) との通信を担う。

| メソッド | 説明 |
|---|---|
| `extractKeywords(theme)` | テーマからSEOキーワードを抽出（Gemini） |
| `generateContent(theme, keywords, products)` | 記事本文を生成（Gemini） |
| `generateImage(prompt)` | アイキャッチ画像を生成（Imagen 3） |

```kotlin
@Component
class VertexAiClient(
    private val predictionServiceClient: PredictionServiceClient,
    @Value("\${gcp.project-id}") private val projectId: String,
    @Value("\${gcp.location}") private val location: String,
) {
    fun extractKeywords(theme: String): List<SeoKeyword> { ... }
    fun generateContent(theme: String, keywords: List<SeoKeyword>, products: List<ProductInfo>): Content { ... }
    fun generateImage(prompt: String): Image { ... }
}
```

### `RakutenAffiliateClient`

楽天アフィリエイトAPIとの通信を担う。

| メソッド | 説明 |
|---|---|
| `searchProducts(keywords)` | キーワードで商品を検索し `ProductInfo` リストを返す |
| `generateLink(productId)` | 商品IDからアフィリエイトリンクを生成する |

```kotlin
@Component
class RakutenAffiliateClient(
    private val restClient: RestClient,
    @Value("\${rakuten.api-key}") private val apiKey: String,
) : AffiliateApiClient {
    override fun searchProducts(keywords: List<SeoKeyword>): List<ProductInfo> { ... }
}
```

### `AmazonAffiliateClient`

Amazon Product Advertising APIとの通信を担う。楽天と同一の `AffiliateApiClient` インターフェースを実装する。

```kotlin
@Component
class AmazonAffiliateClient(
    private val restClient: RestClient,
    @Value("\${amazon.access-key}") private val accessKey: String,
    @Value("\${amazon.secret-key}") private val secretKey: String,
    @Value("\${amazon.partner-tag}") private val partnerTag: String,
) : AffiliateApiClient {
    override fun searchProducts(keywords: List<SeoKeyword>): List<ProductInfo> { ... }
}
```

### `NoteClient`

note APIとの通信を担う。記事の一時保存（下書き）を実行する。

```kotlin
@Component
class NoteClient(
    private val restClient: RestClient,
    @Value("\${note.api-base-url}") private val baseUrl: String,
) {
    fun postDraft(article: Article): NotePostResult { ... }
}
```

## 認証・設定管理

- すべてのAPIキーは **Google Cloud Secret Manager** で管理し、ハードコード厳禁
- Spring Boot の `@Value` でSecret Managerの値を注入する（`spring-cloud-gcp-starter-secretmanager` 使用）
- GCPリソースへのアクセスは **Workload Identity** を使用し、サービスアカウントキーファイルを使用しない

```yaml
# application.yml（キー名のみ。値はSecret Managerから取得）
rakuten:
  api-key: ${sm://projects/PROJECT_ID/secrets/rakuten-api-key/versions/latest}
amazon:
  access-key: ${sm://projects/PROJECT_ID/secrets/amazon-access-key/versions/latest}
  secret-key: ${sm://projects/PROJECT_ID/secrets/amazon-secret-key/versions/latest}
```

## エラーハンドリング方針

| 状況 | 対応 |
|---|---|
| 外部APIの一時的な障害（5xx） | Exponential Backoff でリトライ（最大3回） |
| 外部APIの認証エラー（401/403） | リトライせず即時例外をスロー |
| Firestore書き込み失敗 | アプリケーション層でロールバック |
| Vertex AIのレスポンス不正 | `InfrastructureException` をスローし、アプリケーション層でハンドル |

## 設計ルール

### 含めてよいもの

| 種別 | 例 |
|---|---|
| リポジトリ実装 | `FirestoreArticleRepository` |
| 外部APIクライアント | `VertexAiClient`, `RakutenAffiliateClient` |
| Spring設定クラス | `SecurityConfig`, `FirestoreConfig` |
| データマッパー | `ArticleDocument.toArticle()` |

### 含めてはいけないもの

| 禁止事項 | 理由 |
|---|---|
| ビジネスルールの実装 | ドメイン層の責務 |
| ユースケースの組み合わせロジック | アプリケーション層の責務 |
| HTTPリクエスト/レスポンス処理 | プレゼンテーション層の責務 |
