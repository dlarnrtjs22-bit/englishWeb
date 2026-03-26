import { Link, useNavigate, useParams } from 'react-router-dom';
import { useToast } from '../app/ToastContext';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';
import { createRandomLearningSession } from '../utils/randomLearningSession';

export function SeriesDetailPage() {
  const { seriesId = '' } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { data, error, loading, reload } = useRemoteData(
    () => contentService.getSeriesDetail(seriesId),
    [seriesId],
  );

  const handleRandomLearning = async (packId: string) => {
    try {
      const response = await contentService.getRandomPackQueue(packId);

      if (!response.itemIds.length) {
        showToast('랜덤 학습에 사용할 단어가 없습니다.', 'info');
        return;
      }

      const sessionId = createRandomLearningSession({
        itemIds: response.itemIds,
        packId,
        seriesId: data?.id ?? seriesId,
      });

      navigate(`/learning/${response.itemIds[0]}?mode=random&seriesId=${data?.id ?? seriesId}&packId=${packId}&step=1&randomSession=${sessionId}`);
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '랜덤 학습을 시작하지 못했습니다.', 'error');
    }
  };

  if (loading) {
    return <LoadingPanel message="시리즈 세부 정보를 준비하고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={error ?? '시리즈를 찾을 수 없습니다.'}
        onAction={() => void reload()}
        title="시리즈 상세를 불러오지 못했습니다."
      />
    );
  }

  return (
    <div className="page-stack">
      <section className="series-hero">
        <div className="series-hero-copy">
          <p className="eyebrow">{data.categoryLabel}</p>
          <h3>{data.title}</h3>
          <p>{data.description}</p>
        </div>
        <img alt={data.title} className="series-hero-image" src={data.thumbnailUrl} />
      </section>

      <div className="dashboard-grid narrow-right">
        <section className="page-stack">
          <div className="section-header">
            <div>
              <p className="eyebrow">Curriculum</p>
              <h3>학습 커리큘럼</h3>
            </div>
            <span>{data.packs.length}개 유닛</span>
          </div>

          {data.packs.map((pack) => (
            <article className={`pack-card${pack.locked ? ' locked' : ''}`} key={pack.id}>
              <div className={`icon-badge${pack.completed ? ' mint' : pack.locked ? '' : ' indigo'}`}>
                <span className="material-symbols-outlined">
                  {pack.completed ? 'check_circle' : pack.locked ? 'lock' : 'play_circle'}
                </span>
              </div>
              <div className="pack-copy">
                <div className="split-line">
                  <p className="eyebrow">{pack.unitLabel}</p>
                  <span>{pack.statusLabel}</span>
                </div>
                <h4>{pack.title}</h4>
                <p>{pack.description}</p>
              </div>
              <div className="pack-meta">
                <span>{pack.itemCount} Items</span>
                <div className="progress-track compact">
                  <span className="progress-fill" style={{ width: `${pack.progress}%` }} />
                </div>
                {!pack.locked && (pack.firstItemId || pack.completed) ? (
                  pack.completed ? (
                    <button className="button secondary" onClick={() => void handleRandomLearning(pack.id)} type="button">
                      랜덤 학습
                    </button>
                  ) : (
                    <Link className="button secondary" to={`/learning/${pack.firstItemId}?mode=study&seriesId=${data.id}`}>
                      학습 시작
                    </Link>
                  )
                ) : null}
              </div>
            </article>
          ))}

          <section className="membership-banner">
            <div>
              <h3>모든 시리즈를 무제한으로</h3>
              <p>
                실제 결제는 아직 없지만, 이후 프리미엄 구독 페이지로 연결할 수 있도록 CTA 영역을
                먼저 잡아두었습니다.
              </p>
            </div>
            <button className="button accent" type="button">
              프리미엄 멤버십 알아보기
            </button>
          </section>
        </section>

        <aside className="sidebar-column">
          <section className="content-card">
            <h3>시리즈 정보</h3>
            <div className="tag-row">
              {data.tags.map((tag) => (
                <span className="tag neutral" key={tag}>
                  {tag}
                </span>
              ))}
            </div>
            <dl className="detail-grid">
              <div>
                <dt>강사</dt>
                <dd>{data.instructor}</dd>
              </div>
              <div>
                <dt>레벨</dt>
                <dd>{data.levelLabel}</dd>
              </div>
              <div>
                <dt>업데이트</dt>
                <dd>{data.updatedAt}</dd>
              </div>
            </dl>
          </section>

          <section className="content-card note-card">
            <div className="icon-badge warm">
              <span className="material-symbols-outlined">star</span>
            </div>
            <h3>학습 요약 AI 노트</h3>
            <p>{data.coachNote}</p>
            <button className="button ghost wide" type="button">
              PDF 가이드 다운로드
            </button>
          </section>

          <section className="content-card centered">
            <div className="circle-progress" style={{ ['--degree' as any]: `${data.progress * 3.6}deg` }}>
              <strong>{data.progress}%</strong>
            </div>
            <h3>현재 진도</h3>
            <p>{data.progressMessage}</p>
          </section>
        </aside>
      </div>
    </div>
  );
}
