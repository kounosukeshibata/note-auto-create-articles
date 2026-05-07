---
name: プロジェクト全体コンテキスト
description: note自動投稿アプリのプロジェクト概要・技術スタック・現在の実装状態・MVPスコープ
type: project
---

AIとアフィリエイトを統合した収益化記事自動生成アプリ。ビジョン「収益をデザインする」。

## 技術スタック（現在の実装）
- **Frontend**: React(TypeScript) + TanStack Router + Vite → Vercel にデプロイ
- **Backend**: Kotlin + Spring Boot 3.x + DDD → Cloud Run にデプロイ
- **AI**: Gemini 2.5 Flash（Gemini API 直呼び出し、Vertex AI SDK は未使用）
- **DB**: InMemoryArticleRepository（開発）。将来 Firestore/Supabase 切替予定
- **認証**: Spring Security + JWT（email/password）
- **秘密情報**: Google Cloud Secret Manager（シークレット名: `note-auto-post-secret`）

## 現在の MVPスコープ（実装済み）
1. ログイン画面（email/password + JWT）
2. 記事生成フォーム（テーマ・記事タイプ・ターゲット悩み/理想・ストーリー・独自情報・CTA・文字数・ASP選択）
3. Gemini 2.5 Flash で記事生成（文字数: 1000/2000/3000/4000字選択可）
4. Amazon アフィリエイトリンクの自動挿入（還元率順ソート、上位5件）
5. DB 保存（InMemory）→ `status=SAVED` で返却
6. プレビュー画面

## 未実装（将来対応予定）
- note への自動投稿（`NoteClient` は定義済みだが UseCase には組み込まれていない）
- Rakuten アフィリエイト API 連携（ドメインに `AffiliatePlatform.RAKUTEN` は定義済み）
- Firestore/Supabase への永続化切り替え
- Vercel ↔ GitHub の自動デプロイ連携（現在は手動 `npx vercel --prod`）

**Why:** モバイルから数タップで高品質な収益化記事を生成・投稿するユースケースを最速で実現するため。

**How to apply:** 設計提案はMVPスコープを基準に行い、未実装機能は「将来の拡張ポイント」として分離して提案する。実装済みと未実装の混同に注意。
