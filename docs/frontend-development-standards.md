# フロントエンド開発標準

## ディレクトリ構造

```
frontend/src/
  api/           バックエンドAPIとの通信関数（axios等）
  components/    再利用可能なUIコンポーネント
  contexts/      React Context（認証状態など）
  layouts/       ページレイアウトコンポーネント
  lib/           汎用ユーティリティ・設定
  routes/        TanStack Router のルートコンポーネント（ページ）
  types/         TypeScript 型定義（ドメイン型を含む）
```

## 命名規則

| 種別 | 命名パターン | 例 |
|---|---|---|
| コンポーネント | PascalCase | `ArticlePreview`, `GenerationProgress` |
| ルートファイル | TanStack Router の規約に従う | `generate.tsx`, `preview/$articleId.tsx` |
| Context | PascalCase + Context | `AuthContext` |
| カスタムフック | use + PascalCase | `useArticleForm`, `useAuth` |
| API 関数 | 動詞 + 名詞 | `generateArticle`, `loginUser` |
| 型定義 | PascalCase | `GenerateFormInput`, `ArticleOutput`, `WordCount` |

命名はユビキタス言語 (`docs/domain/ubiquitous.md`) に準拠すること。

## TypeScript 型安全性ルール

- `any` の使用は禁止。型が不明な場合は `unknown` を使い型ガードを書く。
- TanStack Router のルート型を活用し、`useParams()` は型安全に使用する。
- バックエンドとの通信型は `src/types/index.ts` で定義し、API 関数と共有する。
- `as` による型アサーションは、型ガードで確認済みの場合のみ許可。

```typescript
// NG: any 使用
const data: any = await fetchArticle(id)

// OK: 型を明示
const data: ArticleOutput = await fetchArticle(id)
```

## TanStack Router ルール

- ルートコンポーネントは `src/routes/` 配下にファイルベースで配置する。
- `routeTree.gen.ts` は自動生成ファイル。直接編集しない。
- ナビゲーションは `useNavigate` または `<Link>` を使用（`window.location` 禁止）。
- 動的ルートパラメーターは `Route.useParams()` で型安全に取得する。

```typescript
// OK: 型安全なパラメーター取得
const { articleId } = Route.useParams()
```

## コンポーネント設計ルール

- 1コンポーネント1ファイル。コンポーネントは `default export` する。
- Props 型は `interface Props {}` として同ファイル内に定義する。
- フォームは React Hook Form または `useState` を使い、非制御コンポーネントを避ける。
- ローディング状態・エラー状態を必ず考慮する。

## API 通信パターン

```typescript
// src/api/articles.ts の基本パターン
import { apiBase } from '../lib/apiBase'

export async function generateArticle(input: GenerateFormInput): Promise<ArticleOutput> {
  const res = await apiBase.post('/api/articles/generate', input)
  return res.data
}
```

- `apiBase` (`src/lib/apiBase.ts`) の axios インスタンスを使用する（`fetch` 直接使用は避ける）。
- JWT トークンは `apiBase` インターセプターで自動付与される。
- エラーハンドリングはコンポーネント側で try/catch するか、エラーバウンダリを使う。

## テストルール

| 対象 | テスト種別 |
|---|---|
| 型定義・ユーティリティ関数 | ユニットテスト（Vitest） |
| フォームコンポーネント | コンポーネントテスト（Testing Library） |
| E2E フロー | Playwright（`.playwright-mcp/` 経由） |

## 環境変数

- ブラウザに公開される値のみ `VITE_` プレフィックスを付ける。
- API キーはクライアントに持たせない（バックエンド経由で呼び出す）。

```
VITE_API_BASE_URL=http://localhost:8080  # バックエンドURL
```

## セキュリティ

- `dangerouslySetInnerHTML` は禁止。
- ユーザー入力は表示前にサニタイズする。
- 認証トークンは `localStorage` または `sessionStorage` に保存し、Cookie には使用しない（現在の実装に合わせる）。
