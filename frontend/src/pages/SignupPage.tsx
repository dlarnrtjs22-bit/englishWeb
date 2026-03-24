import { startTransition, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';

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
    setIsSubmitting(true);
    setError('');

    try {
      await signup(form);
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
        <h2>프리미엄 학습 공간 시작하기</h2>
        <p className="muted">
          지금은 DB 없이 목업으로 연결되어 있지만, 실제 가입 플로우로 이어질 수 있게 구조를
          잡아두었습니다.
        </p>

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
              placeholder="8자 이상 입력"
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
        <h1>시리즈를 구독하고, 복습 큐로 계속 기억하게 만듭니다.</h1>
        <p>
          지금 초안에서는 한국어 기반 UX, 학습 카드, 복습 흐름, 설정 화면까지 한 번에 확인할 수
          있습니다.
        </p>
        <div className="stat-strip">
          <article>
            <strong>24</strong>
            <span>오늘 복습 예정</span>
          </article>
          <article>
            <strong>12일</strong>
            <span>연속 학습</span>
          </article>
          <article>
            <strong>1,204</strong>
            <span>누적 표현</span>
          </article>
        </div>
      </section>
    </div>
  );
}
