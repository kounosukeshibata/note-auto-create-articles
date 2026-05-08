---
name: vercel-deployment
description: Use when deploying the frontend to Vercel, troubleshooting Vercel issues, or setting up Vercel GitHub integration
---

# Vercel デプロイ手順

## プロジェクト構成

- フロントエンドは `frontend/` ディレクトリに存在する（`rootDirectory: frontend` 設定済み）
- Vercel GitHub 自動デプロイ設定済み: `main` ブランチへの push で自動デプロイが走る
- Vercel プロジェクト設定は `.vercel/` ディレクトリ（gitignore 済み）
- GitHub リポジトリ: `kounosukeshibata/note-auto-create-articles`

## 初回セットアップ確認

`.vercel/project.json` が存在しない場合、以下でリンクを初期化する:

```bash
cd frontend && npx vercel link
```

対話形式で Vercel アカウント・プロジェクトを選択する。完了後 `.vercel/project.json` が生成される（gitignore 対象なので再クローン時は毎回必要）。

## 手動デプロイ（本番）

```bash
cd frontend && npx vercel --prod
```

## プレビューデプロイ（本番適用前確認）

```bash
cd frontend && npx vercel
```

## デプロイ確認

- Vercel ダッシュボード → 対象プロジェクト → **Deployments** タブ
- デプロイ URL は `npx vercel --prod` 実行後のターミナル出力に表示される

## GitHub 自動デプロイ（設定済み）

`main` ブランチへの push で自動的に本番デプロイが走る。

- GitHub リポジトリ: `kounosukeshibata/note-auto-create-articles`
- 対象ブランチ: `main`
- rootDirectory: `frontend`

デプロイ状況は Vercel ダッシュボード → **Deployments** タブ、または GitHub MCP で確認:
- 「直近のデプロイの状態を確認して」→ `mcp__github__get_pull_request_status` で取得可能

## トラブルシュート

| 症状 | 原因 | 対処 |
|---|---|---|
| `npx vercel --prod` でエラー | `.vercel/project.json` がない | `npx vercel link` で初期化 |
| デプロイは成功するが画面が古い | キャッシュ | ブラウザのハードリフレッシュ（Cmd+Shift+R） |
| API 呼び出しが失敗する | 環境変数 `VITE_API_BASE_URL` の設定漏れ | Vercel ダッシュボード → Settings → Environment Variables |
| ビルドが失敗する | TypeScript エラー | `cd frontend && npm run lint` でエラー確認 |

## 環境変数（Vercel 設定）

| 変数名 | 値 | 用途 |
|---|---|---|
| `VITE_API_BASE_URL` | Cloud Run の URL | バックエンド API エンドポイント |

Vercel ダッシュボード → プロジェクト → Settings → Environment Variables から設定する。
