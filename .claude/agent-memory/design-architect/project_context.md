---
name: プロジェクト全体コンテキスト
description: note自動投稿アプリのプロジェクト概要・技術スタック・MVPスコープの要約
type: project
---

AIとアフィリエイトを統合した収益化記事自動生成アプリ。ビジョン「収益をデザインする」。

技術スタック: React(TypeScript)+TanStack Router+Vite / Kotlin+Spring Boot 3.x+DDD / Firestore / Vertex AI (Gemini 1.5 Pro / Imagen 3) / Cloud Run

MVPスコープ: ログイン→記事生成フォーム→AI生成（記事・画像・アフィリエイトリンク）→モバイルプレビュー→DB保存+note下書き一時保存

**Why:** モバイルから数タップで高品質な収益化記事を生成・投稿するユースケースを最速で実現するため。

**How to apply:** 設計提案はMVPスコープを基準に行い、拡張ポイントは「今後の拡張ポイント」として分離して提案する。
