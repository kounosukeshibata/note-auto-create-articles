/**
 * note.com にログインしてセッションクッキーを取得し Secret Manager を更新する。
 * 使い方: node scripts/refresh-note-cookie.mjs
 */
import { chromium } from 'playwright';
import { execSync } from 'child_process';

const PROJECT = 'project-384b1a9e-de04-4629-b84';
const SECRET  = 'note-auto-post-secret';
const EMAIL   = process.env.NOTE_EMAIL   || 'kounosukehokudai@gmail.com';
const PASSWORD = process.env.NOTE_PASSWORD || '&nosuppi1';

async function getSessionCookie() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  console.log('note.com へ移動...');
  await page.goto('https://note.com/login', { waitUntil: 'networkidle' });

  console.log('ログイン情報を入力...');
  await page.getByRole('textbox', { name: /mail@example|note ID/ }).fill(EMAIL);
  await page.getByRole('textbox', { name: 'パスワード' }).fill(PASSWORD);
  await page.getByRole('button', { name: 'ログイン' }).click();

  console.log('ログイン完了を待機...');
  await page.waitForURL('https://note.com/', { timeout: 15000 });

  const cookies = await context.cookies('https://note.com');
  await browser.close();

  const sessionCookie = cookies.find(c => c.name.startsWith('_note_session'));
  if (!sessionCookie) {
    const names = cookies.map(c => c.name).join(', ');
    throw new Error(`セッションクッキーが見つかりません。取得済み: ${names}`);
  }

  const cookieStr = `${sessionCookie.name}=${sessionCookie.value}`;
  console.log(`クッキー取得成功: ${sessionCookie.name}=***`);
  return cookieStr;
}

async function getCurrentSecret() {
  const out = execSync(
    `gcloud secrets versions access latest --secret=${SECRET} --project=${PROJECT}`,
    { encoding: 'utf8' }
  );
  return JSON.parse(out);
}

function updateSecret(data) {
  const json = JSON.stringify(data);
  execSync(
    `gcloud secrets versions add ${SECRET} --project=${PROJECT} --data-file=-`,
    { input: json, encoding: 'utf8', stdio: ['pipe', 'inherit', 'inherit'] }
  );
}

(async () => {
  try {
    const cookie   = await getSessionCookie();
    const current  = await getCurrentSecret();

    // NOTE_EMAIL / NOTE_PASSWORD は不要なので削除し NOTE_SESSION_COOKIE を追加
    const { NOTE_EMAIL, NOTE_PASSWORD, ...rest } = current;
    const updated = { ...rest, NOTE_SESSION_COOKIE: cookie };

    console.log('Secret Manager を更新...');
    updateSecret(updated);
    console.log('完了: NOTE_SESSION_COOKIE を更新しました。');
  } catch (err) {
    console.error('失敗:', err.message);
    process.exit(1);
  }
})();
