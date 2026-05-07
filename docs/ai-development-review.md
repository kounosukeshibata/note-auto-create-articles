# AI開発体験レビュー & 改善提案

> 対象: `note-auto-post` プロジェクト  
> 評価日: 2026-05-08（Claude Code機能活用を新規評価軸として追加）  
> 評価観点: CLAUDE.md・ドキュメント・サブエージェント・スキル・**フック・MCP**（今回追加）・セキュリティ・AI活用効果

---

## 総合スコア: 70 / 100 — **C-**

> **今回のポイント**: フックとMCPを評価軸に加えた結果、前回88点から70点に再評価。既存の基盤（ドキュメント・エージェント）は引き続き高水準だが、Claude Codeのプラットフォーム機能（フック・MCP）がほぼ未活用であることが判明。伸びしろが最も大きい領域。

| カテゴリ | 配点 | 得点 | 割合 | グレード |
|---|---|---|---|---|
| CLAUDE.md & コンテキスト設計 | 15 | 13 | 87% | **B+** |
| ドキュメント整備 | 15 | 14 | 93% | **A** |
| サブエージェント設計 | 15 | 14 | 93% | **A** |
| スキル整備 | 10 | 7 | 70% | **C** |
| **フック活用** | 15 | 2 | 13% | **F** |
| **MCP活用** | 10 | 3 | 30% | **D** |
| セキュリティ・権限設計 | 10 | 9 | 90% | **A** |
| AI活用の実際の効果 | 10 | 8 | 80% | **B** |

### グレード基準

| グレード | 割合 | 意味 |
|---|---|---|
| A+ | 97-100% | 模範的。他プロジェクトの参考になるレベル |
| A | 93-96% | 優秀。ほぼ最大限に活用できている |
| A- | 90-92% | 良好。わずかな改善余地あり |
| B+ | 87-89% | 平均以上。具体的な改善点がある |
| B | 83-86% | 及第点。重要な機能を活用しきれていない |
| B- | 80-82% | 基本は押さえているが課題が多い |
| C+ | 77-79% | 改善が必要な状態 |
| C | 73-76% | 基本的な活用にとどまっている |
| C- | 70-72% | 大きな未活用領域がある |
| D | 60-69% | 最低限しか使えていない |
| F | 60%未満 | ほぼ未活用 |

---

## 1. CLAUDE.md & コンテキスト設計 — 13 / 15｜B+

### 良い点

- **行動指針が具体的**: TDD強制・エラー時の推測修正禁止・破壊的変更前の許可取得など、AIの暴走を防ぐルールが明文化されている。
- **ツールインベントリが充実**: エージェント一覧・スキル一覧・MCP一覧・状況別ガイドが揃い、AIが自律選択できる状態。
- **ドキュメントインデックス**: 主要ドキュメントへの参照が揃い、AIがコンテキスト不足に陥りにくい。

### 残る改善点

**① スキルとエージェントの使い分け原則がない**

`documentation-writer`（スキル）と `docs-manager`（エージェント）のように類似機能が混在している。1行追加するだけで解決できる:

```markdown
# Skills vs Agents 使い分け原則
# Skills (.agents/skills/): AIの振る舞いを強化。/スキル名 で呼び出す。呼び出し元のコンテキストで動く。
# Agents (.claude/agents/): タスクを独立して委託。別コンテキストで動く。並行実行も可能。
```

**② フックとMCPの設定説明がない**

CLAUDE.md にフックの設計思想（「どのイベントでどのツールを自動起動するか」）が書かれていないため、AIがフックを考慮した提案をしにくい。

---

## 2. ドキュメント整備 — 14 / 15｜A

### 良い点

- **ユビキタス言語辞書**: Gemini 2.5 Flash / Gemini API に修正済み。命名ブレが起きにくい。
- **ドメインモデル図**: 集約境界・値オブジェクト・シーケンス図が現在の実装と一致。
- **レイヤードキュメント**: `docs/layer/` の各層の禁止事項まで明文化されており、AIが誤実装しにくい。
- **`backend/frontend-development-standards.md` 新設**: 命名規則・テスト規則・パッケージ配置が整備され、エージェントが標準を参照できる。
- **`deployment.md` 新設**: Cloud Run / Vercel 手順を一元化。

### 残る改善点

**① プロンプトファイルの管理ポリシーがない**

`prompts/vertex/*.md` はアプリの記事品質を決定する核心だが、変更ルール・バージョン戦略・A/Bテスト方針が文書化されていない。`docs/prompt-management.md` を新設することを推奨。

---

## 3. サブエージェント設計 — 14 / 15｜A

### 良い点

- **開発ライフサイクルの全フェーズをカバー**: backend・frontend・TDD・review・bug・設計・docs・インフラの8役割。
- **`examples` が充実**: 各エージェント定義に具体的なユーザー発言例と選択理由（commentary）が書かれており、AIが自律選択できる。
- **エージェントメモリ稼働中**: `backend-impl-ddd` / `design-architect` のメモリに UseCase フロー・設計判断・実装状態が記録済み。セッション間コンテキスト再構築コストが大幅削減。

### 残る改善点

**① `gcloud-infra-ops` と MCP `gcloud` の役割重複**

どちらを使うか毎回判断が発生する。使い分けルールを CLAUDE.md に追記するだけで解決できる:

| 用途 | 推奨ツール |
|---|---|
| 単発コマンド（describe, list, secrets access） | MCP `gcloud` 直接 |
| 複数ステップ作業（デプロイ、ログ調査、権限設定） | `gcloud-infra-ops` エージェント |

---

## 4. スキル整備 — 7 / 10｜C

### 良い点

- **`test-driven-development`**: Red-Green-Refactor の哲学・アンチパターン・「言い訳への反論」まで含む最高品質のスキル。
- **`clean-ddd-hexagonal`**: レイヤー配置のデシジョンツリー付き。AIが自律判断できる。
- **スキル構造**: SKILL.md + references/ サブディレクトリでコンテキストウィンドウを節約しながら詳細参照できる設計。

### 残る改善点

**① Vercel デプロイスキルがない** → 今セッションの最大トラブルの再発防止。

**② プロンプトエンジニアリングスキルがない** → プレースホルダー規約・parseContent() との整合性・スタブ→本番検証フロー。

---

## 5. フック活用 — 2 / 15｜F 🔴 最重要改善領域

### 現状

唯一設定されているフックは「Stop 時にドキュメント更新を促すリマインダー」のみ。Claude Code のフック機能はほぼ未活用。

```json
// 現在の唯一のフック（.claude/settings.local.json）
"Stop": [{ "hooks": [{ "type": "command", "command": "echo '{\"systemMessage\": \"...\"}'"}] }]
```

### フックが解決する問題

フックは「AIが作業するたびに人間が手動でやっていた作業を自動化する」仕組み。以下はこのプロジェクトで即効果がある設定:

---

### 改善案 ①: コード保存時に自動コンパイル確認（PostToolUse）

Kotlin ファイルを編集するたびにコンパイルエラーを即座に検出できる。

```json
// .claude/settings.local.json に追加
"PostToolUse": [{
  "matcher": "Write|Edit",
  "hooks": [{
    "type": "command",
    "command": "jq -r '.tool_input.file_path // .tool_response.filePath // empty' | grep -E '\\.kt$' | { read -r f; [ -n \"$f\" ] && cd /Users/shibatakonosuke/git/note/note-auto-post/backend && ./gradlew compileKotlin --no-daemon -q 2>&1 | tail -5; } || true",
    "timeout": 60,
    "statusMessage": "Kotlin コンパイル確認中..."
  }]
}]
```

**効果**: 「コードを書いた → 別途コンパイルを依頼する → エラーを確認する」という往復が0になる。

---

### 改善案 ②: TypeScript 型チェック自動実行（PostToolUse）

```json
"PostToolUse": [{
  "matcher": "Write|Edit",
  "hooks": [{
    "type": "command",
    "command": "jq -r '.tool_input.file_path // .tool_response.filePath // empty' | grep -E '\\.(tsx?|ts)$' | { read -r f; [ -n \"$f\" ] && cd /Users/shibatakonosuke/git/note/note-auto-post/frontend && npx tsc --noEmit 2>&1 | tail -5; } || true",
    "timeout": 30,
    "statusMessage": "TypeScript 型チェック中..."
  }]
}]
```

---

### 改善案 ③: テストファイル変更時に自動テスト実行（PostToolUse）

```json
"PostToolUse": [{
  "matcher": "Write|Edit",
  "hooks": [{
    "type": "command",
    "command": "jq -r '.tool_input.file_path // .tool_response.filePath // empty' | grep -E 'Test\\.kt$' | { read -r f; [ -n \"$f\" ] && cd /Users/shibatakonosuke/git/note/note-auto-post/backend && ./gradlew test --no-daemon -q 2>&1 | tail -20; } || true",
    "timeout": 120,
    "statusMessage": "テスト実行中..."
  }]
}]
```

**効果**: テストファイルを書くと即座に Red/Green が判明。TDD のフィードバックループが格段に速くなる。

---

### 改善案 ④: プロンプトファイル変更時に警告（PostToolUse）

```json
"PostToolUse": [{
  "matcher": "Write|Edit",
  "hooks": [{
    "type": "command",
    "command": "jq -r '.tool_input.file_path // .tool_response.filePath // empty' | grep 'prompts/' | { read -r f; [ -n \"$f\" ] && echo '{\"systemMessage\": \"⚠️ プロンプトファイルを変更しました。記事品質に影響します。スタブモード(vertex.ai.stub=true)で動作確認してください。\"}'; } || true"
  }]
}]
```

**効果**: プロンプト変更は記事品質に直結するため、意図せず変更した場合のリスクを即座に通知できる。

---

### 改善案 ⑤: コンパクション前にセッション状態を保存（PreCompact）

```json
"PreCompact": [{
  "matcher": "auto",
  "hooks": [{
    "type": "command",
    "command": "echo \"$(date): セッションコンパクション発生\" >> /Users/shibatakonosuke/git/note/note-auto-post/.claude/session-log.txt && git -C /Users/shibatakonosuke/git/note/note-auto-post diff --stat >> /Users/shibatakonosuke/git/note/note-auto-post/.claude/session-log.txt 2>/dev/null || true"
  }]
}]
```

**効果**: コンテキストが圧縮される直前に変更状態をログとして保存。「どこまで作業したか」の証跡が残る。

---

### 改善案 ⑥: セッション開始時にプロジェクト状態を表示（SessionStart）

```json
"SessionStart": [{
  "hooks": [{
    "type": "command",
    "command": "cd /Users/shibatakonosuke/git/note/note-auto-post && echo '{\"systemMessage\": \"'\"$(git log --oneline -3 | tr '\\n' ' ')\"'\"}'"
  }]
}]
```

**効果**: セッション開始時に最新3コミットを自動表示。前回作業の続きを把握しやすくなる。

---

## 6. MCP活用 — 3 / 10｜D 🔴 重要改善領域

### 現状

```json
// .mcp.json - 登録済みMCP
{ "mcpServers": { "gcloud": { "command": "npx", "args": ["-y", "@google-cloud/gcloud-mcp"] } } }
```

- `gcloud` MCP: 登録済みだが、実際の作業では Bash コマンドを直接実行することが多く、MCP のメリットを活かしきれていない。
- `Playwright` MCP: グローバル設定で有効だが、E2E テストとしての体系的な活用はない。
- **GitHub MCP・Supabase MCP**: 未登録。このプロジェクトで最も効果的なMCPが未活用。

### MCP が「Bash コマンドの実行」と何が違うのか

| 観点 | Bash コマンド | MCP |
|---|---|---|
| 自然言語 | 正確なコマンドを知っている必要がある | 「Cloudのログを確認して」でOK |
| エラー処理 | 手動でエラーを解釈する | MCPサーバーが意味のある形で返す |
| 複合操作 | 複数コマンドを組み合わせる | 1つの意図で複数操作を実行 |
| コンテキスト共有 | 出力をコピペして渡す | ツール結果が直接モデルのコンテキストに入る |

---

### 改善案 ①: GitHub MCP の導入 ★最優先

```json
// .mcp.json に追加
{
  "mcpServers": {
    "gcloud": { "command": "npx", "args": ["-y", "@google-cloud/gcloud-mcp"] },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": { "GITHUB_PERSONAL_ACCESS_TOKEN": "（Secret Managerから取得）" }
    }
  }
}
```

**できるようになること:**
- 「このコミットでPRを作って」→ PR作成・タイトル/本文の自動生成
- 「CI が落ちているか確認して」→ GitHub Actions の状態確認
- 「直近のissueを確認して実装の優先度を決めて」→ issue一覧から判断

---

### 改善案 ②: Supabase MCP の導入

```json
"supabase": {
  "command": "npx",
  "args": ["-y", "@supabase/mcp-server-supabase", "--supabase-url", "https://xxx.supabase.co", "--supabase-key", "service_role_key"]
}
```

**できるようになること:**
- 「本番の記事テーブルのスキーマを確認して」→ DB定義をAIが直接参照
- 「直近10件の記事データを見せて」→ デバッグ時にSupabaseダッシュボードを開かずに済む
- 「テスト用データを挿入して」→ E2Eテストのフィクスチャ管理

---

### 改善案 ③: gcloud MCP をもっと活用する

現在は `mcp__gcloud__run_gcloud_command` が許可リストにあるが、実際の作業では `Bash(gcloud *)` が使われることが多い。MCP を優先することで自然言語で操作できる:

```
# 現在（Bash直接）
gcloud run services describe note-auto-post-backend --region asia-northeast1

# MCP活用後（自然言語指示）
「バックエンドのCloud Run設定を確認して」
```

CLAUDE.md の MCP セクションに「`gcloud` 操作は原則 MCP を使う」と明記することで、AIが自動的に MCP を選択するようになる。

---

### 改善案 ④: Playwright MCP をE2Eテストとして体系化

現在はデバッグ用途で単発使用されているが、デプロイ後の自動E2E確認フローとして整備できる:

```markdown
# .agents/skills/e2e-verification/SKILL.md
## デプロイ後E2E確認フロー
1. Playwright でログイン → 記事生成フォーム表示確認
2. フォームに入力 → 生成実行
3. プレビュー画面でコンテンツ確認
4. アフィリエイトリンクが挿入されているか確認
```

---

## 7. セキュリティ・権限設計 — 9 / 10｜A

### 良い点

- `deny` に `curl`・`git push`・`.env` 読み取りを明示。情報漏洩と意図しない push を防止。
- Secret Manager で全 APIキーを管理（ハードコード禁止）。
- `gcloud secrets *` を読み取り系4コマンドに絞り込み済み（前回対応）。
- `xargs cat *` を許可リストから削除済み（前回対応）。

### 残る改善点

**① `Bash(docker push *)` が自動許可されている**

docker push はコンテナレジストリへの書き込みで、誤ったイメージを push するリスクがある。確認ステップを挟む設定への変更を検討:

```json
// 推奨: deny に追加し、明示的に許可する場合のみ実行
"ask": ["Bash(docker push *)"]
```

---

## 8. AI活用の実際の効果 — 8 / 10｜B

### 良い点

- **DDDアーキテクチャの一貫性**: レイヤー構造・リポジトリパターン・値オブジェクト・ドメインサービスが正しく実装されており、AIが標準を維持できている。
- **インフラ切替設計**: `@ConditionalOnProperty` による InMemory ↔ Supabase JPA 切替は、DDDの「ドメインをインフラから分離する」原則の正しい実践。
- **プロンプトをコード管理**: `prompts/vertex/*.md` を git 管理しており、プロンプト変更の差分が追跡できる。
- **サブエージェントの実際の活用**: コード生成・インフラ操作・ドキュメント更新を専門エージェントに分担させており、意図通りに機能している。

### 残る改善点

**① TDD が後追いになる場面がある**

インターフェース変更時にモック引数がズレるなど、テストが実装変更に追従する形になった。以下のフローを徹底することで改善できる:

```
1. tdd-implementation-runner にインターフェース変更を伝える
2. テストを先に更新（Red）→ ./gradlew test で失敗確認
3. 実装を変更（Green）→ ./gradlew test で全テスト通過
```

**② フックがないため「実装したら終わり」になりやすい**

コード編集後のコンパイル確認・テスト実行・ドキュメント更新がすべて手動依頼。フック（カテゴリ5）を整備することで、AIが実装後に自動確認する習慣が付く。

---

## 優先改善アクション

### 🔴 今すぐやる（フック・MCP）

1. **PostToolUse フック: Kotlin コンパイル自動確認を追加する**  
   → `.kt` ファイル編集後に `./gradlew compileKotlin` が自動実行される。コンパイルエラーの往復ゼロ。

2. **PostToolUse フック: テストファイル変更時に自動テスト実行を追加する**  
   → TDDのフィードバックループが格段に速くなる。

3. **GitHub MCP を `.mcp.json` に追加する**  
   → PR作成・CI確認・issue参照が自然言語で操作できる。開発フローの中心になる。

4. **CLAUDE.md に「gcloud 操作は MCP を優先する」を明記する**  
   → AIが自動的に MCP を選択するようになり、gcloud 操作の品質が上がる。

### 🟡 次にやる（スキル・ドキュメント）

5. **PostToolUse フック: TypeScript 型チェック自動実行を追加する**  
   → フロントエンド変更後に `tsc --noEmit` が自動実行される。

6. **PreCompact フック: セッション状態をログに保存する**  
   → コンテキスト圧縮前に変更状態を記録。「どこまでやったか」の証跡が残る。

7. **`.agents/skills/vercel-deployment/` スキルを新設する**  
   → Vercel 手順・`vercel link` セットアップ・GitHub 自動デプロイ設定を文書化。

8. **Supabase MCP を `.mcp.json` に追加する**  
   → 本番DBのスキーマ確認・デバッグクエリを自然言語で実行できる。

### 🟢 余裕があれば

9. **SessionStart フック: 最新コミット表示を追加する**  
   → セッション開始時に前回作業状態を自動把握。

10. **プロンプト変更時警告フック（PostToolUse）を追加する**  
    → `prompts/` 配下の変更時に記事品質への影響を自動通知。

11. **`.agents/skills/prompt-engineering/` スキルを新設する**  
    → プレースホルダー規約・検証フローを文書化。

12. **`docs/prompt-management.md` を新設する**  
    → プロンプトの変更ルール・バージョン戦略・A/Bテスト方針。

---

## 対応済み項目

| 指摘 | 対応内容 |
|---|---|
| `ubiquitous.md` に Gemini 1.5 Pro / Vertex AI の誤記 | Gemini 2.5 Flash / Gemini API に修正済み |
| シーケンス図に削除済みの NoteClient | 現在の UseCase フローに更新済み |
| `docs/deployment.md` がない | 新設済み（Cloud Run / Vercel 手順・チェックリスト） |
| エージェントメモリが空 | `backend-impl-ddd` / `design-architect` に詳細記録済み |
| 存在しないドキュメントへの参照 | CLAUDE.md から削除済み |
| `design.md` の旧フォーム設計 / API仕様 | wordCount・JWT 認証に更新済み |
| `backend/frontend-development-standards.md` が未作成 | 両ファイル新設済み |
| `.claude/agent-memory/` が gitignore 対象 | gitignore から除外済み |
| `gcloud secrets *` 権限が過剰 | 読み取り系4コマンドに絞り込み済み |
| `xargs cat *` が許可されている | 権限リストから削除済み |
| ドキュメント更新が手動依頼のみ | Stop フック追加済み |

---

## まとめ

### 何が優れているか（A以上の領域）

このプロジェクトは **ドキュメント・エージェント・セキュリティ** において国内でも上位レベルの設計ができている:

- サブエージェントの専門分業（8役割）と `examples` の充実度
- `@ConditionalOnProperty` による Supabase/InMemory 切替という DDDを正しく実践したインフラ設計
- Secret Manager によるシークレット管理の徹底
- TDD・DDD スキルの哲学的な深さ

### 何が弱いか（D以下の領域）

**フックとMCPはほぼ未活用**。これは「道具はあるのに使っていない」状態。

| 現在の状況 | 理想の状態 |
|---|---|
| コード書いた → 手動でコンパイル依頼 | コード書いた → フックが自動でコンパイル確認 |
| `gcloud` コマンドを正確に入力 | 「Cloud Runのログ確認して」で済む（MCP） |
| PR作成は `gh` コマンドを覚える必要あり | 「このブランチでPRを作って」で済む（GitHub MCP） |
| デプロイ後のE2E確認は手動 | Playwright MCP が自動でUI確認 |

### 次のレベルへの鍵

**「AIが作業するたびに自動で品質チェックが走る状態」を作ること。**  
フック3本（コンパイル・テスト・型チェック）と GitHub MCP の追加だけで、開発体験が劇的に変わる。

> **フックとMCPの本質**: スキルやエージェントが「AIに何をさせるか」を定義するのに対し、フックは「AIが作業したときに何が自動で起きるか」を定義し、MCPは「AIが使える道具を増やす」。この3層が揃って初めてAI-Nativeな開発環境になる。
