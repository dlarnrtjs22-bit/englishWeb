import { startTransition, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    email: 'hong@example.com',
    password: 'password123',
  });

  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/dashboard';

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSubmitting(true);
    setError('');

    try {
      await login(form);
      startTransition(() => navigate(redirectTo, { replace: true }));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '로그인에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-shell">
      <section className="auth-hero">
        <p className="eyebrow">Premium English Learning</p>
        <h1>한국어 직관을 영어 실력으로 바꾸는 학습 흐름</h1>
        <p>
          장황한 설명 대신, 지금 바로 익히고 써먹는 표현 중심 학습. 오늘의 복습과 AI
          피드백이 한 번에 이어집니다.
        </p>
        <div className="auth-hero-card">
          <strong>오늘의 루틴</strong>
          <span>표현 확인 → 직접 입력 → AI 첨삭 → SRS 복습</span>
        </div>
      </section>

      <section className="auth-panel">
        <p className="eyebrow">Login</p>
        <h2>다시 이어서 학습하기</h2>
        <p className="muted">가입하신 이메일로 로그인하면 대시보드로 바로 이동합니다.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            이메일
            <input
              autoComplete="email"
              onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
              type="email"
              value={form.email}
            />
          </label>

          <label>
            비밀번호
            <input
              autoComplete="current-password"
              onChange={(event) =>
                setForm((prev) => ({ ...prev, password: event.target.value }))
              }
              type="password"
              value={form.password}
            />
          </label>

          {error ? <p className="form-error">{error}</p> : null}

          <button className="button primary wide" disabled={isSubmitting} type="submit">
            {isSubmitting ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="auth-footer">
          <span>아직 회원이 아니신가요?</span>
          <Link to="/signup">회원가입</Link>
        </div>
      </section>
    </div>
  );
}
