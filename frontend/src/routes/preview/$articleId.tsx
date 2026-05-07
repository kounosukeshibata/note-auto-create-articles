import { createRoute, redirect, useNavigate } from '@tanstack/react-router';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Route as rootRoute } from '../__root';
import { getArticle, postArticleToDraft } from '../../api/articles';
import { ArticlePreview } from '../../components/ArticlePreview';
import { AffiliateLinkList } from '../../components/AffiliateLinkList';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/preview/$articleId',
  beforeLoad: ({ context }) => {
    if (!context.isAuthenticated) {
      throw redirect({ to: '/login' });
    }
  },
  component: PreviewPage,
});

function PreviewPage() {
  const { articleId } = Route.useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isDrafting, setIsDrafting] = useState(false);
  const [draftError, setDraftError] = useState<string | null>(null);

  const { data: article, isLoading, isError } = useQuery({
    queryKey: ['article', articleId],
    queryFn: () => getArticle(articleId),
    staleTime: 1000 * 60 * 5,
  });

  const handleBack = () => {
    void navigate({ to: '/generate' });
  };

  const handleDraftToNote = async () => {
    setIsDrafting(true);
    setDraftError(null);
    try {
      await postArticleToDraft(articleId);
      await queryClient.invalidateQueries({ queryKey: ['article', articleId] });
    } catch {
      setDraftError('note への一時保存に失敗しました。再度お試しください。');
    } finally {
      setIsDrafting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="preview-loading">
        <div className="spinner" />
        <p>記事を読み込み中...</p>
      </div>
    );
  }

  if (isError || !article) {
    return (
      <div className="preview-error">
        <p>記事の取得に失敗しました。</p>
        <button type="button" className="btn btn-secondary" onClick={handleBack}>
          フォームに戻る
        </button>
      </div>
    );
  }

  const alreadyDrafted = article.status === 'NOTE_DRAFTED';

  return (
    <div className="preview-page">
      <div className="preview-header">
        <button type="button" className="btn btn-tertiary" onClick={handleBack}>
          フォームに戻る
        </button>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => void handleDraftToNote()}
          disabled={isDrafting || alreadyDrafted}
        >
          {isDrafting ? '保存中...' : alreadyDrafted ? 'note に保存済み' : 'note に一時保存'}
        </button>
      </div>

      {draftError && (
        <div className="draft-error-banner">
          {draftError}
        </div>
      )}

      <ArticlePreview
        title={article.title}
        content={article.content}
        imageUrl={article.imageUrl}
        status={article.status}
      />

      {article.affiliateLinks.length > 0 && (
        <AffiliateLinkList links={article.affiliateLinks} />
      )}
    </div>
  );
}
