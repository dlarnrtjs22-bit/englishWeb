import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';

const navigationItems = [
  { icon: 'dashboard', label: '대시보드', path: '/dashboard' },
  { icon: 'subscriptions', label: '나의 학습 시리즈', path: '/my-series' },
  { icon: 'bookmark', label: '저장한 표현', path: '/favorites' },
  { icon: 'rebase_edit', label: '복습 큐', path: '/reviews' },
  { icon: 'edit_square', label: '나의 영어 일기', path: '/diary' },
  { icon: 'settings', label: '설정', path: '/settings' },
];

const pageTitles: Record<string, string> = {
  '/dashboard': '오늘의 학습 흐름',
  '/favorites': '저장한 표현',
  '/my-series': '시리즈 라이브러리',
  '/reviews': '복습 큐',
  '/diary': '나의 영어 일기',
  '/settings': '설정',
};

export function MainLayout() {
  const { logout, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const isLearningRoute = location.pathname.startsWith('/learning/');
  const title =
    pageTitles[location.pathname] ??
    (location.pathname.startsWith('/series/') ? '시리즈 상세' : isLearningRoute ? '학습 화면' : '학습');

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <p className="eyebrow">English Learning</p>
          <h1>NativeFlow</h1>
          <p className="muted">표현을 익히고, 직접 써보고, 다시 복습하는 학습 흐름</p>
        </div>

        <nav aria-label="주요 메뉴" className="sidebar-nav">
          {navigationItems.map((item) => (
            <NavLink
              className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
              key={item.path}
              to={item.path}
            >
              <span aria-hidden="true" className="material-symbols-outlined">
                {item.icon}
              </span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="profile-chip">
            <div className="avatar-badge">{user?.name.slice(0, 1) ?? 'N'}</div>
            <div>
              <strong>{user?.name ?? '사용자'}</strong>
              <p>{user?.membershipLabel ?? 'Standard Monthly'}</p>
            </div>
          </div>
          <button className="button primary wide" onClick={() => navigate('/reviews')} type="button">
            오늘의 학습 시작
          </button>
        </div>
      </aside>

      <div className="shell-main">
        <header className="topbar">
          <div>
            <p className="eyebrow">Learning Workspace</p>
            <h2>{title}</h2>
          </div>

          <div className="topbar-actions">
            <button className="icon-button" type="button">
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <button className="icon-button" type="button">
              <span className="material-symbols-outlined">history</span>
            </button>
            <button className="profile-button" onClick={() => navigate('/settings')} type="button">
              <span className="material-symbols-outlined">account_circle</span>
              <span>{user?.name ?? '사용자'}</span>
            </button>
            <button className="button ghost" onClick={() => void logout()} type="button">
              로그아웃
            </button>
          </div>
        </header>

        <main className={`page${isLearningRoute ? ' page-learning' : ''}`}>
          <Outlet />
        </main>

        <nav aria-label="모바일 메뉴" className="mobile-nav">
          {navigationItems.map((item) => (
            <NavLink
              className={({ isActive }) => `mobile-nav-item${isActive ? ' active' : ''}`}
              key={item.path}
              to={item.path}
            >
              <span className="material-symbols-outlined">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>
    </div>
  );
}
