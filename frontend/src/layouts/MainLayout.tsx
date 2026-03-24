import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';

const navigationItems = [
  { icon: 'dashboard', label: '대시보드', path: '/dashboard' },
  { icon: 'subscriptions', label: '나의 학습 시리즈', path: '/my-series' },
  { icon: 'bookmark', label: '저장한 표현', path: '/favorites' },
  { icon: 'rebase_edit', label: '복습 큐', path: '/reviews' },
  { icon: 'settings', label: '설정', path: '/settings' },
];

const pageTitles: Record<string, string> = {
  '/dashboard': '오늘의 학습 흐름',
  '/favorites': '저장한 표현',
  '/my-series': '시리즈 라이브러리',
  '/reviews': '복습 큐',
  '/settings': '설정',
};

export function MainLayout() {
  const { logout, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const title =
    pageTitles[location.pathname] ??
    (location.pathname.startsWith('/series/') ? '시리즈 상세' : '학습 화면');

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <p className="eyebrow">Premium English Learning</p>
          <h1>NativeFlow</h1>
          <p className="muted">한국어 감각으로 익히는 프리미엄 영어 루틴</p>
        </div>

        <nav className="sidebar-nav" aria-label="주요 메뉴">
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
              <strong>{user?.name ?? '게스트'}</strong>
              <p>{user?.membershipLabel ?? 'Premium Member'}</p>
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
            <p className="eyebrow">Academic Curator Workspace</p>
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
            <button className="button ghost" onClick={logout} type="button">
              로그아웃
            </button>
          </div>
        </header>

        <main className="page">
          <Outlet />
        </main>

        <nav className="mobile-nav" aria-label="모바일 메뉴">
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
