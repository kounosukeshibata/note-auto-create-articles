# Project Overview
React SPA (Frontend) と Kotlin Spring Boot 3.x (Backend) によるアプリケーション。
ドメイン駆動設計 (DDD) を採用し、保守性と拡張性を最優先する。

# Documentation Index
- Architecture: `docs/architecture.md`
- Product Overview: `docs/product-overview.md`
- Commands: `docs/commands.md`
- Design: `docs/design.md`
- Deployment: `docs/deployment.md`
- Prompt Management: `docs/prompt-management.md`
- Backend Standards: `docs/backend-development-standards.md`
- Frontend Standards: `docs/frontend-development-standards.md`
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

# 状況別エージェント選択ガイド
| 状況 | 推奨ツール | 理由 |
|---|---|---|
| 新機能の設計相談 | `design-architect` | ドキュメント参照してから実装方針を決める |
| 設計決定後の実装 | `tdd-implementation-runner` | TDDで実装 |
| 既存コードが壊れた | `bug-investigator` | 原因特定を先行させる |
| インフラ複数ステップ作業 | `gcloud-infra-ops` | デプロイ・権限設定・ログ調査など |
| 単発 gcloud コマンド | MCP `gcloud` 直接 | 自然言語で即実行できる |
| Vercel デプロイ | `/vercel-deployment` スキル | 手順がスキルに文書化されている |
| デプロイ後の動作確認 | `/e2e-verification` スキル | Playwright MCP でブラウザ自動操作する標準フローを参照 |

# Skills vs Agents 使い分け原則
- **Skills** (`/スキル名`): AIの振る舞いを強化する。呼び出し元のコンテキストで動く。知識・規約の注入に使う。
- **Agents** (自動起動): タスクを独立して委託する。別コンテキストで動く。長期・複雑なタスクに使う。

# MCP (.mcp.json)
- **gcloud** (`@google-cloud/gcloud-mcp`): Cloud Run, Secret Manager などを自然言語で操作。**gcloud 操作は Bash コマンド直接実行より MCP を優先すること。**
- **github** (`@modelcontextprotocol/server-github`): PR作成・CI確認・issue参照。Secret Manager 経由でラッパースクリプト（`.claude/mcp-scripts/github-mcp.sh`）が起動時に認証。
- **supabase** (`@supabase/mcp-server-supabase`): 本番DBスキーマ確認・クエリ実行。Secret Manager 経由でラッパースクリプト（`.claude/mcp-scripts/supabase-mcp.sh`）が起動時に認証。
- **Playwright**: ブラウザ自動操作・E2E 確認（グローバル設定）。`/e2e-verification` スキルで標準フローを参照。

# フック設計 (.claude/settings.json)
フックは「AIが作業したときに自動で品質チェックが走る」仕組み。現在のフック:

| イベント | トリガー | 動作 |
|---|---|---|
| PostToolUse | `.kt` ファイル編集 | `./gradlew compileKotlin` でコンパイルエラーを即検出 |
| PostToolUse | `*Test.kt` ファイル編集 | `./gradlew test` でテスト自動実行（TDDフィードバックループ） |
| PostToolUse | `.ts/.tsx` ファイル編集 | `npm run lint`（`tsc --noEmit`）で型エラーを即検出 |
| PostToolUse | `prompts/` 配下の編集 | プロンプト変更の品質影響を警告 |
| PreCompact | コンテキスト圧縮前（auto/manual） | 変更状態を `session-log.txt` に記録 |
| PostCompact | コンテキスト圧縮後（auto/manual） | 完了ログ記録 + エージェントメモリ更新リマインダー |
| SessionStart | セッション開始時 | 最新3コミットを表示してコンテキストを把握 |
| Stop | 作業完了時 | ドキュメント更新を促すリマインダー |

# Skills (.agents/skills/)
`/スキル名` で呼び出す。`documentation-writer` / `test-driven-development` / `clean-ddd-hexagonal` / `cloud-run-basics` / `google-cloud-recipe-auth` / `kotlin-springboot` / `kotlin-patterns` / `typescript-react-reviewer` / `requesting-code-review` / `vercel-deployment` / `prompt-engineering` / `e2e-verification`
