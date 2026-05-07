# AI開発体験レビュー & 改善提案

> 対象: `note-auto-post` プロジェクト  
> 評価日: 2026-05-08  
> 評価観点: CLAUDE.md・ドキュメント整備・サブエージェント設計・スキル活用・権限設計・実際のAI活用効果

---

## 総合スコア: 76 / 100

| カテゴリ | 配点 | 得点 | 評価 |
|---|---|---|---|
| CLAUDE.md 設計 | 20 | 15 | B+ |
| ドキュメント整備 | 25 | 17 | B |
| サブエージェント設計 | 20 | 17 | A- |
| スキル整備 | 15 | 12 | B+ |
| セキュリティ・権限設計 | 10 | 8 | A- |
| AI活用の実際の効果 | 10 | 7 | B+ |

---

## 1. CLAUDE.md 設計 — 15 / 20

### 良い点

- **行動指針が明確**: TDD強制・エラー時の推測修正禁止・破壊的変更前の許可取得など、AIの暴走を防ぐルールが具体的に書かれている。これは上級レベルの設計。
- **セキュリティルール**: `curl` 禁止・`rm` 確認要求など、副作用の大きいコマンドへのガードが明文化されている。
- **エージェント/スキル/MCPのインベントリ**: 何が使えるかを一覧化しており、AIが自律的に正しいツールを選べる。

### 改善点

**① バージョン不整合**

```diff
- React SPA (Frontend) と Kotlin Spring Boot 4.x (Backend)
+ React SPA (Frontend) と Kotlin Spring Boot 3.x (Backend)
```

CLAUDE.md のプロジェクト概要に `4.x` と書かれているが実装は `3.x`。AIが間違ったバージョン前提でコードを書くリスクがある。

**② 存在しないドキュメントへの参照**

```
- Standards/Rules: docs/backend-development-standards.md, docs/frontend-development-standards.md
```

これらのファイルが `docs/` に存在しない。AIが参照しようとして「見つからない」と混乱する原因になる。  
→ ファイルを作成するか、CLAUDE.md から削除する。

**③ エージェント選択の判断基準が抽象的**

現状は「何をするか」の列挙だが、「どの状況でどのエージェントを選ぶか」の判断基準がない。例:

```
# 推奨: 状況別エージェント選択ガイドを追加
| 状況 | 使うエージェント | 理由 |
|---|---|---|
| 新機能の設計相談 | design-architect | ドキュメント参照してから実装方針を決める |
| 設計決定後の実装 | backend-impl-ddd または tdd-implementation-runner | TDDで実装 |
| 既存コードが壊れた | bug-investigator | 原因特定を先行させる |
```

---

## 2. ドキュメント整備 — 17 / 25

### 良い点

- **ユビキタス言語辞書**: 日本語↔英語コード名の対応が揃っており、AIが命名で迷わない。
- **ドメインモデル図**: Mermaid記法で集約境界・値オブジェクトが可視化されており、設計の一貫性を保てる。
- **レイヤー別ドキュメント**: `docs/layer/` に各層の責務・設計ルール・禁止事項が明文化されており、AIが層を跨いだ誤実装をしにくい。

### 改善点

**① ドキュメントと実装の乖離が発生している**

開発中に仕様が変わったが、以下が更新されていない:

| ドキュメント | 古い記述 | 現在の実装 |
|---|---|---|
| `ubiquitous.md` > 本文 | `Gemini 1.5 Pro が生成した記事` | Gemini 2.5 Flash |
| `ubiquitous.md` > 外部サービス | `Vertex AI` (Google CloudのAIプラットフォーム) | Gemini API を直接呼び出し（Vertex AI SDKは未使用） |
| `domain-model-diagram.md` > シーケンス図 | `UC->>Note: postDraft(article)` が最後にある | 現在の UseCase は DB保存で終了。note投稿は未実装 |
| `ubiquitous.md` > 状態 | `note一時保存済み / NoteDrafted` | 現在到達不可能な状態（UseCase内で呼ばれていない） |

**→ ドキュメントと実装のズレはAIが古い仕様で実装し直す原因になる。**

**② `backend-development-standards.md` / `frontend-development-standards.md` が存在しない**

CLAUDE.md とエージェント定義の双方から参照されているが、ファイルが存在しない。  
AIが「読もうとして見つからない→スキップ」という動作になっており、標準化の効果が出ていない。

**③ デプロイ手順ドキュメントがない**

今回のセッションで Vercel 未連携・リポジトリ未コミット・Artifact Registry権限エラーなど複数のデプロイ失敗が発生した。これはドキュメントで防げたはず:

```markdown
# 推奨: docs/deployment.md を新設
## バックエンド (Cloud Run)
- Cloud Build が git push をトリガー
- Artifact Registry: asia-northeast1-docker.pkg.dev/project-384b1a9e-de04-4629-b84/note-auto-post/backend

## フロントエンド (Vercel)
- プロジェクト: nosukes-projects-92b05a53/frontend
- GitHub 自動デプロイ: 未設定。npx vercel --prod で手動デプロイ
- 本番URL: https://frontend-seven-psi-22.vercel.app

## 環境変数
- Secret Manager バージョン管理: note-auto-post-secret (現 v13)
- vertex.ai.stub=false で RealVertexAiClient が有効になる
```

**④ プロンプトファイルの管理ポリシーがない**

`prompts/vertex/content-generation.md` などのAIプロンプトはこのプロジェクトの重要な資産だが、管理ルールがない。

```markdown
# 推奨: docs/prompt-management.md を新設
## プロンプトの変更ルール
- プロンプト変更後は必ずスタブモードで動作確認する
- 大幅な変更前には git tag でバージョンを残す
- プレースホルダー名は {snake_case} に統一する
```

---

## 3. サブエージェント設計 — 17 / 20

### 良い点

- **網羅性**: backend・frontend・TDD・review・bug調査・設計・docs・インフラと、開発ライフサイクルの全フェーズをカバーしている。
- **examples が充実**: 各エージェントの定義に具体的なユーザー発言例と選択理由（commentary）が書かれており、AIが適切なエージェントを選べる。
- **メモリシステムの実装**: `project` スコープのメモリをエージェントごとに持ち、セッション間で知識が継続される設計になっている。

### 改善点

**① エージェントメモリが空のまま**

`backend-impl-ddd` と `design-architect` の `MEMORY.md` が現時点で空。メモリシステムは設計されているが、知識の蓄積が始まっていない。

これは「使うたびに1からコンテキストを再構築している」状態であり、エージェントの最大の強みが活かせていない。

```markdown
# 推奨: 最初の数タスク後に明示的にメモリを作成する
# backend-impl-ddd のメモリ例

---
name: "コアドメインパターン"
type: project
---
- ArticleRepository は InMemoryArticleRepository のみ（本番DB未実装）
- AffiliatePlatform は現在 AMAZON のみ。RAKUTEN はドメインに定義済みだが未実装
- StubVertexAiClient が vertex.ai.stub=true（デフォルト）で有効
```

**② `gcloud-infra-ops` と MCP `gcloud` の役割が重複**

`gcloud-infra-ops` サブエージェントと、`mcp__gcloud__run_gcloud_command` ツールが機能的に重複している。使い分けルールが明確でないため、どちらを使うか迷う。

```markdown
# 推奨: CLAUDE.md に明記する
| 用途 | 使うツール |
|---|---|
| 1コマンドの操作 (services describe, secrets list) | mcp__gcloud 直接 |
| 複数ステップのインフラ作業（デプロイ、権限設定） | gcloud-infra-ops エージェント |
```

---

## 4. スキル整備 — 12 / 15

### 良い点

- **`test-driven-development` スキル**: Red-Green-Refactor の哲学・アンチパターン・よくある言い訳への反論まで網羅した最高品質のスキル。
- **`clean-ddd-hexagonal` スキル**: 「どのレイヤーに書くか」のデシジョンツリー付き。DDDの判断をAIが自律的にできるように設計されている。
- **リファレンスドキュメント構造**: スキル本体は薄く保ち、詳細を `references/` サブディレクトリに分けている設計が良い。

### 改善点

**① Vercel デプロイスキルがない**

今回のセッションで最も時間を要したトラブルシューティング（Vercel 未連携・未デプロイ）は、スキルがあれば防げた。

```markdown
# 推奨: .agents/skills/vercel-deployment/ を新設
## セットアップ確認チェックリスト
- [ ] .vercel/project.json が存在するか確認
- [ ] 存在しない場合: npx vercel link でリポジトリに紐付け
- [ ] GitHub 自動デプロイの設定確認
## 手動デプロイ
cd frontend && npx vercel --prod
```

**② プロンプトエンジニアリングスキルがない**

このアプリは Gemini プロンプトが品質の核心だが、プロンプトの書き方・テスト方法・改善方法のスキルがない。

```markdown
# 推奨: .agents/skills/prompt-engineering/ を新設
## プレースホルダー規約
- {snake_case} 形式を使う
- プレースホルダーは必ずプロンプト末尾のパラメータ一覧に記載する
## プロンプト品質チェック
- スタブモードで出力テンプレートを確認してからリアルモードでテスト
- 出力パース部分 (parseContent) とプロンプト形式が一致しているか確認
```

**③ スキルの「いつ使うか」が一部曖昧**

`documentation-writer` スキルと `docs-manager` サブエージェントの使い分けが不明確。  
→ スキルは「AIの振る舞いを強化する」、エージェントは「独立したタスクを委託する」という原則をCLAUDE.mdに明記する。

---

## 5. セキュリティ・権限設計 — 8 / 10

### 良い点

- `deny` リストに `curl`・`git push`・`.env` 読み取りを明示しており、意図しない情報漏洩を防いでいる。
- Secret Manager でAPIキーを管理し、コードへのハードコードを禁止している。
- `Bash(./gradlew test *)` のように、テスト実行は自動許可しているため開発速度が落ちない。

### 改善点

**① `Bash(gcloud secrets *)` が過剰に広い**

```json
"Bash(gcloud secrets *)"
```

これは `gcloud secrets create`・`gcloud secrets delete` も許可してしまう。

```json
// 推奨: 操作別に絞る
"Bash(gcloud secrets versions access *)",
"Bash(gcloud secrets versions list *)",
"Bash(gcloud secrets describe *)"
```

**② `Bash(xargs cat *)` が許可されている**

`xargs cat` は任意のファイルを読み込める汎用コマンドで、`Read` ツールで代替できる。意図的な追加か確認が必要。

---

## 6. AI活用の実際の効果 — 7 / 10

### 良い点

- **アーキテクチャへの一貫性**: DDDのレイヤー構造、リポジトリパターン、値オブジェクト、ドメインサービスが正しく実装されており、AIが標準を守っている。
- **AIへのプロンプト委譲**: 記事生成ロジックをAIに任せつつ、プレースホルダーパターンでコードの統制を保っている設計は上手い。
- **サブエージェントの実際の活用**: コード生成・レビュー・インフラ操作・ドキュメント更新を専門エージェントに分担させており、意図通りに機能している。

### 改善点

**① スタブ/本番の切り替えが本番環境で管理されていなかった**

`vertex.ai.stub=true` がデフォルトのため、Secret Manager に `false` を明示しないとスタブが動く。  
これによりMDテンプレートがそのまま記事として出力される障害が発生した。

```markdown
# 推奨: docs/deployment.md に以下を明記
## 本番デプロイ前チェックリスト
- [ ] Secret Manager に vertex.ai.stub=false が設定されているか
- [ ] Secret Manager に amazon.stub=false が設定されているか
```

**② TDDが後追い気味になる場面があった**

`GenerateAffiliateArticleUseCaseTest` のモック引数修正など、テストが実装に追従する形になった箇所がある。  
`tdd-implementation-runner` エージェントを機能変更時に積極的に呼び出すことで改善できる。

**③ ドキュメントの同期が手動で行われている**

機能実装後に「ドキュメントも更新してください」と手動で依頼している。これは自動化できる。

```json
// 推奨: PostToolUse フックで、コード変更後にドキュメント確認を促す
{
  "hooks": {
    "Stop": [{
      "hooks": [{
        "type": "command",
        "command": "echo '{\"systemMessage\": \"実装完了。docs/ の関連ドキュメントを更新しましたか？\"}'"
      }]
    }]
  }
}
```

---

## 優先改善アクション（推奨順）

### 🔴 高優先度（すぐやる）

1. **`docs/backend-development-standards.md` と `docs/frontend-development-standards.md` を作成する**  
   → CLAUDE.md・エージェントから参照されているが存在しない。最も影響が大きい空白。

2. **`ubiquitous.md` の古い記述を修正する**  
   → `Gemini 1.5 Pro` → `Gemini 2.5 Flash`、`Vertex AI` → `Gemini API`、`NoteDrafted` 状態の扱いを整理。

3. **`domain-model-diagram.md` のシーケンス図から `NoteClient` を削除する**  
   → 現在の UseCase はDB保存で終わる。note投稿フローが未実装であることを明示する。

4. **`docs/deployment.md` を新設する**  
   → Cloud Run / Vercel のデプロイ手順・チェックリスト・本番環境変数一覧。

### 🟡 中優先度（今週中）

5. **エージェントメモリを初期化する**  
   → `backend-impl-ddd`・`design-architect` の MEMORY.md に、現在のドメインパターン・既知の制約・命名規則を手動で記録する。

6. **CLAUDE.md のバージョン不整合を修正する**  
   → `Spring Boot 4.x` → `3.x`。

7. **`.agents/skills/vercel-deployment/` スキルを新設する**  
   → 今回のトラブルを再発防止するため。

8. **`gcloud secrets *` 権限を絞り込む**  
   → 読み取り系のみに限定する。

### 🟢 低優先度（余裕があれば）

9. **`.agents/skills/prompt-engineering/` スキルを新設する**  
   → プロンプトMDの書き方・テスト方法・改善サイクルを文書化する。

10. **Stop フックでドキュメント更新リマインダーを追加する**  
    → 実装完了後の自動チェックポイント。

11. **`docs/prompt-management.md` を新設する**  
    → プロンプトファイルの変更ルール・バージョン戦略。

---

## まとめ

このプロジェクトは **生成AIをフル活用した開発体験として、国内でも上位レベルの設計** をしている。サブエージェントの専門分業・スキルの哲学的な深さ・セキュリティ意識の高さは特に優れている。

一方で、**ドキュメントと実装の乖離** と **エージェントメモリの未活用** が最大の課題。  
AIは「今見えているもの」を信じるため、古いドキュメントは正しいコードより強い影響を持つことがある。  
ドキュメントを常に実装より1歩先に保つ習慣が、AIとの協働品質を次のレベルに引き上げる鍵になる。

> **AIとの協働の鉄則**: AIはドキュメントを「真実」として扱う。コードを変えたら、必ずドキュメントを先に（または同時に）変える。
