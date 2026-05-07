import { marked } from 'marked';

interface ArticlePreviewProps {
  title: string;
  content: string;
  imageUrl: string;
  status: string;
}

const STATUS_LABELS: Record<string, string> = {
  GENERATED: '生成済み',
  SAVED: '保存済み',
  NOTE_DRAFTED: 'note一時保存済み',
};

marked.setOptions({ breaks: true, gfm: true });

export function ArticlePreview({ title, content, imageUrl, status }: ArticlePreviewProps) {
  const htmlContent = marked(content) as string;

  return (
    <article className="article-preview">
      {imageUrl && (
        <div className="article-eyecatch">
          <img src={imageUrl} alt="アイキャッチ画像" className="article-image" />
        </div>
      )}

      <div className="article-meta">
        <span className="article-status">{STATUS_LABELS[status] ?? status}</span>
      </div>

      <h1 className="article-title">{title}</h1>

      <div
        className="article-content"
        dangerouslySetInnerHTML={{ __html: htmlContent }}
      />
    </article>
  );
}
