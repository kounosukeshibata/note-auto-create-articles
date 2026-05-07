-- ============================================================
-- NOTE AUTO POST — Supabase (PostgreSQL) スキーマ定義
-- ============================================================
-- 適用方法:
--   Supabase ダッシュボード > SQL Editor でこのファイルを実行する
--   または: psql $DATABASE_URL -f supabase/schema.sql
-- ============================================================

-- updated_at を自動更新するトリガー関数
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- ユーザーテーブル
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
  id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  email        VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  name         VARCHAR(255) NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER users_set_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- email での検索に使用
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- ============================================================
-- 記事テーブル
-- ============================================================
-- keywords カラム格納形式 (TEXT — JSON 配列文字列):
--   ["SEOキーワード1", "SEOキーワード2", ...]
--
-- affiliate_links カラム格納形式 (TEXT — JSON 配列文字列):
--   [
--     {
--       "url": "https://...",
--       "trackingId": "tracking-amazon-0",
--       "platform": "AMAZON",
--       "productName": "商品名",
--       "price": "9800",
--       "category": "electronics",
--       "thumbnailUrl": "https://...",
--       "commissionRate": 0.08
--     }
--   ]
-- ============================================================
CREATE TABLE IF NOT EXISTS articles (
  id              UUID         PRIMARY KEY,
  user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  title           VARCHAR(500) NOT NULL,
  content         TEXT         NOT NULL,
  image_url       TEXT         NOT NULL,
  image_alt_text  VARCHAR(500) NOT NULL DEFAULT '',
  keywords        TEXT         NOT NULL DEFAULT '[]',
  affiliate_links TEXT         NOT NULL DEFAULT '[]',
  status          VARCHAR(50)  NOT NULL
                    CHECK (status IN ('GENERATED', 'SAVED', 'NOTE_DRAFTED')),
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TRIGGER articles_set_updated_at
  BEFORE UPDATE ON articles
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ユーザーごとの記事一覧取得（findAllByUserId）に使用
CREATE INDEX IF NOT EXISTS idx_articles_user_id ON articles (user_id);

-- ステータスでの絞り込みが必要になった場合に備えて
CREATE INDEX IF NOT EXISTS idx_articles_status ON articles (status);

-- ============================================================
-- Row Level Security (RLS)
-- ============================================================
-- アプリケーション側で JWT 認証を行っているため、
-- Supabase の anon キーを直接 API から呼ぶ構成では使用しない。
-- Spring Boot バックエンドが service_role 相当の権限で接続するため
-- RLS は無効のまま運用する（必要に応じて有効化すること）。
ALTER TABLE users   DISABLE ROW LEVEL SECURITY;
ALTER TABLE articles DISABLE ROW LEVEL SECURITY;
