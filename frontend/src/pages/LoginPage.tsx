import { startTransition, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { useToast } from '../app/ToastContext';
import { isValidEmail } from '../utils/validation';

export function LoginPage() {
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    email: '',
    password: '',
  });

  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/dashboard';

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!isValidEmail(form.email)) {
      showToast('올바른 이메일 형식이 아닙니다.', 'error');
      return;
    }

    if (!form.password.trim()) {
      showToast('비밀번호를 입력해주세요.', 'error');
      return;
    }

    setIsSubmitting(true);

    try {
      await login({
        email: form.email.trim(),
        password: form.password,
      });
      showToast('다시 만나서 반가워요. 학습을 이어서 진행해보세요.', 'success');
      startTransition(() => navigate(redirectTo, { replace: true }));
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '로그인에 실패했습니다.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-shell">
      <section className="auth-hero">
        <p className="eyebrow">English Learning</p>
        <h1>익숙한 표현부터 차근차근 쌓아가는 학습 공간</h1>
        <p>
          오늘의 표현을 확인하고, 직접 써보고, 다시 복습하는 흐름으로 영어를 꾸준히 이어갈 수
          있도록 준비했습니다.
        </p>
        <div className="auth-hero-card">
          <strong>오늘의 추천</strong>
          <span>짧게라도 복습 큐를 확인하면 학습 흐름이 훨씬 안정적으로 유지됩니다.</span>
        </div>
      </section>

      <section className="auth-panel">
        <p className="eyebrow">Login</p>
        <h2>로그인</h2>
        <p className="muted">가입한 이메일과 비밀번호를 입력해주세요.</p>

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
