# アプリケーション層

## 概要

アプリケーション層はユースケースのオーケストレーションを担う。
ドメインオブジェクトとインフラ層のサービスを組み合わせて、ユーザーの1つの操作（ユースケース）を完結させる。

**パッケージ:** `com.example.application`

## 責務

- ユースケースの定義と実装
- ドメインサービス・リポジトリの呼び出し順序の制御
- トランザクション境界の管理
- DTOとドメインオブジェクト間の変換（ドメイン側に変換ロジックを持たせない）

## ユースケース

### `GenerateAffiliateArticleUseCase`

ユーザー入力から記事生成・保存・note投稿までを一括実行するメインユースケース。

#### インターフェース

```kotlin
interface GenerateAffiliateArticleUseCase {
    fun execute(input: GenerateArticleInput): GenerateArticleOutput
}

data class GenerateArticleInput(
    val theme: String,
    val affiliatePlatforms: List<AffiliatePlatform>,
    val userId: UserId,
    val targetPainPoint: String = "",
    val targetIdealState: String = "",
    val storyTrigger: String = "",
    val uniqueInsight: String = "",
    val articleType: String = "一般",
    val ctaInfo: String = "",
)

data class GenerateArticleOutput(
    val articleId: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    val affiliateLinks: List<AffiliateLinkDto>,
)
```

#### 処理フロー

```kotlin
@Service
@Transactional
class GenerateAffiliateArticleUseCaseImpl(
    private val vertexAiClient: VertexAiClient,
    private val affiliateApiClient: AffiliateApiClient,
    private val linkReplacementService: LinkReplacementService,
    private val articleRepository: ArticleRepository,
    private val noteClient: NoteClient,
) : GenerateAffiliateArticleUseCase {

    override fun execute(input: GenerateArticleInput): GenerateArticleOutput {
        // 1. SEOキーワード抽出
        val keywords = vertexAiClient.extractKeywords(input.theme)

        // 2. 商品検索
        val products = affiliateApiClient.searchProducts(keywords, input.affiliatePlatforms)
        val productLinks = ProductLinks(products.map { it.toAffiliateLink() })

        // 3. 記事・画像生成
        val content = vertexAiClient.generateContent(input.theme, keywords, products)
        val image = vertexAiClient.generateImage(input.imagePrompt)

        // 4. リンク置換
        val article = Article(
            id = ArticleId.generate(),
            content = content,
            image = image,
            keywords = keywords,
            productLinks = productLinks,
        ).injectLinks(linkReplacementService)

        // 5. 永続化
        val saved = articleRepository.save(article.markAsSaved())

        // 6. note一時保存
        noteClient.postDraft(saved)
        val drafted = articleRepository.save(saved.markAsDrafted())

        return drafted.toOutput()
    }
}
```

## トランザクション管理

- ユースケースクラスに `@Transactional` を付与し、ユースケース単位でトランザクション境界を制御する
- ドメイン層・インフラ層のクラスには `@Transactional` を付与しない
- 外部API呼び出し（Vertex AI, アフィリエイトAPI, note API）はトランザクション外で行うことを検討する（サガパターン）

## 設計ルール

### 含めてよいもの

| 種別 | 例 |
|---|---|
| ユースケースインターフェース | `GenerateAffiliateArticleUseCase` |
| ユースケース実装クラス | `GenerateAffiliateArticleUseCaseImpl` |
| インプット/アウトプットDTO | `GenerateArticleInput`, `GenerateArticleOutput` |
| トランザクション制御 | `@Transactional` |
| ドメインサービスの呼び出し | `linkReplacementService.replace(...)` |

### 含めてはいけないもの

| 禁止事項 | 理由 |
|---|---|
| ビジネスルールの実装 | ドメイン層の責務 |
| SQLや永続化の実装 | インフラ層の責務 |
| HTTPリクエスト/レスポンス処理 | プレゼンテーション層の責務 |
| ドメインオブジェクトをそのまま返す | ドメイン層の漏洩 |
