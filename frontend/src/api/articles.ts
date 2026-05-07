import type { GenerateArticleRequest, GenerateArticleResponse, GenerationError } from '../types';
import { API_BASE_URL } from '../lib/apiBase';

const API_BASE = `${API_BASE_URL}/api`;

function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function generateArticle(req: GenerateArticleRequest): Promise<GenerateArticleResponse> {
  const res = await fetch(`${API_BASE}/articles/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(req),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw mapApiError(res.status, err);
  }
  return res.json() as Promise<GenerateArticleResponse>;
}

export async function getArticle(id: string): Promise<GenerateArticleResponse> {
  const res = await fetch(`${API_BASE}/articles/${id}`, {
    headers: { ...authHeaders() },
  });
  if (!res.ok) throw mapApiError(res.status, {});
  return res.json() as Promise<GenerateArticleResponse>;
}

export async function postArticleToDraft(id: string): Promise<{ noteUrl: string }> {
  const res = await fetch(`${API_BASE}/articles/${id}/draft`, {
    method: 'POST',
    headers: { ...authHeaders() },
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw mapApiError(res.status, err);
  }
  return res.json() as Promise<{ noteUrl: string }>;
}

function mapApiError(status: number, body: { error?: { code?: string; message?: string } }): GenerationError {
  const code = (body?.error?.code ?? (status === 503 ? 'AI_SERVICE_UNAVAILABLE' : 'INTERNAL_SERVER_ERROR')) as GenerationError['code'];
  return {
    code,
    message: body?.error?.message ?? 'エラーが発生しました',
    retryable: status === 503,
  };
}
