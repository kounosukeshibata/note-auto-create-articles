import type { AffiliateLinkDto } from '../types';

interface AffiliateLinkListProps {
  links: AffiliateLinkDto[];
}

const PLATFORM_LABELS = {
  AMAZON: 'Amazon',
} as const;

export function AffiliateLinkList({ links }: AffiliateLinkListProps) {
  return (
    <section className="affiliate-link-list">
      <h2 className="affiliate-list-title">アフィリエイトリンク一覧</h2>
      <ul className="affiliate-list">
        {links.map((link) => (
          <li key={link.trackingId} className="affiliate-item">
            <div className="affiliate-info">
              <span className="affiliate-platform">{PLATFORM_LABELS[link.platform]}</span>
              <span className="affiliate-product-name">{link.productName}</span>
              <span className="affiliate-price">
                {link.price.toLocaleString('ja-JP')}円
              </span>
            </div>
            <a
              href={link.url}
              target="_blank"
              rel="noopener noreferrer"
              className="btn btn-affiliate"
            >
              リンクを確認
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}
