# バックエンド開発標準

## 命名規則

| 種別 | 命名パターン | 例 |
|---|---|---|
| ユースケース（インターフェース） | 動詞 + 名詞 + UseCase | `GenerateAffiliateArticleUseCase` |
| ユースケース（実装） | インターフェース名 + Impl | `GenerateAffiliateArticleUseCaseImpl` |
| 入力 DTO | ユースケース名 + Input | `GenerateArticleInput` |
| 出力 DTO | ユースケース名 + Output | `GenerateArticleOutput` |
| 値オブジェクト | 名詞のみ（形容詞+名詞可） | `SeoKeyword`, `AffiliateLink`, `ProductInfo` |
| リポジトリ（インターフェース） | ドメイン名 + Repository | `ArticleRepository` |
| リポジトリ（実装） | 実装方式 + ドメイン名 + Repository | `JpaArticleRepository`, `InMemoryArticleRepository` |
| 外部クライアント（インターフェース） | 役割 + Client | `VertexAiClient`, `AffiliateApiClient` |
| 外部クライアント（実装） | Real + 役割 + Client | `RealVertexAiClient`, `RealAmazonAffiliateClient` |
| 外部クライアント（スタブ） | Stub + 役割 + Client | `StubVertexAiClient` |
| ドメイン例外 | 事象名 + Exception | `ArticleNotFoundException`, `AffiliateApiUnavailableException` |

## パッケージ配置ルール

```
com.example/
  domain/          ビジネスロジックのみ。Spring依存禁止
  application/     ユースケース、DTO、サービスインターフェース
  infrastructure/  リポジトリ実装、外部APIクライアント、設定クラス
  presentation/    RESTコントローラー、リクエスト/レスポンスDTO
```

### 禁止事項（ドメイン層）

- `@Repository`, `@Service`, `@Component` などの Spring アノテーション
- 外部ライブラリへの直接依存（Jackson, JPA, HTTP クライアント等）
- `@Transactional` によるトランザクション管理

## テストルール

| 対象 | テスト種別 | モック |
|---|---|---|
| 値オブジェクト / ドメインサービス | ユニットテスト | モックなし |
| ユースケース実装 | ユニットテスト | Mockito でインフラをモック |
| リポジトリ実装 | インテグレーションテスト（将来） | 実DBを使用 |
| REST コントローラー | Spring MVC テスト | MockMvc + MockBean |

```kotlin
// ユースケーステストの基本パターン
@ExtendWith(MockitoExtension::class)
class GenerateAffiliateArticleUseCaseImplTest {
    @Mock private lateinit var articleRepository: ArticleRepository
    @Mock private lateinit var vertexAiClient: VertexAiClient
    // ...

    @Test
    fun `記事が正常に生成され保存される`() {
        whenever(vertexAiClient.generateContent(anyOrNull())).thenReturn(mockContent)
        // ...
        val output = useCase.execute(input)
        assertThat(output.status).isEqualTo("SAVED")
    }
}
```

## インフラ切替パターン（Real / Stub）

本番とローカルでの切替は `@ConditionalOnProperty` で行う。環境変数はすべて Secret Manager 経由。

```kotlin
// 本番実装
@Component
@ConditionalOnProperty(name = ["vertex.ai.stub"], havingValue = "false", matchIfMissing = false)
class RealVertexAiClient(...) : VertexAiClient

// スタブ実装（ローカルデフォルト）
@Component
@ConditionalOnProperty(name = ["vertex.ai.stub"], havingValue = "true", matchIfMissing = true)
class StubVertexAiClient : VertexAiClient
```

| 環境変数 | ローカルデフォルト | 本番設定 |
|---|---|---|
| `VERTEX_AI_STUB` | `true`（スタブ） | `false`（Real） |
| `AMAZON_STUB` | `true`（スタブ） | `false`（Real） |
| `STORAGE_TYPE` | `memory`（InMemory） | `supabase`（JPA） |

## フォールバック方針

| 障害箇所 | 挙動 |
|---|---|
| Gemini API 障害 | フォールバックテキストで Content を構築（例外を外部に出さない） |
| Amazon Affiliate API 障害 | `AffiliateApiUnavailableException` を throw → 503 応答 |
| 画像生成障害 | `placehold.co` のプレースホルダー画像で継続 |

## セキュリティ

- API キー・シークレットは環境変数経由のみ（ハードコード厳禁）
- すべての秘密情報は Google Cloud Secret Manager (`note-auto-post-secret`) で管理
- `@Transactional` は Application 層以上にのみ付与する
