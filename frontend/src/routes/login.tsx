import { createRoute, redirect, useNavigate } from '@tanstack/react-router';
import { useState } from 'react';
import { flushSync } from 'react-dom';
import { Route as rootRoute } from './__root';
import { useAuth } from '../contexts/AuthContext';
import { login as loginApi, register as registerApi } from '../api/auth';
import type { AuthError } from '../types';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/login',
  beforeLoad: ({ context }) => {
    if (context.isAuthenticated) {
      throw redirect({ to: '/generate' });
    }
  },
  component: LoginPage,
});

type Mode = 'login' | 'register';

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<Mode>('login');

  // 共通フィールド
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  // 新規登録のみ
  const [name, setName] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (mode === 'register' && password !== confirmPassword) {
      setError('パスワードが一致しません');
      return;
    }

    setIsSubmitting(true);
    try {
      const response =
        mode === 'login'
          ? await loginApi(email, password)
          : await registerApi(email, password, name);

      flushSync(() => {
        login(response.token, {
          id: response.userId,
          name: response.name,
          email: response.email,
        });
      });
      await navigate({ to: '/generate' });
    } catch (err) {
      const authErr = err as AuthError;
      if (authErr.code === 'INVALID_CREDENTIALS') {
        setError('メールアドレスまたはパスワードが正しくありません');
      } else if (authErr.code === 'USER_ALREADY_EXISTS') {
        setError('このメールアドレスはすでに登録されています');
      } else {
        setError(authErr.message ?? 'エラーが発生しました');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const switchMode = (newMode: Mode) => {
    setMode(newMode);
    setError(null);
    setEmail('');
    setPassword('');
    setName('');
    setConfirmPassword('');
  };

  return (
    <div className="login-page">
      <div className="login-brand">
        <h1 className="login-title">NOTE AUTO POST</h1>
        <p className="login-subtitle">収益をデザインする</p>
      </div>

      <div className="login-form-container">
        {/* モード切り替えタブ */}
        <div className="auth-tabs">
          <button
            type="button"
            className={`auth-tab ${mode === 'login' ? 'auth-tab-active' : ''}`}
            onClick={() => switchMode('login')}
          >
            ログイン
          </button>
          <button
            type="button"
            className={`auth-tab ${mode === 'register' ? 'auth-tab-active' : ''}`}
            onClick={() => switchMode('register')}
          >
            新規登録
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          {mode === 'register' && (
            <div className="form-group">
              <label htmlFor="name" className="form-label">
                名前 <span className="required">*</span>
              </label>
              <input
                id="name"
                type="text"
                className="form-input"
                placeholder="山田 太郎"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
          )}

          <div className="form-group">
            <label htmlFor="email" className="form-label">
              メールアドレス <span className="required">*</span>
            </label>
            <input
              id="email"
              type="email"
              className="form-input"
              placeholder="example@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">
              パスワード <span className="required">*</span>
            </label>
            <input
              id="password"
              type="password"
              className="form-input"
              placeholder={mode === 'register' ? '8文字以上' : 'パスワードを入力'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={mode === 'register' ? 8 : undefined}
              required
            />
          </div>

          {mode === 'register' && (
            <div className="form-group">
              <label htmlFor="confirmPassword" className="form-label">
                パスワード（確認） <span className="required">*</span>
              </label>
              <input
                id="confirmPassword"
                type="password"
                className="form-input"
                placeholder="もう一度入力"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
          )}

          {error && <p className="auth-error">{error}</p>}

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? '処理中...'
              : mode === 'login'
              ? 'ログイン'
              : 'アカウントを作成'}
          </button>
        </form>
      </div>
    </div>
  );
}
