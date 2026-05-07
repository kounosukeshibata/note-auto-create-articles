// In local dev: empty string → Vite proxy handles /api/* → localhost:8080
// In production (Vercel): VITE_API_BASE_URL = Cloud Run URL (e.g. https://xxx.run.app)
export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '';
