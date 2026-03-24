import { startTransition, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { isStrongPassword, isValidEmail } from '../utils/validation';

export function SignupPage() {
  const { signup } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    email: '',
    name: '',
    password: '',
    targetLanguage: 'en',
  });

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');

    if (!form.name.trim()) {
      setError('이름을 입력해주세요.');
      return;
    }

    if (!isValidEmail(form.email)) {
      setError('올바른 이메일 형식이 아닙니다.');
      return;
    }

    if (!isStrongPassword(form.password)) {
      setError('비밀번호는 8자 이상이며 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.');
      return;
    }

    setIsSubmitting(true);

    try {
      await signup({
        email: form.email.trim(),
        name: form.name.trim(),
        password: form.password,
        targetLanguage: form.targetLanguage,
      });
      startTransition(() => navigate('/dashboard', { replace: true }));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '회원가입에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-shell reverse">
      <section className="auth-panel">
        <p className="eyebrow">Create Account</p>
        <h2>회원가입</h2>
        <p className="muted">가입 즉시 월 구독 기준 계정 구조로 연결되도록 준비합니다.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            이름
            <input
              onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
              placeholder="홍길동"
              type="text"
              value={form.name}
            />
          </label>

          <label>
            이메일
            <input
              onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
              placeholder="you@example.com"
              type="email"
              value={form.email}
            />
          </label>

          <label>
            비밀번호
            <input
              onChange={(event) =>
                setForm((prev) => ({ ...prev, password: event.target.value }))
              }
              placeholder="예: Password1!"
              type="password"
              value={form.password}
            />
          </label>

          <label>
            목표 언어
            <select
              onChange={(event) =>
                setForm((prev) => ({ ...prev, targetLanguage: event.target.value }))
              }
              value={form.targetLanguage}
            >
              <option value="en">영어</option>
              <option value="ja">일본어</option>
              <option value="es">스페인어</option>
            </select>
          </label>

          {error ? <p className="form-error">{error}</p> : null}

          <button className="button primary wide" disabled={isSubmitting} type="submit">
            {isSubmitting ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <div className="auth-footer">
          <span>이미 계정이 있으신가요?</span>
          <Link to="/login">로그인</Link>
        </div>
      </section>

      <section className="auth-hero">
        <p className="eyebrow">NativeFlow</p>
        <h1>회원가입 후 구독 상태와 남은 기간을 기준으로 접근 권한을 관리합니다.</h1>
        <p>
          월 구독 구조를 기준으로 인증, 세션, 구독 기간 정보가 함께 움직이도록 설계했습니다.
        </p>
        <div className="stat-strip">
          <article>
            <strong>JWT</strong>
            <span>액세스 토큰</span>
          </article>
          <article>
            <strong>Session</strong>
            <span>DB 세션 관리</span>
          </article>
          <article>
            <strong>Monthly</strong>
            <span>월 구독 기준</span>
          </article>
        </div>
      </section>
    </div>
  );
}
