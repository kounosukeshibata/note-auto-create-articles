# ドメインモデル図

## クラス図

```mermaid
classDiagram
    class Article {
        +ArticleId id
        +Content content
        +Image image
        +List~SeoKeyword~ keywords
        +ProductLinks productLinks
        +ArticleStatus status
        +injectLinks(LinkReplacementService) Article
        +markAsDrafted() Article
    }

    class ArticleId {
        +UUID value
        +generate() ArticleId$
    }

    class Content {
        +String text
        +String title
    }

    class Image {
        +String url
        +String altText
    }

    class SeoKeyword {
        +String value
    }

    class ProductLinks {
        +List~AffiliateLink~ links
        +add(AffiliateLink) ProductLinks
    }

    class AffiliateLink {
        +String url
        +String trackingId
        +AffiliatePlatform platform
        +ProductInfo productInfo
    }

    class ProductInfo {
        +String name
        +BigDecimal price
        +String category
        +String thumbnailUrl
    }

    class AffiliatePlatform {
        <<enumeration>>
        RAKUTEN
        AMAZON
    }

    class ArticleStatus {
        <<enumeration>>
        GENERATED
        SAVED
        NOTE_DRAFTED
    }

    class LinkReplacementService {
        +replace(Content, ProductLinks) Content
    }

    class ArticleRepository {
        <<interface>>
        +save(Article) Article
        +findById(ArticleId) Article?
        +findAll() List~Article~
        +delete(ArticleId)
    }

    Article *-- ArticleId : id
    Article *-- Content : content
    Article *-- Image : image
    Article *-- SeoKeyword : keywords
    Article *-- ProductLinks : productLinks
    Article -- ArticleStatus : status
    ProductLinks *-- AffiliateLink : links
    AffiliateLink *-- ProductInfo : productInfo
    AffiliateLink -- AffiliatePlatform : platform
    LinkReplacementService ..> Article : operates on
    ArticleRepository ..> Article : persists
```

## 集約境界

```mermaid
graph TD
    subgraph Article集約
        A[Article<br/>集約ルート]
        B[Content<br/>値オブジェクト]
        C[Image<br/>値オブジェクト]
        D[SeoKeyword<br/>値オブジェクト]
        E[ProductLinks<br/>値オブジェクト]
        F[AffiliateLink<br/>値オブジェクト]
        G[ProductInfo<br/>値オブジェクト]
        A --> B
        A --> C
        A --> D
        A --> E
        E --> F
        F --> G
    end

    subgraph ドメインサービス
        H[LinkReplacementService]
    end

    subgraph リポジトリI/F
        I[ArticleRepository]
    end

    H -->|Content変換| A
    I -->|永続化・取得| A
```

## アフィリエイトパイプラインのフロー

```mermaid
sequenceDiagram
    participant UC as GenerateAffiliateArticleUseCase
    participant GeminiClient as VertexAiClient (Gemini)
    participant AffAPI as AffiliateApiClient
    participant LRS as LinkReplacementService
    participant Repo as ArticleRepository
    participant Note as NoteClient

    UC->>GeminiClient: extractKeywords(input)
    GeminiClient-->>UC: List~SeoKeyword~

    UC->>AffAPI: searchProducts(keywords)
    AffAPI-->>UC: List~ProductInfo~

    UC->>GeminiClient: generateContent(input, keywords, products)
    GeminiClient-->>UC: Content (with placeholders)

    UC->>LRS: replace(content, productLinks)
    LRS-->>UC: Content (with affiliate links)

    UC->>Repo: save(article)
    Repo-->>UC: Article

    UC->>Note: postDraft(article)
    Note-->>UC: NotePostResult
```
