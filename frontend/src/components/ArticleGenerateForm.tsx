import { useState } from 'react';
import type { GenerateArticleRequest, ArticleType, AffiliatePlatform, WordCount } from '../types';

interface FormState {
  theme: string;
  targetPainPoint: string;
  targetIdealState: string;
  storyTrigger: string;
  uniqueInsight: string;
  articleType: ArticleType;
  ctaInfo: string;
  affiliatePlatforms: AffiliatePlatform[];
  wordCount: WordCount | '';
}

interface FormErrors {
  theme?: string;
  affiliatePlatforms?: string;
}

interface ArticleGenerateFormProps {
  onSubmit: (req: GenerateArticleRequest) => void;
}

const ARTICLE_TYPE_LABELS: Record<ArticleType, string> = {
  '一般': '一般（無料記事）',
  'アフィリエイト': 'アフィリエイト',
  '有料(500円)': '有料（500円）',
};

function validateForm(input: FormState): FormErrors {
  const errors: FormErrors = {};
  if (!input.theme.trim()) {
    errors.theme = 'テーマは必須です';
  } else if (input.theme.length > 100) {
    errors.theme = 'テーマは100文字以内です';
  }
  if (input.affiliatePlatforms.length === 0) {
    errors.affiliatePlatforms = 'アフィリエイトASPを1つ以上選択してください';
  }
  return errors;
}

export function ArticleGenerateForm({ onSubmit }: ArticleGenerateFormProps) {
  const [form, setForm] = useState<FormState>({
    theme: '',
    targetPainPoint: '',
    targetIdealState: '',
    storyTrigger: '',
    uniqueInsight: '',
    articleType: 'アフィリエイト',
    ctaInfo: '',
    affiliatePlatforms: ['AMAZON'],
    wordCount: 2000,
  });
  const [errors, setErrors] = useState<FormErrors>({});

  const handlePlatformChange = (platform: AffiliatePlatform, checked: boolean) => {
    setForm((prev) => ({
      ...prev,
      affiliatePlatforms: checked
        ? [...prev.affiliatePlatforms, platform]
        : prev.affiliatePlatforms.filter((p) => p !== platform),
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validateForm(form);
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    const req: GenerateArticleRequest = {
      theme: form.theme.trim(),
      affiliatePlatforms: form.affiliatePlatforms,
      articleType: form.articleType,
    };
    if (form.targetPainPoint.trim()) req.targetPainPoint = form.targetPainPoint.trim();
    if (form.targetIdealState.trim()) req.targetIdealState = form.targetIdealState.trim();
    if (form.storyTrigger.trim()) req.storyTrigger = form.storyTrigger.trim();
    if (form.uniqueInsight.trim()) req.uniqueInsight = form.uniqueInsight.trim();
    if (form.ctaInfo.trim()) req.ctaInfo = form.ctaInfo.trim();
    if (form.wordCount) req.wordCount = form.wordCount;

    onSubmit(req);
  };

  const showCtaInfo = form.articleType === '有料(500円)' || form.articleType === 'アフィリエイト';

  return (
    <form className="generate-form" onSubmit={handleSubmit} noValidate>
      <h2 className="form-title">記事生成</h2>

      <div className="form-group">
        <label htmlFor="theme" className="form-label">
          記事テーマ <span className="required">*</span>
        </label>
        <textarea
          id="theme"
          className={`form-textarea ${errors.theme ? 'form-input-error' : ''}`}
          placeholder="例: 登山初心者向けトレッキングシューズ おすすめ"
          maxLength={100}
          rows={2}
          value={form.theme}
          onChange={(e) => {
            setForm((prev) => ({ ...prev, theme: e.target.value }));
            if (errors.theme) setErrors((prev) => ({ ...prev, theme: undefined }));
          }}
        />
        {errors.theme && <p className="form-error">{errors.theme}</p>}
      </div>

      <div className="form-group">
        <label htmlFor="articleType" className="form-label">
          記事タイプ <span className="required">*</span>
        </label>
        <select
          id="articleType"
          className="form-select"
          value={form.articleType}
          onChange={(e) =>
            setForm((prev) => ({ ...prev, articleType: e.target.value as ArticleType }))
          }
        >
          {(Object.keys(ARTICLE_TYPE_LABELS) as ArticleType[]).map((type) => (
            <option key={type} value={type}>
              {ARTICLE_TYPE_LABELS[type]}
            </option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label htmlFor="targetPainPoint" className="form-label">
          ターゲットの悩み・現状
        </label>
        <textarea
          id="targetPainPoint"
          className="form-textarea"
          placeholder="例: 膝が痛くて長時間歩けない、どのシューズを選べばいいかわからない"
          maxLength={200}
          rows={2}
          value={form.targetPainPoint}
          onChange={(e) => setForm((prev) => ({ ...prev, targetPainPoint: e.target.value }))}
        />
      </div>

      <div className="form-group">
        <label htmlFor="targetIdealState" className="form-label">
          ターゲットの理想・解決後
        </label>
        <textarea
          id="targetIdealState"
          className="form-textarea"
          placeholder="例: 疲れにくく、安全に山を楽しめるようになりたい"
          maxLength={200}
          rows={2}
          value={form.targetIdealState}
          onChange={(e) => setForm((prev) => ({ ...prev, targetIdealState: e.target.value }))}
        />
      </div>

      <div className="form-group">
        <label htmlFor="storyTrigger" className="form-label">
          執筆のきっかけ・ストーリーの起点
        </label>
        <textarea
          id="storyTrigger"
          className="form-textarea"
          placeholder="例: 初めての登山で靴ずれして大変だった経験から"
          maxLength={200}
          rows={2}
          value={form.storyTrigger}
          onChange={(e) => setForm((prev) => ({ ...prev, storyTrigger: e.target.value }))}
        />
      </div>

      <div className="form-group">
        <label htmlFor="uniqueInsight" className="form-label">
          独自の発見・一次情報
        </label>
        <textarea
          id="uniqueInsight"
          className="form-textarea"
          placeholder="例: 10足以上を実際に試し、ソールの硬さと防水性が最重要と気づいた"
          maxLength={300}
          rows={3}
          value={form.uniqueInsight}
          onChange={(e) => setForm((prev) => ({ ...prev, uniqueInsight: e.target.value }))}
        />
      </div>

      {showCtaInfo && (
        <div className="form-group">
          <label htmlFor="ctaInfo" className="form-label">
            紹介リンク・価格設定
          </label>
          <textarea
            id="ctaInfo"
            className="form-textarea"
            placeholder={
              form.articleType === '有料(500円)'
                ? '例: 有料部分では具体的なシューズ10選を詳細比較（500円）'
                : '例: Amazonアソシエイトリンクを各商品に設置'
            }
            maxLength={200}
            rows={2}
            value={form.ctaInfo}
            onChange={(e) => setForm((prev) => ({ ...prev, ctaInfo: e.target.value }))}
          />
        </div>
      )}

      <div className="form-group">
        <label htmlFor="wordCount" className="form-label">
          文字数目安 <span className="required">*</span>
        </label>
        <select
          id="wordCount"
          className="form-select"
          value={form.wordCount}
          onChange={(e) => {
            const val = e.target.value;
            setForm((prev) => ({
              ...prev,
              wordCount: val ? (Number(val) as WordCount) : '',
            }));
          }}
        >
          <option value={1000}>1,000文字程度</option>
          <option value={2000}>2,000文字程度</option>
          <option value={3000}>3,000文字程度</option>
          <option value={4000}>4,000文字程度</option>
        </select>
      </div>

      <div className="form-group">
        <fieldset>
          <legend className="form-label">
            アフィリエイトASP <span className="required">*</span>
          </legend>
          <div className="checkbox-group">
            {(['AMAZON'] as AffiliatePlatform[]).map((platform) => (
              <label key={platform} className="checkbox-label">
                <input
                  type="checkbox"
                  className="checkbox-input"
                  checked={form.affiliatePlatforms.includes(platform)}
                  onChange={(e) => handlePlatformChange(platform, e.target.checked)}
                />
                Amazon
              </label>
            ))}
          </div>
          {errors.affiliatePlatforms && (
            <p className="form-error">{errors.affiliatePlatforms}</p>
          )}
        </fieldset>
      </div>

      <button type="submit" className="btn btn-primary btn-generate">
        記事を生成する
      </button>
    </form>
  );
}
