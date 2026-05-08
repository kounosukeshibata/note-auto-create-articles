---
name: prompt-engineering
description: Use when modifying Gemini prompts in prompts/vertex/, debugging article generation quality, or managing prompt versions
---

# プロンプトエンジニアリング

## このプロジェクトのプロンプト構造

```
prompts/vertex/
  content-generation.md   記事本文生成プロンプト（メイン）
  image-generation.md     画像生成プロンプト（将来用）
```

プロンプトは `PromptLoader` が読み込み、`RealVertexAiClient.generateContent()` に渡される。

## プレースホルダー規約

プロンプト内の変数置換には **二重波括弧** `{{variable_name}}` を使用する。

```
{{theme}}           記事テーマ
{{article_type}}    記事タイプ（ハウツー / レビュー等）
{{target_pain}}     ターゲットの悩み
{{target_ideal}}    ターゲットの理想状態
{{story}}           ストーリー・体験談
{{unique_info}}     独自情報
{{cta}}             CTA（行動喚起）
{{word_count}}      文字数（1000/2000/3000/4000）
```

**注意**: `{single_brace}` ではなく `{{double_brace}}` を使うこと。混在するとプレースホルダーが未置換のまま出力される。

## parseContent() との整合性

`RealVertexAiClient.parseContent()` はレスポンスを以下の形式でパースする:

```
# タイトル
（本文）
```

1行目が `#` で始まる場合、タイトルとして抽出し、残りが本文になる。プロンプトで指示する出力形式はこれに合わせること。

```markdown
# 出力形式指示（プロンプトに含めること）
以下の形式で出力してください：
1行目: `# [記事タイトル]`
2行目以降: 記事本文（Markdown形式）
```

## 変更フロー

1. **スタブモードで確認**: `vertex.ai.stub=true`（ローカルデフォルト）でテンプレートの構造を確認
2. **変更を加える**: `prompts/vertex/content-generation.md` を編集
3. **スタブで出力を確認**: スタブはプロンプトをそのまま返すため、置換が正しく機能しているか確認できる
4. **本番モードで動作確認**: Secret Manager で `VERTEX_AI_STUB=false` に変更後、実際の Gemini API 応答を確認
5. **品質確認**: 生成された記事のタイトル・構成・アフィリエイトリンク挿入位置を確認
6. **git commit**: 変更内容とその効果をコミットメッセージに記載

## 品質チェックリスト

- [ ] プレースホルダーが全て `{{double_brace}}` 形式になっている
- [ ] 出力形式が `# タイトル\n本文` になっている
- [ ] `{{product_link_N}}` プレースホルダーの数が指示した商品リンク数と一致している（最大5件）
- [ ] 文字数指示が `{{word_count}}` で正しく渡されている
- [ ] スタブモードで出力に未置換の `{{xxx}}` が残っていない

## 変更時の注意

- プロンプトの変更は記事品質に直接影響する
- `PostToolUse` フックが `prompts/` 配下の変更を検知して警告を出す
- 変更理由・期待する効果・実際の結果を `docs/prompt-management.md` に記録すること
- A/Bテストが必要な場合は `prompts/vertex/content-generation-v2.md` のように別ファイルで管理する
