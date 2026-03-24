import { startTransition, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { isValidEmail } from '../utils/validation';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    email: '',
    password: '',
  });

  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/dashboard';

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');

    if (!isValidEmail(form.email)) {
      setError('올바른 이메일 형식이 아닙니다.');
      return;
    }

    if (!form.password.trim()) {
      setError('비밀번호를 입력해주세요.');
      return;
    }

    setIsSubmitting(true);

    try {
      await login({
        email: form.email.trim(),
        password: form.password,
      });
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
        <h1>구독 중인 계정만 로그인해서 학습을 이어갑니다.</h1>
        <p>
          로그인 후에는 시리즈 구독 상태와 학습 기록을 기준으로 대시보드, 복습 큐,
          설정 화면까지 이어집니다.
        </p>
        <div className="auth-hero-card">
          <strong>로그인 규칙</strong>
          <span>활성 구독이 있거나 체험 구독 기간 안에 있는 계정만 접속 가능합니다.</span>
        </div>
      </section>

      <section className="auth-panel">
        <p className="eyebrow">Login</p>
        <h2>로그인</h2>
        <p className="muted">가입한 이메일과 비밀번호로 로그인하세요.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            이메일
            <input
              autoComplete="email"
              onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
              placeholder="you@example.com"
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
              placeholder="비밀번호 입력"
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
