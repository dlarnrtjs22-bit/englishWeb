import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useToast } from '../app/ToastContext';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';
import type { CheckAnswerResponse, ReviewResult, SentenceFeedbackResponse } from '../types/models';
import { clearRandomLearningSession, readRandomLearningSession } from '../utils/randomLearningSession';
import {
  clearReviewLearningSession,
  readReviewLearningSession,
  writeReviewLearningSession,
} from '../utils/reviewLearningSession';
import {
  clearStudyLearningSession,
  readStudyLearningSession,
  writeStudyLearningSession,
} from '../utils/studyLearningSession';

const reviewOptions: Array<{ label: string; result: ReviewResult; subtitle: string }> = [
  { label: '1분 후', result: 'minute', subtitle: '1분' },
  { label: '다시', result: 'again', subtitle: '1일' },
  { label: '어려움', result: 'hard', subtitle: '2일' },
  { label: '좋음', result: 'good', subtitle: '4일' },
  { label: '쉬움', result: 'easy', subtitle: '7일' },
  { label: '1달', result: 'month', subtitle: '30일' },
  { label: '1년', result: 'year', subtitle: '365일' },
  { label: '완료(복습)', result: 'complete', subtitle: '복습으로 이동' },
];

export function LearningPage() {
  const { itemId = '' } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { showToast } = useToast();
  const mode = searchParams.get('mode') ?? 'study';
  const seriesId = searchParams.get('seriesId');
  const currentStep = Number(searchParams.get('step') ?? '1');
  const randomSessionId = searchParams.get('randomSession');
  const reviewSessionId = searchParams.get('reviewSession');
  const studySessionId = searchParams.get('studySession');
  const { data, error, loading, reload } = useRemoteData(() => contentService.getLearningItem(itemId, mode), [itemId, mode]);
  const [answer, setAnswer] = useState('');
  const [answerResult, setAnswerResult] = useState<CheckAnswerResponse | null>(null);
  const [sentence, setSentence] = useState('');
  const [sentenceFeedback, setSentenceFeedback] = useState<SentenceFeedbackResponse | null>(null);
  const [sentenceFeedbackLoading, setSentenceFeedbackLoading] = useState(false);
  const [favorite, setFavorite] = useState(false);
  const [reviewResult, setReviewResult] = useState<ReviewResult | null>(null);

  useEffect(() => {
    setAnswer('');
    setAnswerResult(null);
    setSentence('');
    setSentenceFeedback(null);
    setSentenceFeedbackLoading(false);
    setFavorite(false);
    setReviewResult(null);
    window.speechSynthesis.cancel();
  }, [itemId]);

  if (loading) {
    return <LoadingPanel message="학습 카드를 준비하고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={error ?? '학습 카드를 찾을 수 없습니다.'}
        onAction={() => void reload()}
        title="학습 화면을 불러오지 못했습니다."
      />
    );
  }

  const answerCard = answerResult
    ? {
        sentence: answerResult.exampleSentence,
        text: answerResult.correctAnswer,
        translation: answerResult.exampleTranslation,
      }
    : null;
  const randomSession = mode === 'random' && randomSessionId ? readRandomLearningSession(randomSessionId) : null;
  const reviewSession = mode === 'review' && reviewSessionId ? readReviewLearningSession(reviewSessionId) : null;
  const studySession = mode === 'study' && studySessionId ? readStudyLearningSession(studySessionId) : null;
  const studyRemaining = studySession ? studySession.itemIds.length : data.progress.total;
  const studyCompleted = studySession ? Math.max(0, studySession.initialCount - studySession.itemIds.length) : 0;
  const reviewRemaining = reviewSession ? reviewSession.itemIds.length : data.progress.total;
  const reviewCompleted = reviewSession ? Math.max(0, reviewSession.initialCount - reviewSession.itemIds.length) : 0;
  const randomCurrent = ((Math.max(currentStep, 1) - 1) % Math.max(data.progress.total, 1)) + 1;
  const randomTotal = randomSession ? randomSession.itemIds.length : data.progress.total;
  const progressNumerator = mode === 'study'
    ? studyCompleted
    : mode === 'review'
      ? reviewCompleted
      : randomCurrent;
  const progressDenominator = mode === 'study'
    ? Math.max(studyCompleted + studyRemaining, 1)
    : mode === 'review'
      ? Math.max(reviewCompleted + reviewRemaining, 1)
      : Math.max(randomTotal, 1);
  const sentenceFeedbackText = sentenceFeedbackLoading
    ? 'AI가 문장을 확인하고 있습니다.'
    : sentenceFeedback
      ? [
          sentenceFeedback.headline,
          sentenceFeedback.message,
          !sentenceFeedback.perfect && sentenceFeedback.correctedSentence
            ? `추천 문장\n${sentenceFeedback.correctedSentence}`
            : '',
          sentenceFeedback.tips.length > 0
            ? `팁\n${sentenceFeedback.tips.map((tip) => `- ${tip}`).join('\n')}`
            : '',
        ]
          .filter(Boolean)
          .join('\n\n')
      : '문장을 직접 만들어보며 표현을 익혀보세요.';

  const speak = (text: string) => {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 0.95;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  };

  const handleCheckAnswer = async () => {
    try {
      const response = await contentService.checkAnswer(itemId, answer, mode);
      setAnswerResult(response);
      speak(response.correctAnswer);
      showToast(response.isCorrect ? '정답입니다.' : '정답을 확인해보세요.', response.isCorrect ? 'success' : 'error');
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '정답 확인에 실패했습니다.', 'error');
    }
  };

  const handleSentenceFeedback = async () => {
    if (!sentence.trim()) {
      showToast('문장을 먼저 입력해주세요.', 'error');
      return;
    }

    try {
      setSentenceFeedbackLoading(true);
      const response = await contentService.getSentenceFeedback(itemId, sentence, mode);
      setSentenceFeedback(response);
      showToast(response.perfect ? '문장이 아주 자연스러워요.' : 'AI가 문장을 다듬어드렸어요.', 'success');
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : 'AI 문장 피드백에 실패했습니다.', 'error');
    } finally {
      setSentenceFeedbackLoading(false);
    }
  };

  const handleFavorite = async () => {
    try {
      const response = favorite
        ? await contentService.unfavoriteItem(itemId)
        : await contentService.favoriteItem(itemId);
      setFavorite(response.isFavorited);
      showToast(response.isFavorited ? '저장한 표현에 추가했습니다.' : '저장한 표현에서 제거했습니다.', 'success');
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '표현 저장 처리에 실패했습니다.', 'error');
    }
  };

  const handleReview = async (result: ReviewResult) => {
    if (!answerResult) {
      return;
    }

    if (mode === 'study' && studySession && studySessionId && result === 'minute') {
      const updatedItemIds = [...studySession.itemIds];
      const currentItemId = updatedItemIds.shift();

      if (currentItemId) {
        updatedItemIds.push(currentItemId);
      }

      writeStudyLearningSession(studySessionId, { ...studySession, itemIds: updatedItemIds });
      const nextItemId = updatedItemIds[0];

      showToast('1분 후 다시 볼 단어로 보냈습니다.', 'success');

      if (!nextItemId) {
        clearStudyLearningSession(studySessionId);
        navigate(seriesId ? `/series/${seriesId}` : '/my-series', { replace: true });
        return;
      }

      navigate(`/learning/${nextItemId}?mode=study&seriesId=${seriesId ?? studySession.seriesId}&studySession=${studySessionId}&step=1`, { replace: true });
      return;
    }

    try {
      const response = await contentService.submitReview(itemId, result, mode);
      setReviewResult(result);
      showToast(
        mode === 'random'
          ? '다음 랜덤 단어로 넘어갑니다.'
          : mode === 'review'
          ? result === 'review_done'
            ? '오늘 복습 완료로 처리했습니다.'
            : '현재 복습 세션 뒤로 다시 넣었습니다.'
          : result === 'complete'
          ? '완료 단어로 저장하고 복습 대상으로 등록했습니다.'
          : `복습 결과를 저장했습니다. 다음 복습은 ${response.nextReviewAt ? new Date(response.nextReviewAt).toLocaleString('ko-KR') : '-'} 입니다.`,
        'success',
      );

      if (mode === 'review') {
        if (!reviewSession || !reviewSessionId) {
          navigate('/reviews', { replace: true });
          return;
        }

        const currentIndex = Math.max(currentStep, 1) - 1;
        const updatedItemIds = [...reviewSession.itemIds];
        const [currentItemId] = updatedItemIds.splice(currentIndex, 1);

        if (result === 'repeat') {
          if (currentItemId) {
            updatedItemIds.push(currentItemId);
          }
          writeReviewLearningSession(reviewSessionId, { ...reviewSession, itemIds: updatedItemIds });
          const nextItemId = updatedItemIds[Math.min(currentIndex, updatedItemIds.length - 1)];
          if (!nextItemId) {
            clearReviewLearningSession(reviewSessionId);
            navigate('/reviews', { replace: true });
            return;
          }
          navigate(`/learning/${nextItemId}?mode=review&reviewSession=${reviewSessionId}&step=${Math.min(currentIndex + 2, updatedItemIds.length)}`, { replace: true });
          return;
        }

        writeReviewLearningSession(reviewSessionId, { ...reviewSession, itemIds: updatedItemIds });
        const nextItemId = updatedItemIds[currentIndex];
        if (!nextItemId) {
          clearReviewLearningSession(reviewSessionId);
          navigate('/reviews', { replace: true });
          return;
        }
        navigate(`/learning/${nextItemId}?mode=review&reviewSession=${reviewSessionId}&step=${Math.min(currentIndex + 1, updatedItemIds.length)}`, { replace: true });
        return;
      }

      if (mode === 'study' && studySession && studySessionId) {
        const updatedItemIds = [...studySession.itemIds];
        updatedItemIds.shift();

        writeStudyLearningSession(studySessionId, { ...studySession, itemIds: updatedItemIds });
        const nextItemId = updatedItemIds[0];
        if (!nextItemId) {
          clearStudyLearningSession(studySessionId);
          showToast('현재 유닛 학습을 완료했습니다.', 'success');
          navigate(seriesId ? `/series/${seriesId}` : '/my-series', { replace: true });
          return;
        }
        navigate(`/learning/${nextItemId}?mode=study&seriesId=${seriesId ?? studySession.seriesId}&studySession=${studySessionId}&step=1`, { replace: true });
        return;
      }

      if (response.nextItemId) {
        const nextParams = new URLSearchParams({ mode });
        if (seriesId) {
          nextParams.set('seriesId', seriesId);
        }
        if (mode === 'random') {
          nextParams.set('step', String(Math.max(currentStep, 1) + 1));
        }
        navigate(`/learning/${response.nextItemId}?${nextParams.toString()}`, { replace: true });
        return;
      }

      if (mode === 'random') {
        if (randomSessionId) {
          clearRandomLearningSession(randomSessionId);
        }
        navigate(seriesId ? `/series/${seriesId}` : '/my-series', { replace: true });
        return;
      }

      if (mode === 'study') {
        showToast('현재 유닛 학습을 완료했습니다.', 'success');
        navigate(seriesId ? `/series/${seriesId}` : '/my-series', { replace: true });
        return;
      }

      if (mode === 'favorites') {
        navigate('/favorites', { replace: true });
        return;
      }

      navigate('/reviews', { replace: true });
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '복습 저장에 실패했습니다.', 'error');
    }
  };

  return (
    <div className="learning-workspace mobile-fit-learning">
      <header className="learning-topbar compact">
        <div className="learning-topbar-copy">
          <p className="eyebrow">Meaning &amp; Context</p>
          <h3>핵심 표현 학습</h3>
        </div>
        <div className="learning-progress">
          {mode === 'study' ? (
            <>
              <div className="split-line">
                <span>남은 학습</span>
                <strong>{studyRemaining}개</strong>
              </div>
              <div className="split-line">
                <span>오늘 완료</span>
                <strong>{studyCompleted}개</strong>
              </div>
            </>
          ) : mode === 'review' ? (
            <>
              <div className="split-line">
                <span>남은 복습</span>
                <strong>{reviewRemaining}개</strong>
              </div>
              <div className="split-line">
                <span>오늘 복습 완료</span>
                <strong>{reviewCompleted}개</strong>
              </div>
            </>
          ) : (
            <div className="split-line">
              <span>랜덤 학습</span>
              <strong>
                {randomCurrent} / {randomTotal}
              </strong>
            </div>
          )}
          <div className="progress-track">
            <span
              className="progress-fill"
              style={{ width: `${(progressNumerator / progressDenominator) * 100}%` }}
            />
          </div>
        </div>
      </header>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', overflowY: 'auto', paddingInline: '0.2rem', minHeight: 0, scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
        <style>{`
          .learning-scroll-container::-webkit-scrollbar { display: none; }
        `}</style>
        <div className="learning-scroll-container" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', flex: 1 }}>
          <section className="learning-main-row" style={{ justifyItems: 'stretch', width: '100%', alignItems: 'stretch' }}>
          <article className="learning-panel learning-half" style={{ padding: 0, display: 'grid', gridTemplateColumns: '7fr minmax(130px, 3fr)', overflow: 'hidden' }}>
            <div style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.8rem', minHeight: '100%' }}>
              <div className="learning-head-copy">
                <h1 style={{ fontSize: '1.6rem', marginBottom: '0.3rem' }}>{data.sourceText}</h1>
                <p style={{ fontSize: '0.9rem' }}>{data.nuanceNote}</p>
              </div>

              <div className="split-line">
                <h3 style={{ fontSize: '1.05rem', margin: 0 }}>정답 입력</h3>
                <button
                  className={`icon-button bordered${favorite ? ' active' : ''}`}
                  onClick={() => void handleFavorite()}
                  type="button"
                  style={{ width: '2.2rem', minHeight: '2.2rem' }}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '1.1rem' }}>bookmark</span>
                </button>
              </div>

              <textarea
                className="answer-input"
                onChange={(event) => setAnswer(event.target.value)}
                rows={4}
                value={answer}
                style={{ minHeight: '5rem', fontSize: '0.95rem', padding: '0.75rem' }}
              />

              <button className="button primary wide" onClick={() => void handleCheckAnswer()} type="button" style={{ padding: '0.6rem', fontSize: '0.95rem' }}>
                정답 확인
              </button>
            </div>

            <div style={{ background: 'var(--surface-high)', position: 'relative', borderLeft: '1px solid var(--border)', minHeight: '100%' }}>
              {/* 이미지 영역 (3 View) */}
              <img 
                src="https://images.unsplash.com/photo-1544457070-4cd773b4d71e?auto=format&fit=crop&q=80&w=400" 
                alt="Context visualization" 
                style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', objectFit: 'cover' }} 
              />
            </div>
          </article>

          <article className={`learning-panel learning-half answer-card${answerResult?.isCorrect ? ' success' : ''}${!answerCard ? ' answer-card-hidden' : ''}`} style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
            <div className="split-line">
              <div>
                <p className="eyebrow" style={{ fontSize: '0.65rem' }}>Answer</p>
                <h3 style={{ fontSize: '1.05rem', margin: 0 }}>{answerCard ? answerCard.text : '정답 확인 후 표시됩니다.'}</h3>
              </div>
              <button
                className="icon-button"
                disabled={!answerCard}
                onClick={() => answerCard ? speak(answerCard.text) : undefined}
                type="button"
                style={{ width: '2.2rem', minHeight: '2.2rem' }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '1.1rem' }}>volume_up</span>
              </button>
            </div>

            <div className="example-block" style={{ padding: '0.8rem', gap: '0.5rem', flex: 1 }}>
              <div className="split-line">
                <strong style={{ fontSize: '0.9rem' }}>Example</strong>
                <button
                  className="icon-button"
                  disabled={!answerCard}
                  onClick={() => answerCard ? speak(answerCard.sentence) : undefined}
                  type="button"
                  style={{ width: '1.8rem', minHeight: '1.8rem', background: 'transparent', boxShadow: 'none' }}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: '1rem', color: 'var(--muted)' }}>volume_up</span>
                </button>
              </div>
              <p className="example-text" style={{ fontSize: '0.9rem' }}>{answerCard ? answerCard.sentence : '정답 확인 후 예문이 표시됩니다.'}</p>
              <p className="example-translation" style={{ fontSize: '0.85rem' }}>{answerCard ? answerCard.translation : '해석도 함께 표시됩니다.'}</p>
            </div>
          </article>
        </section>

        <section className="learning-panel sentence-panel mobile-sentence-panel" style={{ flexShrink: 0, display: 'grid', gridTemplateColumns: 'minmax(0, 1.5fr) minmax(0, 1fr)', gap: '1rem', padding: '1.25rem', width: '100%' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
            <div className="split-line">
              <div>
                <p className="eyebrow" style={{ fontSize: '0.65rem' }}>Sentence Practice</p>
                <h3 style={{ fontSize: '1.05rem', margin: 0 }}>내 문장 만들기</h3>
              </div>
            </div>

            <div style={{ position: 'relative', display: 'flex', flexDirection: 'column', flex: 1, minHeight: '5.5rem' }}>
              <textarea
                onChange={(event) => setSentence(event.target.value)}
                placeholder="표현을 사용해서 직접 문장을 만들어보세요."
                value={sentence}
                style={{ flex: 1, minHeight: '100%', resize: 'none', padding: '0.75rem', paddingBottom: '2.8rem', fontSize: '0.9rem', borderRadius: '0.8rem', border: 'none', background: 'var(--surface-low)', outline: 'none', width: '100%', boxShadow: 'inset 0 0 0 1px rgba(198, 197, 212, 0.2)' }}
              />
              <button
                className="button primary"
                disabled={sentenceFeedbackLoading}
                onClick={() => void handleSentenceFeedback()}
                style={{ position: 'absolute', bottom: '0.5rem', right: '0.5rem', padding: '0.4rem 1.2rem', fontSize: '0.8rem', minHeight: '2.2rem', opacity: sentenceFeedbackLoading ? 0.7 : 1 }}
                type="button"
              >
                확인
              </button>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
            <div style={{ display: 'flex', flexDirection: 'column', flex: 1, padding: '1rem', gap: '0.8rem', background: 'rgba(139, 241, 230, 0.18)', borderRadius: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <div className="icon-badge mint" style={{ width: '1.8rem', height: '1.8rem', minWidth: '1.8rem' }}>
                  <span className="material-symbols-outlined" style={{ fontSize: '0.9rem' }}>auto_awesome</span>
                </div>
                <strong style={{ fontSize: '0.9rem', color: 'var(--mint-deep)' }}>AI 문장 피드백</strong>
              </div>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflowY: 'auto' }}>
                <style>{`@keyframes spinPulse { 0% { transform: rotate(0deg) scale(1); } 50% { transform: rotate(180deg) scale(1.1); } 100% { transform: rotate(360deg) scale(1); } }`}</style>
                {sentenceFeedbackLoading ? (
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', gap: '0.8rem', color: 'var(--mint-deep)', padding: '1rem 0' }}>
                    <span className="material-symbols-outlined" style={{ fontSize: '2rem', animation: 'spinPulse 1.5s linear infinite' }}>autorenew</span>
                    <p style={{ margin: 0, fontSize: '0.9rem', fontWeight: 600 }}>AI가 문장을 교정하고 있어요...</p>
                  </div>
                ) : (
                  <p style={{ margin: 0, fontSize: '0.85rem', lineHeight: '1.45', color: 'var(--text)', whiteSpace: 'pre-line' }}>
                    {sentenceFeedbackText}
                  </p>
                )}
              </div>
            </div>
          </div>
        </section>
        </div>
      </div>

      {mode === 'random' ? (
        <footer className="review-action-bar learning-review-bar" style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr)', gap: '0.5rem', padding: '0.8rem' }}>
          <button
            className="button primary wide"
            disabled={!answerResult}
            onClick={() => {
              if (!answerResult) {
                return;
              }

              if (!randomSession || !randomSessionId) {
                showToast('랜덤 학습 세션을 찾지 못했습니다.', 'error');
                return;
              }

              const nextIndex = Math.max(currentStep, 1);
              const nextItemId = randomSession.itemIds[nextIndex];

              if (!nextItemId) {
                clearRandomLearningSession(randomSessionId);
                navigate(seriesId ? `/series/${seriesId}` : '/my-series', { replace: true });
                return;
              }

              const nextParams = new URLSearchParams({
                mode: 'random',
                packId: randomSession.packId,
                randomSession: randomSessionId,
                seriesId: randomSession.seriesId,
                step: String(nextIndex + 1),
              });
              navigate(`/learning/${nextItemId}?${nextParams.toString()}`, { replace: true });
            }}
            type="button"
            style={{
              minHeight: '3.1rem',
              opacity: answerResult ? 1 : 0.45,
              cursor: answerResult ? 'pointer' : 'not-allowed',
            }}
          >
            다음 문제
          </button>
        </footer>
      ) : mode === 'review' ? (
        <footer className="review-action-bar learning-review-bar" style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: '0.5rem', padding: '0.8rem' }}>
          <button
            className="button secondary wide"
            disabled={!answerResult}
            onClick={() => void handleReview('repeat')}
            type="button"
            style={{
              minHeight: '3rem',
              opacity: answerResult ? 1 : 0.45,
              cursor: answerResult ? 'pointer' : 'not-allowed',
            }}
          >
            다시복습하기
          </button>
          <button
            className="button primary wide"
            disabled={!answerResult}
            onClick={() => void handleReview('review_done')}
            type="button"
            style={{
              minHeight: '3rem',
              opacity: answerResult ? 1 : 0.45,
              cursor: answerResult ? 'pointer' : 'not-allowed',
            }}
          >
            오늘복습완료
          </button>
        </footer>
      ) : (
        <footer
          className="review-action-bar learning-review-bar"
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
            gap: '0.5rem',
            padding: '0.8rem',
          }}
        >
          {reviewOptions.map((option) => (
            <button
              className={`review-pill review-pill-compact${reviewResult === option.result ? ' active' : ''}`}
              disabled={!answerResult}
              key={option.result}
              onClick={() => void handleReview(option.result)}
              type="button"
              style={{
                minHeight: '3rem',
                padding: '0.4rem 0.2rem',
                gap: '0.15rem',
                opacity: answerResult ? 1 : 0.45,
                cursor: answerResult ? 'pointer' : 'not-allowed',
              }}
            >
              <strong style={{ fontSize: '0.82rem' }}>{option.label}</strong>
              <span style={{ fontSize: '0.65rem', whiteSpace: 'nowrap' }}>{option.subtitle}</span>
            </button>
          ))}
        </footer>
      )}
    </div>
  );
}
