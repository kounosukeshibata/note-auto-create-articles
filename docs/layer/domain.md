# ドメイン層

## 概要

ドメイン層はDDDにおける中核であり、ビジネスルールとドメインロジックのみを担う。
インフラ技術（DB・外部API・フレームワーク）への依存を一切持たず、ビジネス的に正しいモデルを表現することに集中する。

**パッケージ:** `com.example.domain`

## 責務

- 集約・エンティティ・値オブジェクトによるビジネスルールの表現
- ドメインサービスによる集約をまたぐロジックの実装
- リポジトリインターフェースの定義（実装はインフラ層）

## 集約（Aggregate）

### `Article` 集約

記事ドメインの中心集約。すべての操作はこの集約ルートを通して行う。

```kotlin
data class Article(
    val id: ArticleId,
    val userId: UserId,
    val content: Content,
    val image: Image,
    val keywords: List<SeoKeyword>,
    val productLinks: ProductLinks,
    val status: ArticleStatus = ArticleStatus.GENERATED,
) {
    fun injectLinks(service: LinkReplacementService): Article =
        copy(content = service.replace(content, productLinks))

    fun markAsSaved(): Article = copy(status = ArticleStatus.SAVED)

    fun markAsDrafted(): Article = copy(status = ArticleStatus.NOTE_DRAFTED)
}

enum class ArticleStatus { GENERATED, SAVED, NOTE_DRAFTED }
```

### `User` 集約

ユーザー認証ドメインの集約。email/password 認証を管理する。

```kotlin
data class User(
    val id: UserId,
    val email: String,
    val passwordHash: String,
    val name: String,
)
```

## 値オブジェクト（Value Object）

不変であり、同一性は値そのもので判断する。`copy()` でのみ状態を変更する。

### `ArticleId`

```kotlin
@JvmInline
value class ArticleId(val value: UUID) {
    companion object {
        fun generate(): ArticleId = ArticleId(UUID.randomUUID())
    }
}
```

### `UserId`

```kotlin
data class UserId(val value: UUID) {
    companion object {
        fun generate() = UserId(UUID.randomUUID())
        fun of(str: String) = UserId(UUID.fromString(str))
    }
}
```

### `Content`

```kotlin
data class Content(
    val title: String,
    val text: String,
) {
    init {
        require(title.isNotBlank()) { "タイトルは必須です" }
    }
}
```

### `SeoKeyword`

```kotlin
@JvmInline
value class SeoKeyword(val value: String) {
    init {
        require(value.isNotBlank()) { "キーワードは空にできません" }
    }
}
```

### `AffiliateLink`

```kotlin
data class AffiliateLink(
    val url: String,
    val trackingId: String,
    val platform: AffiliatePlatform,
    val productInfo: ProductInfo,
)

enum class AffiliatePlatform { AMAZON }
```

### `ProductInfo`

```kotlin
data class ProductInfo(
    val name: String,
    val price: BigDecimal,
    val category: String,
    val thumbnailUrl: String,
    val commissionRate: Double,
)
```

### `ProductLinks`

```kotlin
data class ProductLinks(val links: List<AffiliateLink> = emptyList()) {
    fun add(link: AffiliateLink): ProductLinks = copy(links = links + link)
}
```

### `Image`

```kotlin
data class Image(
    val url: String,
    val altText: String,
)
```

## ドメインサービス（Domain Service）

複数の集約にまたがるロジック、または集約の責務として自然でないロジックをドメインサービスに置く。

### `LinkReplacementService`

```kotlin
class LinkReplacementService {
    fun replace(content: Content, productLinks: ProductLinks): Content {
        var text = content.text
        productLinks.links.forEachIndexed { index, link ->
            text = text.replace("{{product_link_$index}}", link.url)
        }
        return content.copy(text = text)
    }
}
```

## リポジトリインターフェース（Repository Interface）

ドメイン層でインターフェースを定義し、実装はインフラ層に委ねる。

```kotlin
interface ArticleRepository {
    fun save(article: Article): Article
    fun findById(id: ArticleId): Article?
    fun findAllByUserId(userId: UserId): List<Article>
    fun delete(id: ArticleId)
}

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun findById(id: UserId): User?
}
```

## ドメイン例外

| 例外クラス | 説明 |
|---|---|
| `ArticleNotFoundException` | 指定IDの記事が存在しない |
| `DuplicateUserException` | 同一メールアドレスのユーザーが既に存在する |
| `InvalidCredentialsException` | メールアドレスまたはパスワードが不正 |

## 設計ルール

### 含めてよいもの

| 種別 | 例 |
|---|---|
| 集約・エンティティ | `Article`, `User` |
| 値オブジェクト | `Content`, `AffiliateLink`, `SeoKeyword`, `UserId` など |
| ドメインサービス | `LinkReplacementService` |
| リポジトリインターフェース | `ArticleRepository`, `UserRepository` |
| ドメインイベント（将来） | `ArticleGeneratedEvent` |
| ドメイン例外 | `ArticleNotFoundException`, `DuplicateUserException` |

### 含めてはいけないもの

| 禁止事項 | 理由 |
|---|---|
| `@Repository`, `@Service` などSpringアノテーション | インフラ依存 |
| 外部APIのクライアントクラス | インフラ依存 |
| HTTPリクエスト/レスポンスのDTO | プレゼンテーション依存 |
| トランザクション管理（`@Transactional`） | インフラ依存 |
| ロギング実装（`LoggerFactory`） | インフラ依存 |
