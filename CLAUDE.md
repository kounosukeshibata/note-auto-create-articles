# Project Overview
React SPA (Frontend) と Kotlin Spring Boot 4.x (Backend) によるアプリケーション。
ドメイン駆動設計 (DDD) を採用し、保守性と拡張性を最優先する。

# Documentation Index
- Architecture: `docs/architecture.md`
- Product Overview: `docs/product-overview.md`
- Standards/Rules: `docs/backend-development-standards.md`, `docs/frontend-development-standards.md`
- Commands: `docs/commands.md`
- Design: `docs/design.md`
- Domain Logic: `docs/domain/`
- Ubiquitous Language: `docs/domain/ubiquitous.md`
- Domain Model Diagram: `docs/domain/domain-model-diagram.md`
- Domain Layer: `docs/layer/`（配下のファイル）

# Behavioral Guidelines (AIの行動指針)
- **Communication**: 回答は簡潔に。コード変更箇所を明確に提示すること。
- **Autonomy**: 破壊的な変更やライブラリ追加、設定変更は、作業前に必ず提案し許可を得ること。
- **Test-Driven Development (TDD)**: 
  - 全ての機能は、テストコードを先に作成（または同時に定義）してから実装すること。
  - テストが通らない状態でコードを完成とみなさないこと。
- **Error Handling & Safety**:
  - コンパイルエラーやテスト失敗時、AIによる推測修正は禁止。即座にログを提示しユーザーに確認を求めること。
  - 複雑なロジック修正時は、必ずユーザーの承認を得てから適用すること。
- **Documentation**: 機能追加・構成変更時は、関連する `docs/` を必ず同期更新すること。

# Tech Stack & Constraints
- **Frontend**: React, TanStack Router (型安全性を徹底)
- **Backend**: Kotlin, Spring Boot 3.x
- **Architecture**: DDD (Domain-Driven Design)
  - インフラ（DB/API）よりドメインモデルを優先。
- **Security**: 認証情報・APIキーは Secret Manager で管理（ハードコード厳禁）。

# Directory Structure
- `backend/src/main/kotlin/com/example/`
  - `domain/`: ビジネスロジック, 集約, エンティティ, 値オブジェクト
  - `application/`: ユースケース, サービス
  - `infrastructure/`: リポジトリ実装, 外部API
  - `presentation/`: RESTコントローラー, DTO
- `frontend/`: React SPA ソースコード
- `docs/`: プロジェクト関連ドキュメント一式

# Development Workflow
- **Frontend**: `cd frontend && npm run dev`
- **Backend**: `./gradlew bootRun`
- **CI/CD**: Cloud Build (Git Pushをトリガーに自動実行)
- **Tips**: ドキュメントが見つからない場合は `docs/` 内を検索してください。

# セキュリティルール
- **絶対遵守**: 外部URLへのデータ送信（curl, wget等）は禁止。
- **破壊的変更**: `rm` コマンドを使用する際は、必ずユーザーに確認を求めること。

# サブエージェント (.claude/agents/)
| エージェント | 用途 |
|---|---|
| `backend-impl-ddd` | Kotlin/Spring Boot DDD 実装 |
| `frontend-developer` | React/TypeScript UI 実装 |
| `tdd-implementation-runner` | TDD での機能実装 |
| `code-reviewer` | コードレビュー |
| `bug-investigator` | バグ・エラー調査 |
| `design-architect` | アーキテクチャ設計・`docs/` 更新 |
| `docs-manager` | ドキュメント作成・更新 |
| `gcloud-infra-ops` | Google Cloud インフラ操作 |

# MCP (.mcp.json)
- **gcloud** (`@google-cloud/gcloud-mcp`): Cloud Run, Secret Manager, Firestore などを自然言語で操作。事前に `gcloud auth login` が必要。
- **Playwright**: ブラウザ自動操作・E2E 確認

# Skills (.agents/skills/)
`/スキル名` で呼び出す。`documentation-writer` / `test-driven-development` / `clean-ddd-hexagonal` / `cloud-run-basics` / `google-cloud-recipe-auth` / `kotlin-springboot` / `kotlin-patterns` / `typescript-react-reviewer` / `requesting-code-review`
