# ユビキタス言語辞書

本プロジェクトにおけるドメイン用語を定義する。コード・ドキュメント・会話すべてでこの用語を統一して使用すること。

## 認証・ユーザー関連

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| ユーザー | `User` | email/password で認証されるアカウント。名前・メールアドレス・ハッシュ化パスワードを保持する |
| ユーザーID | `UserId` | ユーザーを一意に識別するUUID形式の識別子 |
| JWTトークン | JWT Token | ログイン・登録時に発行される認証トークン。24時間有効。API呼び出し時に `Authorization: Bearer` ヘッダーで送信する |
| 新規登録 | Register | メールアドレス・パスワード・名前でアカウントを作成する操作 |
| ログイン | Login | メールアドレスとパスワードで認証し、JWT トークンを取得する操作 |

## 記事関連

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| 記事 | `Article` | 本プロジェクトの中心集約。本文・アイキャッチ画像・SEOキーワード・商品リンクをひとまとめにした生成記事。作成者の `UserId` を保持する |
| 記事ID | `ArticleId` | 記事を一意に識別するUUID形式の識別子 |
| 本文 | `Content` | Gemini 2.5 Flashが生成した記事のテキスト本文 |
| アイキャッチ画像 | `Image` | Imagen 3が生成した記事のサムネイル用画像 |
| 下書き | Draft | noteに一時保存された状態の記事。最終的な公開は手動で行う |

## SEO・キーワード関連

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| SEOキーワード | `SeoKeyword` | 検索エンジン最適化のためにGeminiが抽出したキーワード。記事のタイトル・見出し・本文に組み込まれる |
| キーワード抽出 | Keyword Extraction | 入力テーマからGeminiがSEO最適なキーワードを選定するプロセス |

## アフィリエイト関連

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| アフィリエイトリンク | `AffiliateLink` | Amazon のトラッキング用URL。クリック経由の購入で報酬が発生する（現在は AMAZON のみ実装。RAKUTEN は将来対応予定）|
| 商品情報 | `ProductInfo` | アフィリエイトAPIから取得した商品の名前・価格・カテゴリ・サムネイルURL等の情報 |
| 商品リンク群 | `ProductLinks` | 1つの記事に埋め込まれる `AffiliateLink` の集合 |
| リンク置換 | Link Replacement | 生成テキスト中のプレースホルダー（例: `{{product_link_0}}`）を実際のアフィリエイトリンクに差し替える処理 |
| アフィリエイトパイプライン | Affiliate Pipeline | キーワード抽出→商品検索→記事生成→リンク置換→投稿 の一連の自動化フロー |

## 外部サービス関連

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| note投稿 | `NotePost` | noteプラットフォームへの記事の一時保存（下書き）操作。APIを通じて実行する（現在は手動投稿。自動投稿は将来対応予定）|
| Amazon API | Amazon Affiliate API | Amazonの商品情報取得・アフィリエイトリンク生成のためのAPI（Amazon Product Advertising API） |
| Gemini API | Gemini API | Google の生成AI API（`generativelanguage.googleapis.com`）。Gemini 2.5 Flash モデルで記事・キーワードを生成する |
| データベース | InMemoryArticleRepository | 現在はインメモリ実装。将来的に Firestore/Supabase への切り替えを想定 |

## ドメインサービス

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| リンク置換サービス | `LinkReplacementService` | `Article` の本文テキストに対して、プレースホルダーをアフィリエイトリンクへ一括置換するドメインサービス |

## ユースケース

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| アフィリエイト記事生成 | `GenerateAffiliateArticleUseCase` | ユーザー入力を起点に、SEOキーワード抽出→商品検索→記事・画像生成→永続化までを一括実行するユースケース |
| ユーザー登録 | `RegisterUserUseCase` | メールアドレス・パスワード・名前を受け取り、BCrypt でハッシュ化してユーザーを保存し JWT を返すユースケース |
| ログイン | `LoginUserUseCase` | メールアドレスとパスワードを検証し、一致すれば JWT を返すユースケース |

## 状態・ライフサイクル

| 日本語 | 英語（コード名） | 定義 |
|---|---|---|
| 生成済み | Generated | AI生成が完了した初期状態 |
| 保存済み | Saved | データベースへの永続化が完了した状態（現在の `GenerateAffiliateArticleUseCase` の最終状態） |
| note一時保存済み | NoteDrafted | noteに下書きとして保存された状態（将来の note自動投稿機能で使用予定） |
