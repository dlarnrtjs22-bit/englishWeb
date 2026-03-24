import { Link } from 'react-router-dom';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';

export function MySeriesPage() {
  const { data, error, loading, reload } = useRemoteData(() => contentService.getSeriesList(), []);

  if (loading) {
    return <LoadingPanel message="시리즈 목록을 정리하고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="새로고침"
        message={error ?? '시리즈 정보를 가져오지 못했습니다.'}
        onAction={() => void reload()}
        title="시리즈 목록을 준비할 수 없습니다."
      />
    );
  }

  return (
    <div className="page-stack">
      <section className="hero-panel muted-hero">
        <div>
          <p className="eyebrow inverse">Series Library</p>
          <h3>지금 바로 이어서 학습할 수 있는 시리즈</h3>
          <p className="hero-copy">
            구독 중인 시리즈와 추천 시리즈를 한 화면에서 확인하고, 각 유닛으로 바로 진입할 수
            있도록 구성했습니다.
          </p>
        </div>
      </section>

      <div className="content-list">
        {data.map((series) => (
          <article className="series-detail-row" key={series.id}>
            <img alt={series.title} className="cover-image" src={series.thumbnailUrl} />
            <div className="series-detail-copy">
              <div className="split-line">
                <div>
                  <p className="eyebrow">{series.categoryLabel}</p>
                  <h3>{series.title}</h3>
                </div>
                <span className={`tag${series.isSubscribed ? '' : ' neutral'}`}>
                  {series.isSubscribed ? '구독 중' : '추천'}
                </span>
              </div>
              <p>{series.description}</p>
              <div className="progress-row">
                <div className="progress-track compact">
                  <span className="progress-fill" style={{ width: `${series.progress}%` }} />
                </div>
                <span>{series.progress}%</span>
              </div>
              <div className="split-line">
                <span>{series.packCount}개 유닛</span>
                <Link className="button secondary" to={`/series/${series.id}`}>
                  상세 보기
                </Link>
              </div>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
