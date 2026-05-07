import type { GenerationError, GenerationErrorCode } from '../types';

interface ErrorModalProps {
  error: GenerationError;
  onRetry: () => void;
  onDismiss: () => void;
  retryCount: number;
  maxRetries: number;
}

function getUserFriendlyMessage(code: GenerationErrorCode): string {
  const messages: Record<GenerationErrorCode, string> = {
    VALIDATION_ERROR: '入力内容が正しくありません。テーマと画像プロンプトを確認してください',
    UNAUTHORIZED: '認証が失効しました。再度ログインしてください',
    AI_SERVICE_UNAVAILABLE: 'AI処理が一時的に利用できません。しばらく経ってから再度お試しください',
    AFFILIATE_API_UNAVAILABLE: '商品情報が一時的に取得できません。しばらく経ってから再度お試しください',
    INTERNAL_SERVER_ERROR: '予期しないエラーが発生しました。サポートにお問い合わせください',
  };
  return messages[code];
}

function AlertIcon() {
  return (
    <svg width="40" height="40" viewBox="0 0 40 40" fill="none" aria-hidden="true">
      <circle cx="20" cy="20" r="20" fill="var(--color-error)" opacity="0.1" />
      <path
        d="M20 12v10M20 26v2"
        stroke="var(--color-error)"
        strokeWidth="2.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

export function ErrorModal({ error, onRetry, onDismiss, retryCount, maxRetries }: ErrorModalProps) {
  const canRetry = error.retryable && retryCount < maxRetries;
  const userMessage = getUserFriendlyMessage(error.code);

  return (
    <div className="error-modal">
      <div className="error-icon">
        <AlertIcon />
      </div>
      <h2 className="error-title">生成に失敗しました</h2>
      <p className="error-message">{userMessage}</p>

      <div className="error-actions">
        {canRetry && (
          <button type="button" className="btn btn-primary" onClick={onRetry}>
            リトライ（{retryCount + 1}/{maxRetries}）
          </button>
        )}
        {!canRetry && error.code === 'INTERNAL_SERVER_ERROR' && (
          <a
            href="mailto:support@example.com?subject=記事生成エラー"
            className="btn btn-secondary"
          >
            サポートに連絡
          </a>
        )}
        <button type="button" className="btn btn-tertiary" onClick={onDismiss}>
          フォームに戻る
        </button>
      </div>
    </div>
  );
}
