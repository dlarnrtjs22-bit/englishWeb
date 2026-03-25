import { startTransition, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { useToast } from '../app/ToastContext';
import { isValidEmail } from '../utils/validation';

export function LoginPage() {
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    email: '',
    password: '',
    rememberMe: true,
  });

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
      await login(
        {
          email: form.email.trim(),
          password: form.password,
        },
        form.rememberMe,
      );
      showToast('로그인되었습니다.', 'success');
      startTransition(() => navigate('/dashboard', { replace: true }));
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
        <h1>오늘의 표현부터 차근차근 이어가는 영어 학습</h1>
        <p>짧은 표현 하나라도 직접 써보고 복습까지 이어가면 학습 감각이 훨씬 안정적으로 쌓입니다.</p>
        <div className="auth-hero-card">
          <strong>학습 루틴</strong>
          <span>표현 확인, 직접 입력, 예문 이해, 복습까지 자연스럽게 이어집니다.</span>
        </div>
      </section>

      <section className="auth-panel">
        <p className="eyebrow">Login</p>
        <h2>로그인</h2>
        <p className="muted">이메일과 비밀번호를 입력해주세요.</p>

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
              onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
              placeholder="비밀번호 입력"
              type="password"
              value={form.password}
            />
          </label>

          <div className="remember-row" style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-start' }}>
            <label className="remember-field" style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', margin: 0 }}>
              <input
                checked={form.rememberMe}
                onChange={(event) => setForm((prev) => ({ ...prev, rememberMe: event.target.checked }))}
                type="checkbox"
                style={{ margin: 0 }}
              />
              <span style={{ margin: 0 }}>로그인 상태 유지</span>
            </label>
          </div>

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
