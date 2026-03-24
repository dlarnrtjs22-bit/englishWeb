import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';

export function SettingsPage() {
  const { data, error, loading, reload } = useRemoteData(() => contentService.getSettings(), []);

  if (loading) {
    return <LoadingPanel message="설정 화면을 준비하고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={error ?? '설정 정보를 가져오지 못했습니다.'}
        onAction={() => void reload()}
        title="설정을 불러오지 못했습니다."
      />
    );
  }

  return (
    <div className="page-stack">
      <section className="section-header">
        <div>
          <p className="eyebrow">Preferences</p>
          <h3>설정</h3>
        </div>
      </section>

      <div className="settings-grid">
        <section className="content-card large-card">
          <div className="split-line">
            <h3>프로필 정보</h3>
            <button className="button ghost" type="button">
              편집하기
            </button>
          </div>
          <div className="profile-panel">
            <div className="profile-avatar">홍</div>
            <dl className="detail-grid">
              <div>
                <dt>이름</dt>
                <dd>{data.profile.name}</dd>
              </div>
              <div>
                <dt>이메일</dt>
                <dd>{data.profile.email}</dd>
              </div>
              <div className="wide">
                <dt>소개</dt>
                <dd>{data.profile.bio}</dd>
              </div>
            </dl>
          </div>
        </section>

        <section className="content-card">
          <h3>학습 설정</h3>
          <div className="level-list">
            {data.learningLevels.map((level) => (
              <button className={`level-option${level.active ? ' active' : ''}`} key={level.label} type="button">
                <span>{level.label}</span>
                <span>{level.description}</span>
              </button>
            ))}
          </div>
          <div className="goal-box">
            <div className="split-line">
              <span>일일 학습 목표</span>
              <strong>{data.dailyGoal}</strong>
            </div>
            <div className="progress-track">
              <span className="progress-fill" style={{ width: '60%' }} />
            </div>
          </div>
        </section>

        <section className="content-card">
          <h3>계정 및 보안</h3>
          <div className="settings-list">
            {data.accountItems.map((item) => (
              <article className="settings-row" key={item.title}>
                <div>
                  <strong>{item.title}</strong>
                  <p>{item.description}</p>
                </div>
                <button className="button secondary" type="button">
                  {item.actionLabel}
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="content-card">
          <h3>알림 설정</h3>
          <div className="settings-list">
            {data.notifications.map((notification) => (
              <article className="settings-row" key={notification.title}>
                <div>
                  <strong>{notification.title}</strong>
                  <p>{notification.description}</p>
                </div>
                <button
                  aria-pressed={notification.enabled}
                  className={`toggle${notification.enabled ? ' active' : ''}`}
                  type="button"
                >
                  <span />
                </button>
              </article>
            ))}
          </div>
        </section>
      </div>

      <div className="footer-actions">
        <button className="button ghost" type="button">
          변경사항 취소
        </button>
        <button className="button primary" type="button">
          설정 저장하기
        </button>
      </div>
    </div>
  );
}
