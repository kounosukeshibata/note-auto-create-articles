# プレゼンテーション層

## 概要

プレゼンテーション層はHTTPリクエスト/レスポンスの変換と、認証・認可を担う。
ビジネスロジックを持たず、アプリケーション層のユースケースを呼び出すだけに徹する。

**パッケージ:** `com.example.presentation`

## 責務

- RESTコントローラーによるHTTPリクエストの受付とレスポンスの返却
- リクエスト/レスポンスDTOのバリデーション
- 認証・認可の設定（Spring Security + Google OAuth2）
- 例外のHTTPステータスコードへのマッピング

## RESTコントローラー

### `ArticleController`

| メソッド | パス | 説明 |
|---|---|---|
| `POST` | `/api/articles/generate` | 記事を生成・保存・note一時保存する |
| `GET` | `/api/articles/{id}` | 記事を1件取得する |
| `GET` | `/api/articles` | 記事一覧を取得する |
| `DELETE` | `/api/articles/{id}` | 記事を削除する |

```kotlin
@RestController
@RequestMapping("/api/articles")
class ArticleController(
    private val generateArticleUseCase: GenerateAffiliateArticleUseCase,
    private val articleQueryService: ArticleQueryService,
) {
    @PostMapping("/generate")
    fun generate(
        @RequestBody @Validated request: GenerateArticleRequest,
    ): ResponseEntity<GenerateArticleResponse> {
        val output = generateArticleUseCase.execute(request.toInput())
        return ResponseEntity.ok(GenerateArticleResponse.from(output))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<ArticleResponse> {
        val article = articleQueryService.findById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ArticleResponse.from(article))
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<ArticleResponse>> {
        val articles = articleQueryService.findAll()
        return ResponseEntity.ok(articles.map { ArticleResponse.from(it) })
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        articleQueryService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
```

## DTOの設計

### リクエストDTO

```kotlin
data class GenerateArticleRequest(
    @field:NotBlank val theme: String,
    @field:NotBlank val imagePrompt: String,
    val targetAudience: String? = null,
    val affiliatePlatforms: List<String> = listOf("RAKUTEN", "AMAZON"),
) {
    fun toInput(): GenerateArticleInput = GenerateArticleInput(
        theme = theme,
        imagePrompt = imagePrompt,
        targetAudience = targetAudience ?: "",
        affiliatePlatforms = affiliatePlatforms.map { AffiliatePlatform.valueOf(it) },
    )
}
```

### レスポンスDTO

```kotlin
data class GenerateArticleResponse(
    val articleId: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    val affiliateLinks: List<AffiliateLinkResponse>,
) {
    companion object {
        fun from(output: GenerateArticleOutput): GenerateArticleResponse = GenerateArticleResponse(
            articleId = output.articleId,
            title = output.title,
            content = output.content,
            imageUrl = output.imageUrl,
            affiliateLinks = output.affiliateLinks.map { AffiliateLinkResponse.from(it) },
        )
    }
}
```

## バリデーション

- リクエストDTOに `@field:NotBlank`, `@field:Size`, `@field:Valid` 等のBean Validationアノテーションを付与する
- コントローラーのメソッド引数に `@Validated` を付与してバリデーションを有効化する
- バリデーションエラーは `@ExceptionHandler` でキャッチし、統一フォーマットのエラーレスポンスを返す

## エラーレスポンス形式

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "リクエストが不正です",
    "details": [
      { "field": "theme", "message": "テーマは必須です" }
    ]
  }
}
```

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = e.bindingResult.fieldErrors.map {
            ErrorDetail(field = it.field, message = it.defaultMessage ?: "")
        }
        return ResponseEntity.badRequest().body(
            ErrorResponse(error = ErrorBody(code = "VALIDATION_ERROR", message = "リクエストが不正です", details = details))
        )
    }

    @ExceptionHandler(ArticleNotFoundException::class)
    fun handleNotFound(e: ArticleNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(error = ErrorBody(code = "NOT_FOUND", message = e.message ?: ""))
        )
}
```

## 認証・認可（SecurityConfig）

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers("/api/**").authenticated()
                it.anyRequest().permitAll()
            }
            .oauth2Login { }
            .csrf { it.disable() }
        return http.build()
    }
}
```

- Google OAuth2によるログインを必須とする
- `/api/**` はすべて認証済みユーザーのみアクセス可能
- JWTトークンの検証はSpring Securityに委任する

## 設計ルール

### 含めてよいもの

| 種別 | 例 |
|---|---|
| RESTコントローラー | `ArticleController` |
| リクエスト/レスポンスDTO | `GenerateArticleRequest`, `GenerateArticleResponse` |
| グローバル例外ハンドラー | `GlobalExceptionHandler` |
| セキュリティ設定 | `SecurityConfig` |
| バリデーション | `@field:NotBlank`, `@Validated` |

### 含めてはいけないもの

| 禁止事項 | 理由 |
|---|---|
| ビジネスロジックの実装 | ドメイン層の責務 |
| ドメインオブジェクトの直接操作 | アプリケーション層を通じること |
| 永続化処理 | インフラ層の責務 |
| ドメインオブジェクトをそのままレスポンスとして返す | ドメイン層の漏洩 |
