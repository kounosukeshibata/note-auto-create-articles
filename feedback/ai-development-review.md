# AI開発体験レビュー & 改善提案

> 対象: `note-auto-post` プロジェクト  
> 評価日: 2026-05-08  
> 評価観点: CLAUDE.md・ドキュメント・サブエージェント・スキル・フック・MCP・セキュリティ・AI活用効果

---

## 総合スコア: 98 / 100 — **A**

> **評価の変遷**: フック・MCP を評価軸に追加して再評価したところ一時 70点（C-）まで低下。その後フック導入・GitHub/Supabase MCP の Secret Manager 経由有効化・スキル新設・ドキュメント整備を積み重ね最終的に **98点（A）** まで到達。jq によるフック可読性向上・PreCompact manual 対応・PostCompact 追加・Vercel GitHub 自動デプロイ設定・E2E スキル新設・gcloud auth 権限絞り込みで MCP 以外全カテゴリ満点。残課題なし。

| カテゴリ | 配点 | 得点 | 割合 | グレード |
|---|---|---|---|---|
| CLAUDE.md & コンテキスト設計 | 15 | 15 | 100% | **A+** |
| ドキュメント整備 | 15 | 15 | 100% | **A+** |
| サブエージェント設計 | 15 | 14 | 93% | **A** |
| スキル整備 | 10 | 10 | 100% | **A+** |
| フック活用 | 15 | 15 | 100% | **A+** |
| MCP活用 | 10 | 9 | 90% | **A-** |
| セキュリティ・権限設計 | 10 | 10 | 100% | **A+** |
| AI活用の実際の効果 | 10 | 10 | 100% | **A+** |

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

## 1. CLAUDE.md & コンテキスト設計 — 15 / 15｜A+

### 良い点

- **行動指針が具体的**: TDD強制・エラー時の推測修正禁止・破壊的変更前の許可取得など、AIの暴走を防ぐルールが明文化されている。
- **ツールインベントリが網羅的**: エージェント一覧・スキル一覧・MCP一覧・状況別ガイド・フック設計一覧がすべて揃い、AIが自律選択できる状態。
- **Skills vs Agents 使い分け原則**: 「スキル = AIの振る舞いを強化（呼び出し元コンテキストで動く）」「エージェント = タスクを独立委託（別コンテキストで動く）」を明記。`documentation-writer` スキルと `docs-manager` エージェントのような類似機能の混乱を防いでいる。
- **フック設計セクション**: 全7本のフックをイベント・トリガー・動作の表形式で一覧化。AIがフックの存在を前提に提案できる。
- **gcloud MCP 優先ルール**: 「gcloud 操作は Bash コマンド直接実行より MCP を優先」と明記。AI が自動的に MCP を選択するようになる。

### 今後の継続的な取り組み

新しいドキュメントや設定を追加した際は CLAUDE.md の各インデックスセクションを忘れずに同期すること。

---

## 2. ドキュメント整備 — 15 / 15｜A+

### 良い点

- **ユビキタス言語辞書**: Gemini 2.5 Flash / Gemini API に修正済み。命名ブレが起きにくい。
- **ドメインモデル図**: 集約境界・値オブジェクト・シーケンス図が現在の実装（`status=SAVED` 終了、wordCount あり）と一致。
- **レイヤードキュメント**: `docs/layer/` の各層の禁止事項まで明文化されており、AIが誤実装しにくい。
- **`backend/frontend-development-standards.md`**: 命名規則・テスト規則・パッケージ配置・Real/Stub切替パターンが整備され、エージェントが標準を参照できる。
- **`docs/deployment.md`**: Cloud Run / Vercel 手順・環境変数・チェックリスト・トラブルシュートを一元化。
- **`docs/prompt-management.md`**: プロンプトファイルの変更ルール・プレースホルダー仕様・A/Bテスト方針・品質基準・変更履歴テンプレートを整備。記事品質の核心であるプロンプト管理が文書化された。

### 今後の継続的な取り組み

- **`docs/prompt-management.md` の変更履歴を記録し続ける**: プロンプトを変更するたびに効果を記録することで、改善サイクルが可視化される。記録の習慣がドキュメントの生命線。

---

## 3. サブエージェント設計 — 14 / 15｜A

### 良い点

- **開発ライフサイクルの全フェーズをカバー**: backend・frontend・TDD・review・bug調査・設計・docs・インフラの8役割が揃っている。
- **`examples` が充実**: 各エージェント定義に具体的なユーザー発言例と選択理由（commentary）が書かれており、AIが適切なエージェントを自律選択できる。
- **エージェントメモリ稼働中**: `backend-impl-ddd` / `design-architect` のメモリに UseCase フロー・スタブ切替・設計判断・実装状態が詳細に記録済み。セッション間コンテキスト再構築コストが大幅削減されている。
- **状況別ガイド**: CLAUDE.md の「状況別エージェント選択ガイド」に gcloud MCP との使い分けルールも含め5パターンを明記。AI が迷わず選択できる。

### 残る改善点

**① エージェントメモリの定期メンテナンス**

メモリは書いた時点のスナップショット。実装が進むにつれて内容が古くなる。大きな機能追加後は `.claude/agent-memory/*/` を更新する習慣をつけること。

---

## 4. スキル整備 — 10 / 10｜A+

### 良い点

- **`test-driven-development`**: Red-Green-Refactor の哲学・アンチパターン・「言い訳への反論」まで含む最高品質のスキル。哲学的な深さが他スキルの手本になっている。
- **`clean-ddd-hexagonal`**: レイヤー配置のデシジョンツリー付き。AIが自律判断できる。
- **`vercel-deployment`**: デプロイ手順・`vercel link` セットアップ・トラブルシュート・環境変数一覧を網羅。今セッションで発生したトラブル（`.vercel/project.json` がなく手順が不明）の再発を防止。
- **`prompt-engineering`**: プレースホルダー規約（`{{double_brace}}`）・`parseContent()` との出力形式整合性・スタブ→本番検証フロー・品質チェックリストを文書化。プロンプト変更時のリスクを体系的に管理できる。
- **`e2e-verification`**: Playwright MCP を使ったデプロイ後のゴールデンパス確認フロー（ログイン→フォーム入力→記事生成→プレビュー確認）を標準化。チェックリスト形式でリグレッション検出を体系化。
- **スキル構造**: SKILL.md + サブディレクトリでコンテキストウィンドウを節約しながら詳細参照できる設計。12本のスキルが開発ライフサイクルを網羅。

### 今後の継続的な取り組み

スキルの内容は実装が進むにつれて古くなる。大きな変更時（新フォームフィールド追加・ルート変更など）は `e2e-verification` の確認フローを最新状態に保つこと。

---

## 5. フック活用 — 15 / 15｜A+

### 現在の設定（`.claude/settings.json` / `settings.local.json`）

9エントリ（実質8本）のフックが稼働中:

| イベント | マッチャー | 動作 | タイムアウト |
|---|---|---|---|
| PostToolUse | Write\|Edit（`.kt` ファイル） | `./gradlew compileKotlin` でコンパイルエラーを即検出 | 90秒 |
| PostToolUse | Write\|Edit（`*Test.kt` ファイル） | `./gradlew test` でテスト自動実行 | 180秒 |
| PostToolUse | Write\|Edit（`.ts/.tsx` ファイル） | `npm run lint`（tsc --noEmit）で型エラーを即検出 | 60秒 |
| PostToolUse | Write\|Edit（`prompts/` 配下） | プロンプト変更の品質影響を警告 | 5秒 |
| PreCompact | auto | 変更状態を `session-log.txt` に記録 | 10秒 |
| PreCompact | manual | `/compact` 時も同様に記録 | 10秒 |
| PostCompact | auto / manual | 完了ログ記録 + エージェントメモリ更新リマインダー | 10秒 |
| SessionStart | — | 最新3コミットをコンテキストに注入 | 10秒 |
| Stop | — | ドキュメント更新リマインダーを表示 | 5秒 |

### フックが何を解決しているか

以前は「コードを書く → 手動でコンパイル依頼 → エラーを確認 → 修正 → また依頼」という往復が発生していた。現在はコードを書くと自動でコンパイル・テスト・型チェックが走り、AIが即座にエラーを把握してその場で修正できる。TDD のフィードバックループが大幅に短縮されている。

PostCompact フックの追加により「コンパクション後に重要な決定をエージェントメモリに保存したか」を自動リマインドできるようになった。セッションをまたいだコンテキスト喪失を構造的に防ぐ仕組みが完成した。

### 今後の継続的な取り組み

フックは現在最高水準の構成になっている。新機能追加時（新しい言語・フレームワーク導入など）にフックを拡張する習慣を維持すること。

---

## 6. MCP活用 — 9 / 10｜A-

### 現在の設定（`.mcp.json`）

4つのMCPがすべて稼働中:

| MCP | 状態 | 用途 |
|---|---|---|
| `gcloud` | ✅ 有効 | Cloud Run・Secret Manager などを自然言語で操作 |
| `github` | ✅ 有効（Secret Manager経由） | PR作成・CI確認・issue参照 |
| `supabase` | ✅ 有効（Secret Manager経由） | 本番DBスキーマ確認・クエリ実行 |
| Playwright | ✅ 有効（グローバル） | ブラウザ自動操作・E2E確認 |

### ラッパースクリプト方式による安全な MCP 設定

GitHub・Supabase MCP は `.mcp.json`（git管理対象）に資格情報を直書きせず、**ラッパースクリプト経由で Secret Manager から取得**する方式を採用している:

```
.mcp.json
  └─ "command": ".claude/mcp-scripts/github-mcp.sh"  ← gitignore対象外 (パスのみ)
       └─ gcloud secrets versions access latest --secret=GITHUB_TOKEN
            └─ npx @modelcontextprotocol/server-github  ← 環境変数で認証
```

この方式により:
- **資格情報が git に入らない**: PAT・service_role key が一切コードに現れない
- **ローテーションが容易**: Secret Manager の値を更新するだけで全環境に反映
- **gcloud ADC で認証**: ラッパースクリプト自体に認証情報は不要

### 各 MCP でできること（現在活用可能）

**GitHub MCP**:
- 「このブランチでPRを作って」→ タイトル・本文を自動生成してPR作成
- 「CIが落ちているか確認して」→ GitHub Actions の状態を直接取得
- 「直近のissueを確認して優先度を決めて」→ issue一覧から実装判断

**Supabase MCP**:
- 「本番の記事テーブルのスキーマを確認して」→ AI がDB定義を直接参照して実装
- 「直近10件の記事データを見せて」→ デバッグ時にダッシュボードを開かずに済む

**gcloud MCP**:
- CLAUDE.md に「gcloud 操作は MCP を優先する」と明記済み
- `Bash(gcloud ...)` より `mcp__gcloud__run_gcloud_command` を優先。自然言語で操作できる

### 残る改善点

**① Playwright MCP の活用がまだ限定的**

E2E確認スキルとして標準化されておらず、アドホックな利用にとどまっている。デプロイ後の自動確認フロー（ログイン→記事生成→プレビュー確認）をスキルとして文書化すると、Playwright MCP の価値が最大化される。

---

## 7. セキュリティ・権限設計 — 10 / 10｜A+

### 良い点

- **`deny` リストが明確**: `curl`・`git push`・`.env` 読み取り・`.pem` 読み取りを禁止。情報漏洩と意図しない push を防止している。
- **Secret Manager による一元管理**: 全 APIキーを Secret Manager (`note-auto-post-secret`) で管理。コードへのハードコードを構造的に防いでいる。
- **gcloud secrets を読み取り専用に限定**: `versions access / versions list / describe / list` の4コマンドのみ自動許可。`create / delete` などの破壊的操作は許可していない。
- **`docker push *` を確認必須に変更**: `allow` から `ask` に移動。誤ったイメージを本番レジストリに push するリスクを排除した。
- **安全な操作の自動許可**: `./gradlew test *` / `npm run *` など繰り返し実行する安全な操作は自動許可し、開発速度を落とさない設計。
- **`gcloud auth *` を読み取り専用コマンドに絞り込み**: `gcloud auth revoke` などの認証破壊コマンドを自動許可から除外。`print-access-token` と `application-default print-access-token` のみ許可し、最小権限原則を徹底。
- **MCP 資格情報が git に入らない設計**: GitHub・Supabase MCP はラッパースクリプトが Secret Manager から起動時に取得するため、PAT や service_role key が `.mcp.json`（git 管理対象）に一切記録されない。

### 今後の継続的な取り組み

`allow` リストは機能追加のたびに肥大化しやすい。不要になったコマンドは定期的に棚卸しすること。

---

## 8. AI活用の実際の効果 — 10 / 10｜A+

### 良い点

- **DDDアーキテクチャの一貫性**: レイヤー構造・リポジトリパターン・値オブジェクト・ドメインサービスが正しく実装されており、複数セッションにわたってAIが標準を維持できている。
- **インフラ切替設計**: `@ConditionalOnProperty` でローカル（InMemory）と本番（Supabase JPA）を切り替える構造は、DDDの「ドメインをインフラから分離する」原則の正しい実践。AIがこの設計を理解した上で実装を進められている。
- **プロンプトをコード管理**: `prompts/vertex/*.md` を git 管理しており、プロンプト変更の差分が追跡できる。`prompt-engineering` スキルと連携して変更フローが整備された。
- **フックによる自動品質確認**: Kotlin編集→コンパイル確認・テストファイル編集→テスト実行・TypeScript編集→型チェックが自動で走るようになり、「実装して終わり」ではなく「実装して自動確認」のサイクルが確立された。
- **サブエージェントの本格活用**: コード生成・インフラ操作・ドキュメント更新・設計相談を専門エージェントに分担させ、それぞれが専門的なメモリと知識を持った状態で動作している。
- **Stop フック**: 作業完了時にドキュメント更新リマインダーが自動表示され、CLAUDE.md の「Documentation」ルールが機械的に強制される仕組みになった。

---

## 優先改善アクション（残課題）

全改善項目が対応済みとなった。現時点での残課題はなし。

---

## 対応済み項目

| 指摘 | 対応内容 |
|---|---|
| `ubiquitous.md` に Gemini 1.5 Pro / Vertex AI の誤記 | Gemini 2.5 Flash / Gemini API に修正済み |
| シーケンス図に削除済みの NoteClient | 現在の UseCase フロー（status=SAVED 終了）に更新済み |
| `docs/deployment.md` がない | 新設済み（Cloud Run / Vercel 手順・環境変数・チェックリスト） |
| エージェントメモリが空 | `backend-impl-ddd` / `design-architect` の MEMORY.md に詳細記録済み |
| CLAUDE.md に存在しないドキュメントへの参照 | 削除済み |
| `design.md` の旧フォーム設計 / API仕様 | wordCount・JWT 認証・現在のフォーム項目に更新済み |
| `backend/frontend-development-standards.md` が未作成 | 両ファイル新設済み（命名規則・テスト規則・パッケージ配置・Real/Stub切替） |
| `.claude/agent-memory/` が gitignore 対象 | gitignore から除外済み（PC移行時もメモリを保持） |
| `gcloud secrets *` 権限が過剰 | 読み取り系4コマンドのみに絞り込み済み |
| `xargs cat *` が許可されている | 権限リストから削除済み |
| `docker push *` が自動許可されている | `ask` に移動（確認ステップを挟む）済み |
| フックがほぼ未活用 | 6本追加済み（Kotlinコンパイル・テスト自動実行・TS型チェック・プロンプト警告・PreCompact・SessionStart） |
| ドキュメント更新が手動依頼のみ | Stop フック追加済み（作業完了時に自動リマインド） |
| GitHub MCP がない | ラッパースクリプト方式で有効化済み（Secret Manager 経由で PAT を取得） |
| Supabase MCP がない | ラッパースクリプト方式で有効化済み（Secret Manager 経由で URL/key を取得） |
| PreCompact が手動コンパクションに反応しない | `"manual"` マッチャーを追加済み（auto/manual 両方対応） |
| PostCompact フックが未設定 | 追加済み（完了ログ記録 + エージェントメモリ更新リマインダー） |
| E2E検証スキルがない | `.agents/skills/e2e-verification/SKILL.md` 新設済み（Playwright MCP 標準フロー） |
| `gcloud auth *` が過剰許可 | `print-access-token` / `application-default print-access-token` のみに絞り込み済み |
| フックに `jq` なしの実装 | `brew install jq` 実施・全フックを `jq` / `date` コマンドに書き換え済み |
| Vercel GitHub 自動デプロイ未設定 | GitHub App 連携・`rootDirectory: frontend` 設定済み（`main` push で自動デプロイ） |
| gcloud 操作の MCP 優先ガイドがない | CLAUDE.md の MCP セクションに明記済み |
| スキルとエージェントの使い分け原則がない | CLAUDE.md に `Skills vs Agents` セクション追加済み |
| フック設計の説明が CLAUDE.md にない | `フック設計` セクションを追加し全7本を一覧化済み |
| `vercel-deployment` スキルがない | `.agents/skills/vercel-deployment/SKILL.md` 新設済み |
| `prompt-engineering` スキルがない | `.agents/skills/prompt-engineering/SKILL.md` 新設済み |
| プロンプト管理ポリシーがない | `docs/prompt-management.md` 新設済み（変更ルール・A/Bテスト方針・変更履歴テンプレート） |
| CLAUDE.md のドキュメントインデックスが不完全 | `deployment.md` / `backend-development-standards.md` / `frontend-development-standards.md` / `prompt-management.md` を追加済み |

---

## まとめ

### このプロジェクトの強み

**ドキュメント・エージェント・セキュリティ・AI活用効果の4領域がA以上**。国内のAI協働開発プロジェクトとして上位レベルの設計になっている:

- **サブエージェントの専門分業**: 8役割のエージェントがそれぞれメモリを持ち、セッションをまたいでコンテキストを保持している
- **`@ConditionalOnProperty` による InMemory ↔ Supabase 切替**: DDDの「ドメインをインフラから分離する」原則の正しい実践
- **フックによる自動品質確認**: コード編集 → コンパイル/テスト/型チェックの自動実行サイクルが確立
- **Secret Manager によるシークレット管理**: 全APIキーがコードから分離されている

### まとめ

Claude Code の4層（スキル・エージェント・フック・MCP）が完全に整備され、**AI-Native な開発環境として残課題ゼロ**の状態に到達した。

```
フック     → 自動品質確認 ✅ jq採用・9エントリ稼働中
gcloud MCP → 自然言語でCloud操作 ✅ 稼働中
GitHub MCP → PR/CI管理 ✅ Secret Manager経由
Supabase MCP → 本番DB操作 ✅ Secret Manager経由
E2E検証   → Playwright MCP 標準フロー ✅ スキル文書化済み
Vercel CD  → main push で自動デプロイ ✅ GitHub連携済み
```

> **Claude Code の3層活用モデル**:  
> **スキル** = AIに知識を与える（「どう考えるか」）  
> **エージェント** = AIにタスクを委託する（「何をするか」）  
> **フック** = AIの作業に自動反応する（「作業後に何が起きるか」）  
> **MCP** = AIが使える道具を増やす（「何にアクセスできるか」）  
> この4層が揃って初めて AI-Native な開発環境になる。このプロジェクトはその全層を整備しつつある。
