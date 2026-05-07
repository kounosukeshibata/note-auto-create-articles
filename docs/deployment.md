# デプロイ手順

## 構成概要

| レイヤー | 環境 | サービス |
|---|---|---|
| バックエンド | Cloud Run | `note-auto-post-backend`（asia-northeast1） |
| バックエンドイメージ | Artifact Registry | `asia-northeast1-docker.pkg.dev/project-384b1a9e-de04-4629-b84/note-auto-post/backend:latest` |
| フロントエンド | Vercel | プロジェクト: `nosukes-projects-92b05a53/frontend` |
| 環境変数 | Secret Manager | シークレット名: `note-auto-post-secret` |

---

## バックエンド (Cloud Run)

### 通常デプロイ（git push → Cloud Build 自動）

```bash
git push origin main
```

Cloud Build が `backend/cloudbuild.yaml` を検知してビルド・デプロイを自動実行する。  
ビルド状況は以下で確認:

```bash
gcloud builds list --limit 5
```

### 手動デプロイ（最新イメージを即時反映）

```bash
# 1. ビルド（Artifact Registry にプッシュ）
gcloud builds submit --config backend/cloudbuild.yaml

# 2. Cloud Run に反映
gcloud run services update note-auto-post-backend \
  --image asia-northeast1-docker.pkg.dev/project-384b1a9e-de04-4629-b84/note-auto-post/backend:latest \
  --region asia-northeast1
```

### デプロイ確認

```bash
gcloud run services describe note-auto-post-backend \
  --region asia-northeast1 \
  --format "json(status.latestReadyRevisionName)"
```

---

## フロントエンド (Vercel)

### 通常デプロイ（git push → Vercel 自動）

> **注意:** 現在 Vercel の GitHub 自動デプロイは未設定。  
> git push 後に以下の手動デプロイを実行する必要がある。

### 手動デプロイ

```bash
cd frontend
npx vercel --prod
```

初回のみ Vercel へのログインが必要:

```bash
npx vercel login
```

### デプロイ確認

Vercel ダッシュボード → プロジェクト `frontend` → Deployments タブ  
または以下でリビジョン確認:

```bash
cd frontend && npx vercel inspect <deployment-url>
```

---

## 環境変数 (Secret Manager)

### 現在のシークレット内容（`note-auto-post-secret`）

| キー | 説明 |
|---|---|
| `GEMINI_API_KEY` | Gemini API キー |
| `AMAZON_ACCESS_KEY` | Amazon PA-API アクセスキー |
| `AMAZON_SECRET_KEY` | Amazon PA-API シークレットキー |
| `AMAZON_ASSOCIATE_TAG` | Amazon アソシエイトタグ |
| `JWT_SECRET` | JWT署名用シークレット |
| `vertex.ai.stub` | `false` で RealVertexAiClient が有効（本番必須） |
| `amazon.stub` | `false` で RealAmazonAffiliateClient が有効（本番必須） |
| `storage.type` | `memory`（現在）。将来は `firestore` に切り替え |

### シークレットの確認・更新

```bash
# バージョン一覧
gcloud secrets versions list note-auto-post-secret

# 現在の内容を確認
gcloud secrets versions access latest --secret=note-auto-post-secret
```

シークレット更新はコンソールまたは以下で実施（ファイル経由）:

```bash
gcloud secrets versions add note-auto-post-secret --data-file=./secret.env
```

---

## 本番デプロイ前チェックリスト

- [ ] `vertex.ai.stub=false` が Secret Manager に設定されているか
- [ ] `amazon.stub=false` が Secret Manager に設定されているか
- [ ] `./gradlew test` が全てパスしているか
- [ ] フロントエンドのビルド（`npm run build`）がエラーなく完了するか
- [ ] API エンドポイント（`/api/articles/generate`）に対して動作確認を実施したか

---

## トラブルシューティング

### Cloud Build が失敗する場合

```bash
# 最新のビルドログを確認
gcloud builds log $(gcloud builds list --limit 1 --format "value(id)")
```

よくある原因:
- Artifact Registry への書き込み権限不足  
  → Cloud Build SA に `roles/artifactregistry.writer` を付与:
  ```bash
  gcloud artifacts repositories add-iam-policy-binding note-auto-post \
    --location=asia-northeast1 \
    --member="serviceAccount:$(gcloud projects describe $(gcloud config get-value project) --format='value(projectNumber)')@cloudbuild.gserviceaccount.com" \
    --role="roles/artifactregistry.writer"
  ```

### Vercel のデプロイが Deployments に表示されない

Vercel と GitHub リポジトリが連携されていない可能性がある。  
→ `cd frontend && npx vercel --prod` で手動デプロイを実行。

### バックエンドが旧実装を返す場合

最新リビジョンが Cloud Run に反映されていない可能性がある。  
→ `gcloud run services describe` でリビジョンを確認し、必要なら手動デプロイを実行。
