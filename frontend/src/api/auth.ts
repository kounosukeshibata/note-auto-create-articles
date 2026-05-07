import type { AuthResponse, AuthError } from '../types';
import { API_BASE_URL } from '../lib/apiBase';

const API_BASE = `${API_BASE_URL}/api/auth`;

export async function register(
  email: string,
  password: string,
  name: string,
): Promise<AuthResponse> {
  const res = await fetch(`${API_BASE}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, name }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw { code: err?.error?.code ?? 'VALIDATION_ERROR', message: err?.error?.message ?? '登録に失敗しました' } as AuthError;
  }
  return res.json();
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const res = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw { code: err?.error?.code ?? 'INVALID_CREDENTIALS', message: err?.error?.message ?? 'ログインに失敗しました' } as AuthError;
  }
  return res.json();
}
