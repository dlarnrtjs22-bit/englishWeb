import { startTransition, useEffect, useState } from 'react';
import { useAuth } from '../app/AuthContext';
import { useToast } from '../app/ToastContext';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';
import type {
  BillingTransactionResponse,
  MySubscriptionResponse,
  SettingsResponse,
  UpdateSettingsPayload,
} from '../types/models';

interface FormState {
  bio: string;
  dailyGoal: number;
  dailyReminderEnabled: boolean;
  interfaceLanguage: string;
  level: 'advanced' | 'beginner' | 'intermediate';
  name: string;
  newContentEnabled: boolean;
  reviewDueEnabled: boolean;
  targetLanguage: string;
}

const notificationFields = [
  { key: 'dailyReminderEnabled', title: '일일 학습 리마인더' },
  { key: 'newContentEnabled', title: '신규 콘텐츠 알림' },
  { key: 'reviewDueEnabled', title: '복습 큐 알림' },
] as const;

export function SettingsPage() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const settingsQuery = useRemoteData(() => contentService.getSettings(), []);
  const subscriptionQuery = useRemoteData(() => contentService.getMySubscription(), []);
  const billingQuery = useRemoteData(() => contentService.getBillingTransactions(), []);

  const [form, setForm] = useState<FormState | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (!settingsQuery.data) {
      return;
    }

    setForm(toFormState(settingsQuery.data, user?.targetLanguage ?? 'en'));
  }, [settingsQuery.data, user?.targetLanguage]);

  if (settingsQuery.loading || subscriptionQuery.loading || billingQuery.loading || !form) {
    return <LoadingPanel message="설정과 구독 정보를 불러오고 있습니다." />;
  }

  if (settingsQuery.error || subscriptionQuery.error || billingQuery.error) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={settingsQuery.error ?? subscriptionQuery.error ?? billingQuery.error ?? '설정 정보를 불러오지 못했습니다.'}
        onAction={() => {
          void settingsQuery.reload();
          void subscriptionQuery.reload();
          void billingQuery.reload();
        }}
        title="설정을 불러오지 못했습니다."
      />
    );
  }

  const settings = settingsQuery.data as SettingsResponse;
  const subscription = subscriptionQuery.data as MySubscriptionResponse;
  const billing = billingQuery.data as BillingTransactionResponse[];

  const handleSave = async () => {
    setIsSaving(true);

    const payload: UpdateSettingsPayload = {
      notifications: {
        dailyReminderEnabled: form.dailyReminderEnabled,
        newContentEnabled: form.newContentEnabled,
        reviewDueEnabled: form.reviewDueEnabled,
      },
      preference: {
        dailyGoal: form.dailyGoal,
        interfaceLanguage: form.interfaceLanguage,
        level: form.level,
        targetLanguage: form.targetLanguage,
      },
      profile: {
        bio: form.bio,
        name: form.name,
      },
    };

    try {
      const updated = await contentService.updateSettings(payload);
      startTransition(() => {
        setForm(toFormState(updated, form.targetLanguage));
      });
      showToast('설정이 저장되었습니다.', 'success');
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '설정 저장에 실패했습니다.', 'error');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="page-stack">
      <section className="section-header">
        <div>
          <p className="eyebrow">Preferences</p>
          <h3>설정</h3>
        </div>
      </section>

      <section className="content-card">
        <div className="split-line">
          <div>
            <p className="eyebrow">Subscription</p>
            <h3>구독 정보</h3>
          </div>
          <span>{subscription.planName}</span>
        </div>
        <p className="muted">상태: {subscription.status}</p>
        <p className="muted">만료일: {new Date(subscription.currentPeriodEnd).toLocaleDateString('ko-KR')}</p>
        <p className="muted">남은 기간: {subscription.daysRemaining}일</p>
      </section>

      <div className="settings-grid">
        <section className="content-card large-card">
          <div className="split-line">
            <h3>프로필 정보</h3>
          </div>
          <div className="profile-panel">
            <div className="profile-avatar">{form.name.slice(0, 1) || '사'}</div>
            <div className="detail-grid">
              <label className="wide">
                <dt>이름</dt>
                <input
                  onChange={(event) => setForm((prev) => prev ? { ...prev, name: event.target.value } : prev)}
                  value={form.name}
                />
              </label>
              <div>
                <dt>이메일</dt>
                <dd>{settings.profile.email}</dd>
              </div>
              <label className="wide">
                <dt>소개</dt>
                <textarea
                  onChange={(event) => setForm((prev) => prev ? { ...prev, bio: event.target.value } : prev)}
                  rows={4}
                  value={form.bio}
                />
              </label>
            </div>
          </div>
        </section>

        <section className="content-card">
          <h3>학습 설정</h3>
          <div className="level-list">
            {[
              { key: 'beginner', label: 'Beginner', description: '초급' },
              { key: 'intermediate', label: 'Intermediate', description: '중급' },
              { key: 'advanced', label: 'Advanced', description: '고급' },
            ].map((level) => (
              <button
                className={`level-option${form.level === level.key ? ' active' : ''}`}
                key={level.key}
                onClick={() => setForm((prev) => prev ? { ...prev, level: level.key as FormState['level'] } : prev)}
                type="button"
              >
                <span>{level.label}</span>
                <span>{level.description}</span>
              </button>
            ))}
          </div>
          <label className="field-block">
            일일 학습 목표
            <input
              min={1}
              onChange={(event) => setForm((prev) => prev ? { ...prev, dailyGoal: Number(event.target.value) || 1 } : prev)}
              type="number"
              value={form.dailyGoal}
            />
          </label>
          <label className="field-block">
            인터페이스 언어
            <select
              onChange={(event) => setForm((prev) => prev ? { ...prev, interfaceLanguage: event.target.value } : prev)}
              value={form.interfaceLanguage}
            >
              <option value="ko">한국어</option>
              <option value="en">English</option>
            </select>
          </label>
          <label className="field-block">
            목표 언어
            <select
              onChange={(event) => setForm((prev) => prev ? { ...prev, targetLanguage: event.target.value } : prev)}
              value={form.targetLanguage}
            >
              <option value="en">영어</option>
              <option value="ja">일본어</option>
              <option value="es">스페인어</option>
            </select>
          </label>
        </section>

        <section className="content-card">
          <h3>계정 및 보안</h3>
          <div className="settings-list">
            {settings.accountItems.map((item) => (
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
            {notificationFields.map((field) => (
              <article className="settings-row" key={field.key}>
                <div>
                  <strong>{field.title}</strong>
                </div>
                <button
                  aria-pressed={form[field.key]}
                  className={`toggle${form[field.key] ? ' active' : ''}`}
                  onClick={() => setForm((prev) => prev ? { ...prev, [field.key]: !prev[field.key] } : prev)}
                  type="button"
                >
                  <span />
                </button>
              </article>
            ))}
          </div>
        </section>
      </div>

      <section className="content-card">
        <div className="split-line">
          <div>
            <p className="eyebrow">Billing</p>
            <h3>결제 내역</h3>
          </div>
        </div>
        <div className="settings-list">
          {billing.length ? (
            billing.map((item) => (
              <article className="settings-row" key={item.id}>
                <div>
                  <strong>{item.amount} {item.currency}</strong>
                  <p>{item.status} / {item.paidAt ? new Date(item.paidAt).toLocaleDateString('ko-KR') : '결제 대기'}</p>
                </div>
                <span>{item.providerOrderId ?? '-'}</span>
              </article>
            ))
          ) : (
            <p className="muted">아직 결제 내역이 없습니다.</p>
          )}
        </div>
      </section>

      <div className="footer-actions">
        <button className="button primary" disabled={isSaving} onClick={() => void handleSave()} type="button">
          {isSaving ? '저장 중...' : '설정 저장하기'}
        </button>
      </div>
    </div>
  );
}

function toFormState(settings: SettingsResponse, targetLanguage: string): FormState {
  const activeLevel = settings.learningLevels.find((item) => item.active)?.label.toLowerCase() ?? 'intermediate';
  const dailyGoal = Number(settings.dailyGoal.replace(/[^\d]/g, '')) || 20;

  return {
    bio: settings.profile.bio,
    dailyGoal,
    dailyReminderEnabled: settings.notifications[0]?.enabled ?? true,
    interfaceLanguage: settings.accountItems[1]?.description.startsWith('ko') ? 'ko' : 'en',
    level: activeLevel as FormState['level'],
    name: settings.profile.name,
    newContentEnabled: settings.notifications[1]?.enabled ?? true,
    reviewDueEnabled: settings.notifications[2]?.enabled ?? true,
    targetLanguage,
  };
}
