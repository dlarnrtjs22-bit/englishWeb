import { startTransition, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { useToast } from '../app/ToastContext';
import { isStrongPassword, isValidEmail } from '../utils/validation';

export function SignupPage() {
  const { signup } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    email: '',
    name: '',
    password: '',
    targetLanguage: 'en',
  });

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!form.name.trim()) {
      showToast('이름을 입력해주세요.', 'error');
      return;
    }

    if (!isValidEmail(form.email)) {
      showToast('올바른 이메일 형식이 아닙니다.', 'error');
      return;
    }

    if (!isStrongPassword(form.password)) {
      showToast('비밀번호는 8자 이상이며 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.', 'error');
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
      showToast('회원가입이 완료되었습니다. 바로 학습을 시작해보세요.', 'success');
      startTransition(() => navigate('/dashboard', { replace: true }));
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '회원가입에 실패했습니다.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-shell reverse">
      <section className="auth-panel">
        <p className="eyebrow">Create Account</p>
        <h2>회원가입</h2>
        <p className="muted">학습 기록을 쌓아갈 계정을 만들어보세요.</p>

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
        <h1>표현을 보고, 직접 써보고, 반복해서 익히는 학습 루틴</h1>
        <p>
          처음부터 무겁게 시작하지 않아도 괜찮습니다. 짧은 표현 하나씩 쌓아가며 자연스럽게
          흐름을 만들 수 있도록 구성했습니다.
        </p>
        <div className="stat-strip">
          <article>
            <strong>Series</strong>
            <span>상황별 학습</span>
          </article>
          <article>
            <strong>Review</strong>
            <span>반복 복습</span>
          </article>
          <article>
            <strong>Practice</strong>
            <span>직접 써보기</span>
          </article>
        </div>
      </section>
    </div>
  );
}
