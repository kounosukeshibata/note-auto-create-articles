---
name: e2e-verification
description: Use when verifying the app end-to-end after deployment or feature implementation, using Playwright MCP to automate the browser
---

# E2E 検証スキル

デプロイ後や機能実装後に Playwright MCP でブラウザを自動操作し、ゴールデンパスを確認する。

## 前提確認

### ローカル検証の場合
```bash
# フロントエンド起動確認
cd frontend && npm run dev   # http://localhost:5173

# バックエンド起動確認
./gradlew bootRun            # http://localhost:8080
```

### 本番検証の場合
- フロントエンド: Vercel URL（デプロイ後のターミナル出力を確認）
- バックエンド: Cloud Run URL（`gcloud run services describe note-auto-post-backend`）

### note cookie の有効性確認
note への投稿を検証する場合のみ必要:
```bash
node refresh-note-cookie.mjs
```

---

## 標準 E2E フロー

### Step 1: ログイン確認

```
mcp__playwright__browser_navigate: { url: "http://localhost:5173/login" }
mcp__playwright__browser_snapshot: {}  # ログインフォームを確認
mcp__playwright__browser_fill_form: { fields: [{ selector: "input[type=password]", value: "<password>" }] }
mcp__playwright__browser_click: { selector: "button[type=submit]" }
mcp__playwright__browser_snapshot: {}  # /generate にリダイレクトされたか確認
```

ログイン成功の確認ポイント: URL が `/generate` に変わっていること。

---

### Step 2: 記事生成フォーム入力

```
mcp__playwright__browser_snapshot: {}  # フォームフィールドを確認
mcp__playwright__browser_fill_form: {
  fields: [
    { selector: "[name=theme]" または テーマ入力欄, value: "テスト用テーマ" },
    { selector: "wordCount選択", value: "2000" }
  ]
}
```

フォームの必須フィールド:
| フィールド | 必須 | 備考 |
|---|---|---|
| `theme`（テーマ） | ✅ | 100文字以内 |
| `affiliatePlatforms` | ✅ | デフォルト AMAZON が選択済み |
| `wordCount` | - | 1000/2000/3000/4000 |
| `articleType` | - | デフォルト「アフィリエイト」 |

バリデーションエラーが出る場合は `mcp__playwright__browser_snapshot` でエラーメッセージを確認。

---

### Step 3: 記事生成の実行と進捗確認

```
mcp__playwright__browser_click: { selector: "button[type=submit]" }
mcp__playwright__browser_snapshot: {}  # GenerationProgress が表示されるか確認
```

生成ステップ: `KEYWORDS → SEARCH → CONTENT → IMAGE → FINALIZE`（合計約29秒）

生成中はネットワークリクエストを監視して API 応答を確認できる:
```
mcp__playwright__browser_network_requests: {}
```

エラーが発生した場合: `ErrorModal` が表示される。`mcp__playwright__browser_snapshot` でエラー内容を取得し、コンソールも確認:
```
mcp__playwright__browser_evaluate: { script: "window.__lastError || ''" }
```

---

### Step 4: プレビュー確認

生成完了後、自動的に `/preview/<articleId>` に遷移する。

```
mcp__playwright__browser_snapshot: {}  # ArticlePreview コンポーネントを確認
```

確認ポイント:
- 記事タイトルが表示されているか
- 記事本文が空でないか
- 「note に投稿」ボタンが表示されているか（articleType が 一般/有料 の場合）
- アフィリエイトリンクが表示されているか（アフィリエイト記事の場合）

---

## チェックリスト（デプロイ後スモークテスト）

```
[ ] /login にアクセスできる
[ ] ログイン後 /generate にリダイレクトされる
[ ] フォームのバリデーションが動作する（テーマ空でエラーが出る）
[ ] 記事生成が開始され GenerationProgress が表示される
[ ] 生成完了後 /preview/$articleId に遷移する
[ ] プレビュー画面に記事内容が表示される
[ ] ブラウザコンソールにエラーがない
```

コンソールエラー確認:
```
mcp__playwright__browser_console_messages: {}
```

---

## よくあるトラブルと対処

| 症状 | 原因候補 | 対処 |
|---|---|---|
| `/generate` にアクセスすると `/login` に飛ばされる | セッション切れ | 再ログイン |
| 記事生成が途中で止まる | バックエンド API エラー | Cloud Run ログを確認 |
| プレビューが白い | `articleId` が取得できていない | ネットワークリクエストを確認 |
| note 投稿が失敗 | cookie の有効期限切れ | `node refresh-note-cookie.mjs` を実行 |
| フォームが送信できない | TypeScript 型エラーによるビルド失敗 | `npm run lint` でエラー確認 |
