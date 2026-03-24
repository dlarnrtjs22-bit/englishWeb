import { Link } from 'react-router-dom';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';

export function ReviewQueuePage() {
  const { data, error, loading, reload } = useRemoteData(
    () => contentService.getReviewQueue(),
    [],
  );

  if (loading) {
    return <LoadingPanel message="복습 큐를 계산하고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={error ?? '복습 큐를 불러오지 못했습니다.'}
        onAction={() => void reload()}
        title="복습 큐를 준비할 수 없습니다."
      />
    );
  }

  return (
    <div className="page-stack">
      <section className="section-header">
        <div>
          <p className="eyebrow">Review Queue</p>
          <h3>오늘의 복습 큐</h3>
        </div>
        <Link className="button primary" to={`/learning/${data.items[0]?.itemId ?? 'item-doze-off'}`}>
          모두 복습하기
        </Link>
      </section>

      <div className="card-grid three">
        {data.summaryCards.map((card) => (
          <article className={`summary-card ${card.variant}`} key={card.label}>
            <div className="split-line">
              <span>{card.label}</span>
              <span className="material-symbols-outlined">{card.icon}</span>
            </div>
            <strong>{card.value}</strong>
            <p>{card.caption}</p>
          </article>
        ))}
      </div>

      <div className="dashboard-grid narrow-right">
        <section className="content-card list-card">
          <div className="section-header">
            <div>
              <p className="eyebrow">Series Queue</p>
              <h3>시리즈별 복습 목록</h3>
            </div>
          </div>

          <div className="queue-groups">
            {data.groups.map((group) => (
              <article className="queue-group" key={group.seriesId}>
                <header className="queue-group-header">
                  <div>
                    <strong>{group.seriesTitle}</strong>
                    <p>{group.description}</p>
                  </div>
                  <button className="button secondary" type="button">
                    복습
                  </button>
                </header>

                <div className="queue-items">
                  {group.items.map((item) => (
                    <Link className="queue-item" key={item.itemId} to={`/learning/${item.itemId}`}>
                      <div>
                        <strong>{item.sourceText}</strong>
                        <p>{item.contextText}</p>
                      </div>
                      <span className="tag warm">Level {item.level}</span>
                    </Link>
                  ))}
                </div>
              </article>
            ))}
          </div>
        </section>

        <aside className="sidebar-column">
          <section className="content-card">
            <h3>복습 히스토리</h3>
            <div className="chart-bars">
              {data.weeklyHistory.map((value, index) => (
                <div className="chart-bar-wrap" key={`${value}-${index}`}>
                  <span
                    className={`chart-bar${index === data.weeklyHistory.length - 1 ? ' active' : ''}`}
                    style={{ height: `${value}%` }}
                  />
                </div>
              ))}
            </div>
            <div className="chart-labels">
              <span>월</span>
              <span>화</span>
              <span>수</span>
              <span>목</span>
              <span>금</span>
              <span>토</span>
              <span>오늘</span>
            </div>
          </section>

          <section className="content-card tip-card">
            <h3>학습 팁</h3>
            <p>
              간격 반복은 조금씩, 자주 만나는 것이 핵심입니다. 오늘 복습이 길어지더라도 먼저
              overdue 항목부터 처리하도록 흐름을 잡았습니다.
            </p>
            <button className="button ghost wide" type="button">
              자세히 보기
            </button>
          </section>
        </aside>
      </div>
    </div>
  );
}
