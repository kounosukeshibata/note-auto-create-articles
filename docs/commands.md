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
gcloud auth login
gcloud config set project PROJECT_ID
gcloud run deploy --source .
```
