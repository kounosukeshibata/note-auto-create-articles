# システムアーキテクチャ

## 全体構成
CI/CDパイプラインを統合した疎結合なマイクロサービスアーキテクチャ。

## 技術スタック
- **Frontend:** React (TypeScript), TanStack Router, Vite
- **Backend:** Kotlin, Spring Boot 3.x
- **Database:** Supabase (PostgreSQL) — 開発環境はインメモリ
- **Auth:** Spring Security + JWT (email/password 認証)
- **AI Services:** Google AI Gemini API (Gemini 2.5 Flash) / Imagen 3
- **External Integration:** Affiliate APIs (Amazon)

## レイヤー定義
### 1. Domain Layer
- **Aggregate:** `Article` (Content, Image, Keywords, ProductLinks, userId)
- **Aggregate:** `User` (email, passwordHash, name)
- **Value Object:** `ProductInfo`, `AffiliateLink`, `SeoKeyword`, `UserId`, `ArticleId`
- **Domain Service:** `LinkReplacementService` (テキスト内のリンク置換ロジック)
- **Repository Interface:** `ArticleRepository`, `UserRepository`

### 2. Application Layer
- **UseCase:** `GenerateAffiliateArticleUseCase`, `RegisterUserUseCase`, `LoginUserUseCase`
- **Port:** `TokenProvider` (JWT 生成・検証インターフェース)
- **Flow:** 
  1. SEOキーワード抽出 
  2. 商品検索API実行 
  3. テキスト/画像生成リクエスト 
  4. 永続化 (Supabase)

### 3. Infrastructure Layer
- **Persistence:** `InMemoryArticleRepository` (開発/テスト), Supabase JPA 実装 (本番)
- **Persistence:** `InMemoryUserRepository` (開発/テスト), Supabase JPA 実装 (本番)
- **JWT:** `JwtTokenProvider` (JJWT 0.12.x)
- **Filter:** `JwtAuthFilter` (認証フィルター)
- **Adapters:** 
  - `VertexAiClient` (Gemini 2.5 Flash, Imagen 3) — プロンプトはMDファイルで管理 (`prompts/`)
  - `RealAmazonAffiliateClient` (Gemini APIで商品提案 → Amazon検索URL生成)
  - `NoteClient` (一時保存)

### 4. Presentation Layer
- **Web:** `ArticleController`, `AuthController`, `UserController` (JSON API)
- **Auth:** `SecurityConfig` (JWT stateless)

## 認証フロー
1. ユーザーが `/api/auth/register` または `/api/auth/login` でトークン取得
2. 以降の全APIリクエストで `Authorization: Bearer {token}` ヘッダーを送信
3. `JwtAuthFilter` がトークンを検証し、SecurityContext に userId をセット

## 処理フロー (Affiliate Pipeline)
1. **Keyword Extraction:** 入力項目から Gemini がSEOキーワードを抽出。
2. **Product Search:** 抽出キーワードをもとにアフィリエイトAPIから商品検索。
3. **Article/Image Gen:** 商品情報を埋め込んで記事・画像を生成。
4. **Link Injection:** 生成テキスト内のプレースホルダーを追跡用アフィリエイトリンクへ置換。
5. **Publish:** note への下書き投稿。

## ワークフロー (CI/CD)
1. Developer: `git push`
2. **Cloud Build:** - Dockerイメージのビルド
   - コンテナイメージを Artifact Registry へプッシュ
   - Cloud Run サービスをデプロイ
3. **Runtime:** ユーザーが入力 -> Backend API -> Vertex AI -> 生成結果返却

## データフロー
1. User: ログイン後、Frontendでテーマ・記事タイプ・ターゲット情報等を入力。
2. Frontend -> Backend (REST API): JWT付き生成リクエスト送信。
3. Backend: Geminiで記事本文生成 ＋ Imagenで画像生成。
4. Backend -> Frontend: 生成データ（テキスト＋画像URL）を返却。
5. User: プレビュー画面で確認し、投稿。
