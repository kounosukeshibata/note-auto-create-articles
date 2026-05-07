# Commands

## Backend (Kotlin/Gradle)

```bash
./gradlew bootRun
./gradlew test
./gradlew test --tests "com.example.ClassName"  # 単一テスト
```

## Frontend (React)

```bash
cd frontend && npm install
npm run dev
npm run lint
npm run build
```

## Docker

```bash
docker build -t note-auto-post .
docker run -p 8080:8080 note-auto-post
```

## Google Cloud

```bash
# 認証
gcloud auth login

# バックエンドビルド（Cloud Build経由）
gcloud builds submit --config backend/cloudbuild.yaml

# バックエンドデプロイ（最新イメージを Cloud Run に反映）
gcloud run services update note-auto-post-backend \
  --image asia-northeast1-docker.pkg.dev/project-384b1a9e-de04-4629-b84/note-auto-post/backend:latest \
  --region asia-northeast1

# フロントエンドデプロイ（Vercel）
cd frontend && npx vercel --prod
```

> 詳細な手順・チェックリストは `docs/deployment.md` を参照。
