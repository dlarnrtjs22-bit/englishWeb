import { Link } from 'react-router-dom';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';

export function FavoritesPage() {
  const { data, error, loading, reload } = useRemoteData(() => contentService.getFavorites(), []);

  if (loading) {
    return <LoadingPanel message="저장한 표현을 모으고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={error ?? '저장한 표현을 불러오지 못했습니다.'}
        onAction={() => void reload()}
        title="표현 목록을 준비할 수 없습니다."
      />
    );
  }

  return (
    <div className="page-stack">
      <section className="section-header">
        <div>
          <p className="eyebrow">Favorites</p>
          <h3>저장한 표현</h3>
        </div>
        <span>{data.items.length}개 표현</span>
      </section>

      <div className="content-list">
        {data.items.map((item) => (
          <article className="favorite-card" key={item.itemId}>
            <div>
              <p className="eyebrow">{item.seriesTitle}</p>
              <h3>{item.targetText}</h3>
              <p>{item.sourceText}</p>
              <span className="muted">{item.packTitle}</span>
            </div>
            <Link className="button secondary" to={`/learning/${item.itemId}`}>
              다시 학습
            </Link>
          </article>
        ))}
      </div>
    </div>
  );
}
