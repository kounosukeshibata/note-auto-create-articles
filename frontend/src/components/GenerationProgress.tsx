import type { GenerationProgressState, GenerationStep } from '../types';

interface GenerationProgressProps {
  state: GenerationProgressState;
}

const STEP_LABELS: Record<GenerationStep, string> = {
  KEYWORDS: 'キーワード抽出中',
  SEARCH: '商品検索中',
  CONTENT: '記事生成中',
  IMAGE: '画像生成中',
  FINALIZE: '最終処理中',
};

const STEPS: GenerationStep[] = ['KEYWORDS', 'SEARCH', 'CONTENT', 'IMAGE', 'FINALIZE'];

function CheckIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <circle cx="8" cy="8" r="8" fill="var(--color-primary)" />
      <path d="M4 8l3 3 5-5" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function SpinnerIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true" className="spinner-icon">
      <circle cx="8" cy="8" r="6" stroke="var(--color-primary)" strokeWidth="2" strokeDasharray="28 10" />
    </svg>
  );
}

function CircleIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <circle cx="8" cy="8" r="7" stroke="var(--color-border)" strokeWidth="2" />
    </svg>
  );
}

function ErrorIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <circle cx="8" cy="8" r="8" fill="var(--color-error)" />
      <path d="M5 5l6 6M11 5l-6 6" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

export function GenerationProgress({ state }: GenerationProgressProps) {
  const progressPercent = Math.min(
    (state.elapsedSeconds / state.estimatedTotalSeconds) * 100,
    100,
  );

  return (
    <div className="generation-progress">
      <h2 className="progress-title">記事を生成しています...</h2>

      <div className="progress-timeline">
        {STEPS.map((step) => {
          const status = state.stepStatus[step];
          return (
            <div key={step} className={`step step-${status}`}>
              <div className="step-icon">
                {status === 'completed' && <CheckIcon />}
                {status === 'in-progress' && <SpinnerIcon />}
                {status === 'pending' && <CircleIcon />}
                {status === 'error' && <ErrorIcon />}
              </div>
              <div className="step-label">{STEP_LABELS[step]}</div>
            </div>
          );
        })}
      </div>

      <div className="progress-info">
        <p className="elapsed-time">
          {state.elapsedSeconds}秒 / {state.estimatedTotalSeconds}秒（予定）
        </p>
        <div className="progress-bar">
          <div
            className="progress-fill"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>
    </div>
  );
}
