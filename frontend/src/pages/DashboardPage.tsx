import { Link } from 'react-router-dom';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';

export function DashboardPage() {
  const { data, error, loading, reload } = useRemoteData(() => contentService.getDashboard(), []);

  if (loading) {
    return <LoadingPanel message="대시보드와 학습 통계를 정리하고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={error ?? '대시보드를 불러오지 못했습니다.'}
        onAction={() => void reload()}
        title="대시보드를 준비할 수 없습니다."
      />
    );
  }

  return (
    <div className="page-stack">
      <section className="hero-panel">
        <div>
          <p className="eyebrow inverse">Daily Motivation</p>
          <h3>
            안녕하세요, {data.userName}님.
            <br />
            오늘 학습을 시작해볼까요?
          </h3>
          <p className="hero-copy">
            한국어의 감각에서 출발해 영어 표현을 직접 쓰고, 복습 큐로 오래 남기는 흐름을
            준비했습니다.
          </p>
        </div>

        <div className="hero-progress-card">
          <div className="split-line">
            <span>오늘의 목표</span>
            <strong>{data.progressPercent}%</strong>
          </div>
          <div className="progress-track">
            <span className="progress-fill" style={{ width: `${data.progressPercent}%` }} />
          </div>
          <p>{data.progressMessage}</p>
        </div>
      </section>

      <div className="dashboard-grid">
        <div className="page-stack">
          <section className="section-header">
            <div>
              <p className="eyebrow">Current Series</p>
              <h3>학습 중인 시리즈</h3>
            </div>
            <Link to="/my-series">전체 보기</Link>
          </section>

          <div className="card-grid two">
            {data.activeSeries.map((series) => (
              <Link className="content-card series-card" key={series.id} to={`/series/${series.id}`}>
                <img alt={series.title} className="card-thumb" src={series.thumbnailUrl} />
                <div className="card-copy">
                  <strong>{series.title}</strong>
                  <p>{series.subtitle}</p>
                  <div className="progress-row">
                    <div className="progress-track compact">
                      <span className="progress-fill" style={{ width: `${series.progress}%` }} />
                    </div>
                    <span>{series.progress}%</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>

          <section className="section-header">
            <div>
              <p className="eyebrow">Recommendations</p>
              <h3>추천 시리즈</h3>
            </div>
          </section>

          <div className="recommendation-grid">
            {data.recommendedSeries.map((series, index) => (
              <Link
                className={`recommendation-card${index === 0 ? ' feature' : ''}`}
                key={series.id}
                to={`/series/${series.id}`}
              >
                <img alt={series.title} src={series.thumbnailUrl} />
                <div className="recommendation-overlay">
                  {series.badge ? <span className="tag warm">{series.badge}</span> : null}
                  <strong>{series.title}</strong>
                  <p>{series.description}</p>
                </div>
              </Link>
            ))}
          </div>
        </div>

        <aside className="sidebar-column">
          <section className="content-card review-card">
            <div className="icon-badge warm">
              <span className="material-symbols-outlined">rebase_edit</span>
            </div>
            <h3>오늘의 복습</h3>
            <strong className="display-number">{data.reviewSummary.dueCount}</strong>
            <p>{data.reviewSummary.description}</p>
            <ul className="meta-list">
              {data.reviewSummary.priorityLabels.map((label) => (
                <li key={label}>{label}</li>
              ))}
            </ul>
            <Link className="button accent wide" to="/reviews">
              복습 시작
            </Link>
          </section>

          <div className="card-grid two compact-stats">
            {data.stats.map((stat) => (
              <article className="content-card centered" key={stat.label}>
                <p className="eyebrow">{stat.label}</p>
                <strong>{stat.value}</strong>
              </article>
            ))}
          </div>
        </aside>
      </div>
    </div>
  );
}
