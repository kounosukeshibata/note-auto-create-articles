# プレゼンテーション層

## 概要

プレゼンテーション層はHTTPリクエスト/レスポンスの変換と、認証・認可を担う。
ビジネスロジックを持たず、アプリケーション層のユースケースを呼び出すだけに徹する。

**パッケージ:** `com.example.presentation`

## 責務

- RESTコントローラーによるHTTPリクエストの受付とレスポンスの返却
- リクエスト/レスポンスDTOのバリデーション
- 認証・認可の設定（Spring Security + JWT stateless）
- 例外のHTTPステータスコードへのマッピング

## RESTコントローラー

### `AuthController` — `/api/auth/**`（認証不要）

| メソッド | パス | 説明 |
|---|---|---|
| `POST` | `/api/auth/register` | 新規ユーザー登録。JWT トークンを返す |
| `POST` | `/api/auth/login` | ログイン。JWT トークンを返す |

### `UserController` — `/api/**`（要認証）

| メソッド | パス | 説明 |
|---|---|---|
| `GET` | `/api/me` | JWT から現在のユーザー情報を返す |

### `ArticleController` — `/api/articles/**`（要認証）

| メソッド | パス | 説明 |
|---|---|---|
| `POST` | `/api/articles/generate` | 記事を生成・保存・note一時保存する |
| `GET` | `/api/articles/{id}` | 記事を1件取得する |
| `GET` | `/api/articles` | ログインユーザーの記事一覧を取得する |
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
        authentication: Authentication,
    ): ResponseEntity<GenerateArticleResponse> {
        val userId = UserId.of(authentication.principal as String)
        val output = generateArticleUseCase.execute(request.toInput(userId))
        return ResponseEntity.ok(output.toResponse())
    }

    @GetMapping
    fun findAll(authentication: Authentication): ResponseEntity<List<ArticleSummaryResponse>> {
        val articles = articleQueryService.findAllByUserId(authentication.principal as String)
        return ResponseEntity.ok(articles.map { it.toSummaryResponse() })
    }
}
```

## DTOの設計

### 認証リクエスト/レスポンス

```kotlin
data class RegisterRequest(
    @field:Email @field:NotBlank val email: String = "",
    @field:NotBlank @field:Size(min = 8) val password: String = "",
    @field:NotBlank val name: String = "",
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String = "",
    @field:NotBlank val password: String = "",
)

data class AuthResponse(val token: String, val userId: String, val email: String, val name: String)
data class MeResponse(val userId: String, val email: String, val name: String)
```

### 記事リクエスト/レスポンス

```kotlin
data class GenerateArticleRequest(
    @field:NotBlank val theme: String,
    val affiliatePlatforms: List<String> = listOf("AMAZON"),
    val targetPainPoint: String? = null,
    val targetIdealState: String? = null,
    val storyTrigger: String? = null,
    val uniqueInsight: String? = null,
    val articleType: String? = "一般",
    val ctaInfo: String? = null,
)
```

## バリデーション

- リクエストDTOに `@field:NotBlank`, `@field:Size`, `@field:Email` 等のBean Validationアノテーションを付与する
- コントローラーのメソッド引数に `@Validated` を付与してバリデーションを有効化する
- バリデーションエラーは `@ExceptionHandler` でキャッチし、統一フォーマットのエラーレスポンスを返す

## エラーレスポンス形式

フロントエンドの期待するフォーマット（`err?.error?.code`）に合わせた統一構造。

```json
{
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "メールアドレスまたはパスワードが正しくありません"
  }
}
```

| コード | HTTPステータス | 説明 |
|---|---|---|
| `VALIDATION_ERROR` | 400 | リクエストバリデーション失敗 |
| `USER_ALREADY_EXISTS` | 409 | メールアドレス重複 |
| `INVALID_CREDENTIALS` | 401 | メールアドレスまたはパスワード不正 |
| `NOT_FOUND` | 404 | リソースが存在しない |
| `AI_SERVICE_UNAVAILABLE` | 503 | Affiliate/AI APIが利用不可 |
| `INTERNAL_SERVER_ERROR` | 500 | サーバー内部エラー |

## 認証・認可（SecurityConfig）

JWT stateless 認証を採用。セッションは使用しない。

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
```

- `/api/auth/**` はすべて認証不要（register / login）
- その他の `/api/**` はすべて有効な JWT が必要
- `JwtAuthFilter` が Bearer トークンを検証し、userId を `Authentication.principal` にセット

## 設計ルール

### 含めてよいもの

| 種別 | 例 |
|---|---|
| RESTコントローラー | `ArticleController`, `AuthController`, `UserController` |
| リクエスト/レスポンスDTO | `GenerateArticleRequest`, `AuthResponse` など |
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
