# Claude Code 活用セットアップガイド

> このドキュメントは `note-auto-post` プロジェクトで確立した Claude Code 活用パターンを、  
> 次のプロジェクトの初日からそのまま再現するための実装手順書です。  
> 抽象的な原則ではなく、**コマンドとファイル内容を含む具体的な手順**を記します。

---

## 達成できる状態（このガイドのゴール）

```
スコア 98/100 相当の AI-Native 開発環境
  ✅ CLAUDE.md       — AIの行動指針・ツールインベントリを完備
  ✅ フック(9本)     — コード編集→自動コンパイル/テスト/型チェックが即実行
  ✅ MCP(4種)        — gcloud / GitHub / Supabase / Playwright を自然言語で操作
  ✅ サブエージェント — 役割別エージェントがセッション間メモリを保持
  ✅ スキル(12本)    — 専門知識をワンコマンドで注入
  ✅ セキュリティ    — Secret Manager + allow/deny で最小権限
  ✅ CI/CD          — GitHub push → Vercel 自動デプロイ
```

---

## Phase 0: リポジトリ作成直後（所要時間: 30分）

### 0-1. ディレクトリ構成を作る

```bash
mkdir -p .claude/mcp-scripts
mkdir -p .claude/agent-memory
mkdir -p .agents/agents
mkdir -p .agents/skills
touch .claude/session-log.txt
```

### 0-2. .gitignore に追加する項目

```gitignore
# Claude Code - ローカル設定（認証情報が入る可能性あり）
.claude/settings.local.json

# Vercel（プロジェクトリンク情報）
.vercel/

# 環境変数
.env
.env.local

# ※ .claude/agent-memory/ は gitignore しないこと！
#   セッション間コンテキストを保持する重要ファイル。
#   PC移行時にも失わないようにリポジトリで管理する。
```

### 0-3. jq を事前にインストールする

フックで JSON を解析するために必須。プロジェクト開始前に一度だけ実行：

```bash
brew install jq   # macOS
# または
sudo apt install jq  # Ubuntu/Debian
```

---

## Phase 1: CLAUDE.md を作る（所要時間: 20分）

CLAUDE.md は「AIへの仕様書」。**これが最も重要なファイル**。ここに書いたことがAIの基本動作を決める。

### CLAUDE.md テンプレート

```markdown
# Project Overview
（プロジェクトの一言説明。技術スタックを明記する）
例: React SPA (Frontend) と Kotlin Spring Boot 3.x (Backend) によるアプリケーション。DDD採用。

# Documentation Index
- Architecture: `docs/architecture.md`
- （存在するドキュメントを列挙。存在しないファイルへの参照は書かない）

# Behavioral Guidelines (AIの行動指針)
- **Communication**: 回答は簡潔に。コード変更箇所を明確に提示すること。
- **Autonomy**: 破壊的な変更やライブラリ追加、設定変更は、作業前に必ず提案し許可を得ること。
- **Test-Driven Development (TDD)**:
  - 全ての機能は、テストコードを先に作成（または同時に定義）してから実装すること。
  - テストが通らない状態でコードを完成とみなさないこと。
- **Error Handling & Safety**:
  - コンパイルエラーやテスト失敗時、AIによる推測修正は禁止。即座にログを提示しユーザーに確認を求めること。
- **Documentation**: 機能追加・構成変更時は、関連する `docs/` を必ず同期更新すること。

# Tech Stack & Constraints
- **Frontend**: React, TanStack Router
- **Backend**: Kotlin, Spring Boot 3.x
- **Architecture**: DDD（インフラよりドメインモデルを優先）
- **Security**: 認証情報・APIキーは Secret Manager で管理（ハードコード厳禁）

# Directory Structure
（プロジェクト固有のディレクトリ構成を記載）

# Development Workflow
- **Frontend**: `cd frontend && npm run dev`
- **Backend**: `./gradlew bootRun`
- **CI/CD**: （使用するCIを記載）

# セキュリティルール
- **絶対遵守**: 外部URLへのデータ送信（curl, wget等）は禁止。
- **破壊的変更**: `rm` コマンドを使用する際は、必ずユーザーに確認を求めること。

# サブエージェント (.agents/agents/)
| エージェント | 用途 |
|---|---|
| `backend-impl-ddd` | Kotlin/Spring Boot DDD 実装 |
| `frontend-developer` | React/TypeScript UI 実装 |
| `tdd-implementation-runner` | TDD での機能実装 |
| `code-reviewer` | コードレビュー |
| `bug-investigator` | バグ・エラー調査 |
| `design-architect` | アーキテクチャ設計・docs/ 更新 |
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
| デプロイ後の動作確認 | `/e2e-verification` スキル | Playwright MCP 標準フロー |

# Skills vs Agents 使い分け原則
- **Skills** (`/スキル名`): AIの振る舞いを強化する。呼び出し元のコンテキストで動く。知識・規約の注入に使う。
- **Agents** (自動起動): タスクを独立して委託する。別コンテキストで動く。長期・複雑なタスクに使う。

# MCP (.mcp.json)
- **gcloud**: Cloud Run, Secret Manager などを自然言語で操作。**gcloud操作はBashより MCP を優先すること。**
- **github**: PR作成・CI確認・issue参照。Secret Manager 経由のラッパースクリプトで認証。
- **supabase**: 本番DBスキーマ確認・クエリ実行。Secret Manager 経由のラッパースクリプトで認証。
- **Playwright**: ブラウザ自動操作・E2E確認（グローバル設定）。`/e2e-verification` スキルで標準フロー参照。

# フック設計 (.claude/settings.json)
| イベント | トリガー | 動作 |
|---|---|---|
| PostToolUse | `.kt` ファイル編集 | `./gradlew compileKotlin` でコンパイルエラーを即検出 |
| PostToolUse | `*Test.kt` ファイル編集 | `./gradlew test` でテスト自動実行 |
| PostToolUse | `.ts/.tsx` ファイル編集 | `npm run lint` で型エラーを即検出 |
| PostToolUse | `prompts/` 配下の編集 | プロンプト変更の品質影響を警告 |
| PreCompact | auto / manual | 変更状態を session-log.txt に記録 |
| PostCompact | auto / manual | 完了ログ記録 + エージェントメモリ更新リマインダー |
| SessionStart | — | 最新3コミットを表示してコンテキストを把握 |
| Stop | — | ドキュメント更新を促すリマインダー |

# Skills (.agents/skills/)
`/スキル名` で呼び出す。（作成したスキルを列挙する）
```

---

## Phase 2: フック・権限設定（所要時間: 15分）

### 設定ファイルの2層構造を理解する

Claude Code の設定ファイルは**2層に分かれており、マージされて動作する**：

| ファイル | git | 誰のPC | 用途 |
|---|---|---|---|
| `.claude/settings.json` | ✅ コミット | チーム全員 | フック・権限などチーム共通設定 |
| `.claude/settings.local.json` | ❌ gitignore | 個人のみ | 個人の上書き設定（追加許可など） |

**原則: フックと権限は `.claude/settings.json` に書いてチームで共有する。**  
`.claude/settings.local.json` は個人がローカルで追加したい許可だけを書く。

### チーム共通設定の落とし穴: 絶対パス問題

フックに絶対パスを書くと他のメンバーのPCで動かない：

```bash
# ❌ 特定のPCでしか動かない
"cd /Users/shibatakonosuke/git/note/note-auto-post/backend && ./gradlew compileKotlin"

# ✅ git のルートを動的取得するので全員のPCで動く
"ROOT=$(git rev-parse --show-toplevel); cd \"$ROOT/backend\" && ./gradlew compileKotlin"
```

### `.claude/settings.json` テンプレート（チーム全員で共有）

```json
{
  "permissions": {
    "allow": [
      "Write",
      "Bash(./gradlew test *)",
      "Bash(./gradlew compileKotlin *)",
      "Bash(./gradlew compileKotlin)",
      "Bash(./gradlew build *)",
      "Bash(npm run *)",
      "Bash(npx vercel *)",
      "Bash(git *)",
      "Bash(jq *)",
      "Bash(node *)",
      "Bash(gcloud secrets versions access *)",
      "Bash(gcloud secrets versions list *)",
      "Bash(gcloud secrets describe *)",
      "Bash(gcloud secrets list *)",
      "Bash(gcloud builds *)",
      "Bash(gcloud run services describe *)",
      "Bash(gcloud run services update *)",
      "Bash(gcloud logging read *)",
      "Bash(gcloud config get-value project *)",
      "Bash(gcloud artifacts *)",
      "Bash(gcloud auth print-access-token)",
      "Bash(gcloud auth application-default print-access-token)",
      "mcp__gcloud__run_gcloud_command",
      "mcp__playwright__browser_navigate",
      "mcp__playwright__browser_snapshot",
      "mcp__playwright__browser_click",
      "mcp__playwright__browser_fill_form",
      "mcp__playwright__browser_type",
      "mcp__playwright__browser_take_screenshot",
      "mcp__playwright__browser_evaluate",
      "mcp__playwright__browser_network_requests",
      "mcp__playwright__browser_press_key",
      "mcp__playwright__browser_handle_dialog",
      "mcp__playwright__browser_file_upload",
      "Bash(chmod +x *)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(curl *)",
      "Bash(git push *)",
      "Read(**/.env)",
      "Read(**/*.pem)"
    ],
    "ask": [
      "Bash(docker push *)"
    ]
  },
  "enableAllProjectMcpServers": true,
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": "ROOT=$(git rev-parse --show-toplevel); jq -r '.tool_input.file_path // .tool_response.filePath // \"\"' | grep -E '\\.kt$' | { read -r f; [ -n \"$f\" ] && cd \"$ROOT/backend\" && ./gradlew compileKotlin -q 2>&1 | tail -5; } || true",
            "timeout": 90,
            "statusMessage": "Kotlin コンパイル確認中..."
          },
          {
            "type": "command",
            "command": "ROOT=$(git rev-parse --show-toplevel); jq -r '.tool_input.file_path // .tool_response.filePath // \"\"' | grep -E 'Test\\.kt$' | { read -r f; [ -n \"$f\" ] && cd \"$ROOT/backend\" && ./gradlew test -q 2>&1 | tail -20; } || true",
            "timeout": 180,
            "statusMessage": "テスト自動実行中..."
          },
          {
            "type": "command",
            "command": "ROOT=$(git rev-parse --show-toplevel); jq -r '.tool_input.file_path // .tool_response.filePath // \"\"' | grep -E '\\.(tsx?|ts)$' | { read -r f; [ -n \"$f\" ] && cd \"$ROOT/frontend\" && npm run lint 2>&1 | tail -5; } || true",
            "timeout": 60,
            "statusMessage": "TypeScript 型チェック中..."
          }
        ]
      }
    ],
    "PreCompact": [
      {
        "matcher": "auto",
        "hooks": [
          {
            "type": "command",
            "command": "ROOT=$(git rev-parse --show-toplevel); echo \"$(date '+%Y-%m-%d %H:%M:%S'): コンパクション発生\" >> \"$ROOT/.claude/session-log.txt\" && git -C \"$ROOT\" diff --stat >> \"$ROOT/.claude/session-log.txt\" 2>/dev/null || true",
            "timeout": 10
          }
        ]
      },
      {
        "matcher": "manual",
        "hooks": [
          {
            "type": "command",
            "command": "ROOT=$(git rev-parse --show-toplevel); echo \"$(date '+%Y-%m-%d %H:%M:%S'): コンパクション発生（手動）\" >> \"$ROOT/.claude/session-log.txt\" && git -C \"$ROOT\" diff --stat >> \"$ROOT/.claude/session-log.txt\" 2>/dev/null || true",
            "timeout": 10
          }
        ]
      }
    ],
    "PostCompact": [
      {
        "matcher": "auto",
        "hooks": [
          {
            "type": "command",
            "command": "ROOT=$(git rev-parse --show-toplevel); cat > /dev/null; echo \"$(date '+%Y-%m-%d %H:%M:%S'): コンパクション完了\" >> \"$ROOT/.claude/session-log.txt\" && echo \"---\" >> \"$ROOT/.claude/session-log.txt\" && jq -n '{\"systemMessage\": \"📝 コンパクション完了。重要な設計決定はエージェントメモリに保存しましたか？\"}' 2>/dev/null || true",
            "timeout": 10
          }
        ]
      },
      {
        "matcher": "manual",
        "hooks": [
          {
            "type": "command",
            "command": "ROOT=$(git rev-parse --show-toplevel); cat > /dev/null; echo \"$(date '+%Y-%m-%d %H:%M:%S'): コンパクション完了（手動）\" >> \"$ROOT/.claude/session-log.txt\" && echo \"---\" >> \"$ROOT/.claude/session-log.txt\" && jq -n '{\"systemMessage\": \"📝 コンパクション完了。重要な設計決定はエージェントメモリに保存しましたか？\"}' 2>/dev/null || true",
            "timeout": 10
          }
        ]
      }
    ],
    "SessionStart": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "git log --oneline -3 | jq -Rn '{\"systemMessage\": \"最新3コミット: \" + ([inputs] | join(\" | \"))}'",
            "timeout": 10
          }
        ]
      }
    ],
    "Stop": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "jq -n '{\"systemMessage\": \"実装完了。docs/ の関連ドキュメントを更新しましたか？\"}'",
            "timeout": 5
          }
        ]
      }
    ]
  }
}
```

**カスタマイズポイント:**
- 使わない言語のフックは削除（Kotlin のみなら TypeScript フックは不要）
- `frontend` / `backend` のサブディレクトリ名はプロジェクトに合わせて変更
- `prompts/` を使う場合は以下を PostToolUse に追加:

```json
{
  "type": "command",
  "command": "jq -r '.tool_input.file_path // .tool_response.filePath // \"\"' | grep 'prompts/' | { read -r f; [ -n \"$f\" ] && jq -n '{\"systemMessage\": \"⚠️ プロンプトファイルを変更しました。品質への影響を確認してください。\"}'; } || true",
  "timeout": 5
}
```

### `.claude/settings.local.json` の使いどころ（個人設定）

チームの `settings.json` には含めたくないが、自分の作業では必要な許可を追加する：

```json
{
  "permissions": {
    "allow": [
      "Bash(brew *)",
      "Read(/Users/自分の名前/**)"
    ]
  }
}
```

`settings.json` の allow リストと **マージされる**ので、チーム設定は引き継がれる。

---

## Phase 3: MCP 設定（所要時間: 30分）

### 3-1. .mcp.json を作成する

```json
{
  "mcpServers": {
    "gcloud": {
      "command": "npx",
      "args": ["-y", "@google-cloud/gcloud-mcp"]
    },
    "github": {
      "command": "bash",
      "args": ["-c", "exec \"$(git rev-parse --show-toplevel)/.claude/mcp-scripts/github-mcp.sh\""]
    },
    "supabase": {
      "command": "bash",
      "args": ["-c", "exec \"$(git rev-parse --show-toplevel)/.claude/mcp-scripts/supabase-mcp.sh\""]
    }
  }
}
```

> **ポイント**: `command` に絶対パスを書くと他のメンバーのPCで動かない。`bash -c` + `git rev-parse --show-toplevel` でリポジトリルートを動的解決することで、誰がどのパスにクローンしても動く。

### 3-2. Secret Manager に資格情報を登録する

```bash
# GitHub PAT (repo + pull_requests スコープ)
# ターミナルで直接入力（履歴に残らないよう read -s を使う）
read -s GITHUB_TOKEN
gcloud secrets create GITHUB_TOKEN --project=YOUR_PROJECT_ID
echo -n "$GITHUB_TOKEN" | gcloud secrets versions add GITHUB_TOKEN --data-file=- --project=YOUR_PROJECT_ID

# Supabase
read -s SUPABASE_URL
gcloud secrets create SUPABASE_URL --project=YOUR_PROJECT_ID
echo -n "$SUPABASE_URL" | gcloud secrets versions add SUPABASE_URL --data-file=- --project=YOUR_PROJECT_ID

read -s SUPABASE_SERVICE_ROLE_KEY
gcloud secrets create SUPABASE_SERVICE_ROLE_KEY --project=YOUR_PROJECT_ID
echo -n "$SUPABASE_SERVICE_ROLE_KEY" | gcloud secrets versions add SUPABASE_SERVICE_ROLE_KEY --data-file=- --project=YOUR_PROJECT_ID
```

> **なぜ Secret Manager か?** `.mcp.json` は git 管理対象のためハードコードすると資格情報がリポジトリに入る。ラッパースクリプトが起動時に Secret Manager から取得することで、git に一切残らない。

### 3-3. ラッパースクリプトを作成する

```bash
# .claude/mcp-scripts/github-mcp.sh
cat > .claude/mcp-scripts/github-mcp.sh << 'EOF'
#!/bin/bash
set -e
export GITHUB_PERSONAL_ACCESS_TOKEN=$(gcloud secrets versions access latest \
  --secret=GITHUB_TOKEN \
  --project=YOUR_PROJECT_ID)
exec npx -y @modelcontextprotocol/server-github
EOF
chmod +x .claude/mcp-scripts/github-mcp.sh

# .claude/mcp-scripts/supabase-mcp.sh
cat > .claude/mcp-scripts/supabase-mcp.sh << 'EOF'
#!/bin/bash
set -e
SUPABASE_URL=$(gcloud secrets versions access latest \
  --secret=SUPABASE_URL \
  --project=YOUR_PROJECT_ID)
SUPABASE_KEY=$(gcloud secrets versions access latest \
  --secret=SUPABASE_SERVICE_ROLE_KEY \
  --project=YOUR_PROJECT_ID)
exec npx -y @supabase/mcp-server-supabase \
  --supabase-url "$SUPABASE_URL" \
  --supabase-key "$SUPABASE_KEY"
EOF
chmod +x .claude/mcp-scripts/supabase-mcp.sh
```

### 3-4. gcloud MCP の認証確認

gcloud MCP は **Application Default Credentials (ADC)** を使うためラッパー不要。以下が通れば即使える：

```bash
gcloud auth application-default login
# → ブラウザが開いてログイン → ~/.config/gcloud/ にトークン保存
```

### 3-5. Playwright MCP はグローバル設定

`~/.claude/settings.json`（ユーザーレベル）に追加することでどのプロジェクトでも使える：

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp@latest"]
    }
  }
}
```

---

## Phase 4: サブエージェント設計（所要時間: 60分）

### エージェントの役割分担（推奨構成）

| エージェント | ファイル | 主な責務 |
|---|---|---|
| `backend-impl-ddd` | `.agents/agents/backend-impl-ddd.md` | バックエンド実装（DDD準拠） |
| `frontend-developer` | `.agents/agents/frontend-developer.md` | フロントエンド実装 |
| `tdd-implementation-runner` | `.agents/agents/tdd-implementation-runner.md` | TDD で機能実装 |
| `code-reviewer` | `.agents/agents/code-reviewer.md` | コードレビュー |
| `bug-investigator` | `.agents/agents/bug-investigator.md` | バグ・エラー調査 |
| `design-architect` | `.agents/agents/design-architect.md` | 設計・docs/ 更新 |
| `docs-manager` | `.agents/agents/docs-manager.md` | ドキュメント管理 |
| `gcloud-infra-ops` | `.agents/agents/gcloud-infra-ops.md` | GCP インフラ操作 |

### エージェント定義ファイルの書き方

```markdown
---
name: backend-impl-ddd
description: Use this agent when you need to implement backend features using Kotlin, Spring Boot 3.x, and DDD.

<example>
Context: The user wants to add a new domain feature to the backend.
user: "記事の下書き保存機能をバックエンドに実装してほしい"
assistant: "バックエンド実装エージェントを起動して、DDDに則った実装を行います。"
<commentary>
ユーザーが新機能の実装を依頼しているため、backend-impl-ddd エージェントを使用する。
</commentary>
</example>
---

# Backend 実装エージェント（DDD準拠）

## 役割
Kotlin + Spring Boot 3.x のバックエンド実装を DDD アーキテクチャに従って行う。

## 参照ドキュメント
- `docs/architecture.md`
- `docs/backend-development-standards.md`
- `docs/domain/domain-model-diagram.md`

## 実装ルール
（プロジェクト固有のルールを記載）
```

**重要**: `description` フィールドに `<example>` タグを含めること。これによりメインの Claude がいつこのエージェントを使うべきかを自動判断できる。

### エージェントメモリの初期設定

各エージェントが記憶を持てるように、メモリディレクトリとインデックスを作る：

```bash
mkdir -p .claude/agent-memory/backend-impl-ddd
mkdir -p .claude/agent-memory/design-architect
# 他のエージェントも同様

# MEMORY.md（各エージェントのインデックス）を初期化
echo "# Memory Index" > .claude/agent-memory/backend-impl-ddd/MEMORY.md
echo "# Memory Index" > .claude/agent-memory/design-architect/MEMORY.md
```

---

## Phase 5: スキル設計（所要時間: 30分）

### スキルの構造

```
.agents/skills/
  vercel-deployment/SKILL.md    # Vercel デプロイ手順
  e2e-verification/SKILL.md     # E2E 検証フロー
  prompt-engineering/SKILL.md   # プロンプト変更ルール
```

### スキルファイルの Frontmatter

```markdown
---
name: vercel-deployment
description: Use when deploying the frontend to Vercel or troubleshooting Vercel issues
---

# Vercel デプロイ手順
（具体的な手順を記載）
```

### 優先的に作るべきスキル（転用可能なもの）

| スキル | いつ使う |
|---|---|
| `vercel-deployment` | フロントエンドデプロイ時 |
| `e2e-verification` | Playwright で動作確認する時 |
| `prompt-engineering` | LLM プロンプトを変更する時 |
| `test-driven-development` | TDD の哲学・手順を注入したい時 |
| `clean-ddd-hexagonal` | DDDレイヤー配置の判断が必要な時 |
| `cloud-run-basics` | Cloud Run デプロイ・設定時 |

---

## Phase 6: Vercel GitHub 自動デプロイ設定（所要時間: 10分）

### 手順

1. **GitHub App のアクセス権付与**
   - `https://github.com/settings/installations` → Vercel → Configure
   - 対象リポジトリを "Repository access" に追加 → Save

2. **Vercel ダッシュボードで連携**
   - プロジェクト → Settings → Git → **Connect Git Repository**
   - GitHub → リポジトリを選択 → Connect

3. **API で rootDirectory を設定**（モノレポの場合）
   ```javascript
   // frontend/ サブディレクトリが Vercel のルートの場合
   // node -e で実行（Vercel CLI トークンを使用）
   const body = JSON.stringify({ rootDirectory: 'frontend' });
   // PATCH https://api.vercel.com/v9/projects/{projectId}?teamId={teamId}
   ```

---

## フックの設計原則

### なぜフックが重要か

フックなしの開発サイクル:
```
コードを書く → 手動でコンパイル依頼 → エラーを確認 → 修正 → また依頼
```

フックありの開発サイクル:
```
コードを書く → 即座にコンパイル/テスト/型チェックが自動実行 → AIがその場で修正
```

TDD のフィードバックループが数分から数秒に短縮される。

### jq を使う理由

フックは stdin から JSON を受け取る。JSON を解析する方法は2つ：

```bash
# ❌ python3（長い・依存が重い）
python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('file_path') or '')"

# ✅ jq（短い・読みやすい・保守しやすい）
jq -r '.tool_input.file_path // ""'
```

`jq` は brew/apt で一度インストールすれば以後すべてのプロジェクトで使える。

### フックのデバッグ方法

フックが動かない場合は手動でパイプテスト：

```bash
# PostToolUse フックのテスト
echo '{"tool_name":"Edit","tool_input":{"file_path":"/path/to/Foo.kt"}}' \
  | jq -r '.tool_input.file_path // ""' \
  | grep -E '\.kt$' \
  && echo "フック: OK"
```

---

## MCP の設計原則

### 4種類のMCPと役割

| MCP | 認証方法 | 使いどころ |
|---|---|---|
| `gcloud` | ADC（ブラウザログイン） | Cloud Run / Secret Manager / Logs |
| `github` | PAT（Secret Manager経由） | PR作成・CI確認・issue管理 |
| `supabase` | URL + key（Secret Manager経由） | 本番DB参照・スキーマ確認 |
| `playwright` | 不要（グローバル設定） | E2E確認・ブラウザ操作 |

### なぜ Secret Manager 経由のラッパースクリプトか

`.mcp.json` は git 管理対象であるため、ここに資格情報を直書きするとリポジトリに永久に残る。ラッパースクリプトが起動時に Secret Manager から取得することで：

- **資格情報がgitに入らない**: PAT・キーがコードに現れない
- **ローテーションが容易**: Secret Manager の値を更新するだけ
- **ADCで認証**: スクリプト自体に認証情報不要

### gcloud MCP は必ず優先する

```
# ❌ Bash で gcloud コマンド（正確なコマンドを知る必要がある）
gcloud run services describe my-service --region=asia-northeast1 --format=json

# ✅ gcloud MCP（自然言語で操作できる）
「Cloud Runのmy-serviceのメモリ設定を確認して」
```

CLAUDE.md に「gcloud 操作は Bash より MCP を優先」と明記することで AI が自動選択する。

---

## セキュリティ設定のチェックリスト

```
[ ] deny リストに "Bash(rm -rf *)" を追加
[ ] deny リストに "Bash(curl *)" を追加（情報漏洩防止）
[ ] deny リストに "Bash(git push *)" を追加（意図しない push 防止）
[ ] deny リストに "Read(**/.env)" を追加
[ ] deny リストに "Read(**/*.pem)" を追加
[ ] ask リストに "Bash(docker push *)" を追加（確認必須）
[ ] gcloud auth は "Bash(gcloud auth *)" でなく read-only コマンドのみ許可
    → "Bash(gcloud auth print-access-token)"
    → "Bash(gcloud auth application-default print-access-token)"
[ ] .gitignore に .claude/settings.local.json を追加
[ ] .gitignore に .env を追加
[ ] .claude/agent-memory/ は gitignore しない（メモリ保持のため）
```

---

## CLAUDE.md 品質チェックリスト

作成後に以下を確認する：

```
[ ] プロジェクトの技術スタックが明記されている
[ ] Documentation Index に実在するファイルのみ参照している
[ ] エージェント一覧に全エージェントが記載されている
[ ] 状況別エージェント選択ガイドがある
[ ] Skills vs Agents の使い分け原則が書いてある
[ ] MCP セクションがある（gcloud 優先ルール含む）
[ ] フック設計セクションがある（全フックを一覧化）
[ ] スキル一覧が最新状態になっている
[ ] セキュリティルールが書いてある
[ ] 存在しないファイル・ツールへの参照がない
```

---

## よくある失敗パターンと対策

### ❌ CLAUDE.md を後から作る

**問題**: 実装が進んだ後に CLAUDE.md を作ると、既存の設計と矛盾する記述が生まれやすい。  
**対策**: リポジトリ作成直後・コード1行も書く前に CLAUDE.md を作る。

### ❌ agent-memory を gitignore する

**問題**: PC交換やクローン時にエージェントの記憶が全部消える。セッション間コンテキストが失われて毎回ゼロから説明することになる。  
**対策**: `.gitignore` に `.claude/agent-memory/` を書かない。

### ❌ MCP 資格情報を .mcp.json に直書きする

**問題**: git 履歴に残り、永久に削除できない。  
**対策**: 必ずラッパースクリプト + Secret Manager の組み合わせを使う。

### ❌ .mcp.json の command に絶対パスを書く

**問題**: `/Users/自分の名前/...` のような絶対パスを書くと、他のメンバーがクローンした環境で MCP が起動しない。  
**対策**: `bash -c` + `git rev-parse --show-toplevel` でリポジトリルートを動的取得する。

```json
{
  "command": "bash",
  "args": ["-c", "exec \"$(git rev-parse --show-toplevel)/.claude/mcp-scripts/github-mcp.sh\""]
}
```

### ❌ フックを python3 で書く

**問題**: `jq` が使えるのに長い python3 コマンドを書くと保守性が下がる。  
**対策**: `brew install jq` を事前に済ませ、フックは最初から jq で書く。

### ❌ gcloud auth * を丸ごと許可する

**問題**: `gcloud auth revoke` なども自動実行できてしまう。  
**対策**: `gcloud auth print-access-token` と `gcloud auth application-default print-access-token` のみを個別に許可する。

### ❌ エージェントなしで長いタスクを依頼する

**問題**: メインコンテキストが圧迫されて精度が下がる。  
**対策**: 30分以上かかりそうなタスクは適切なサブエージェントに委託する。

---

## 参考: このガイドの元プロジェクト

- プロジェクト: `note-auto-post`
- 技術スタック: Kotlin/Spring Boot 3.x + React/TanStack Router + Supabase + Gemini API
- インフラ: Google Cloud Run + Vercel + Cloud Build
- 最終スコア: **98/100（A）**
- 評価軸: CLAUDE.md・ドキュメント・エージェント・スキル・フック・MCP・セキュリティ・AI活用効果
