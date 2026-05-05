# システムアーキテクチャ

## 全体構成
CI/CDパイプラインを統合した疎結合なマイクロサービスアーキテクチャ。

## 技術スタック
- **Frontend:** React (TypeScript), TanStack Router, Vite
- **Backend:** Kotlin, Spring Boot 4.x (Latest)
- **Database:** Google Cloud Firestore
- **Auth:** Spring Security + Google OAuth2
- **AI Services:** Vertex AI (Gemini 1.5 / Imagen 3)
- **External Integration:** Affiliate APIs (Rakuten, Amazon)

## レイヤー定義
### 1. Domain Layer
- **Aggregate:** `Article` (Content, Image, Keywords, ProductLinks)
- **Value Object:** `ProductInfo`, `AffiliateLink`, `SeoKeyword`
- **Domain Service:** `LinkReplacementService` (テキスト内のリンク置換ロジック)

### 2. Application Layer
- **UseCase:** `GenerateAffiliateArticleUseCase`
- **Flow:** 
  1. SEOキーワード抽出 
  2. 商品検索API実行 
  3. テキスト/画像生成リクエスト 
  4. 永続化 (Firestore)

### 3. Infrastructure Layer
- **Persistence:** `FirestoreArticleRepository`
- **Adapters:** 
  - `VertexAiClient` (Gemini 1.5, Imagen 3)
  - `RakutenAffiliateClient`
  - `AmazonAffiliateClient`
  - `NoteClient` (一時保存)

### 4. Presentation　Layer
- **Web:** `ArticleController` (JSON API)
- **Auth:** `SecurityConfig` (Spring Security + Google OAuth2)

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
1. User: Frontendでテーマと画像コンセプトを入力。
2. Frontend -> Backend (REST API): 生成リクエスト送信。
3. Backend: Geminiで記事本文生成 ＋ Imagenで画像生成。
4. Backend -> Frontend: 生成データ（テキスト＋画像URL）を返却。
5. User: プレビュー画面で確認し、投稿。