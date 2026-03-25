import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useToast } from '../app/ToastContext';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';
import type { CheckAnswerResponse, ReviewResult } from '../types/models';

const reviewOptions: Array<{ label: string; result: ReviewResult; subtitle: string }> = [
  { label: '1분 후', result: 'minute', subtitle: '1분' },
  { label: '다시', result: 'again', subtitle: '1일' },
  { label: '어려움', result: 'hard', subtitle: '2일' },
  { label: '좋음', result: 'good', subtitle: '4일' },
  { label: '쉬움', result: 'easy', subtitle: '7일' },
  { label: '1달', result: 'month', subtitle: '30일' },
  { label: '1년', result: 'year', subtitle: '365일' },
  { label: '제외', result: 'exclude', subtitle: '목록 이동' },
];

export function LearningPage() {
  const { itemId = '' } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { showToast } = useToast();
  const mode = searchParams.get('mode') ?? 'study';
  const seriesId = searchParams.get('seriesId');
  const randomStep = Number(searchParams.get('step') ?? '1');
  const { data, error, loading, reload } = useRemoteData(() => contentService.getLearningItem(itemId, mode), [itemId, mode]);
  const [answer, setAnswer] = useState('');
  const [answerResult, setAnswerResult] = useState<CheckAnswerResponse | null>(null);
  const [sentence, setSentence] = useState('');
  const [favorite, setFavorite] = useState(false);
  const [reviewResult, setReviewResult] = useState<ReviewResult | null>(null);

  useEffect(() => {
    setAnswer('');
    setAnswerResult(null);
    setSentence('');
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
  const displayCurrent = mode === 'random' && data.progress.total > 0
    ? ((Math.max(randomStep, 1) - 1) % data.progress.total) + 1
    : data.progress.current;
  const displayTotal = data.progress.total;

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

    try {
      const response = await contentService.submitReview(itemId, result, mode);
      setReviewResult(result);
      showToast(
        mode === 'random'
          ? '다음 랜덤 단어로 넘어갑니다.'
          : result === 'exclude'
          ? '복습 목록에서 제외했습니다.'
          : `복습 결과를 저장했습니다. 다음 복습은 ${response.nextReviewAt ? new Date(response.nextReviewAt).toLocaleString('ko-KR') : '-'} 입니다.`,
        'success',
      );

      if (response.nextItemId) {
        const nextParams = new URLSearchParams({ mode });
        if (seriesId) {
          nextParams.set('seriesId', seriesId);
        }
        if (mode === 'random') {
          nextParams.set('step', String(Math.max(randomStep, 1) + 1));
        }
        navigate(`/learning/${response.nextItemId}?${nextParams.toString()}`, { replace: true });
        return;
      }

      if (mode === 'random') {
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
          <div className="split-line">
            <span>Progress</span>
            <strong>
              {displayCurrent} / {displayTotal}
            </strong>
          </div>
          <div className="progress-track">
            <span
              className="progress-fill"
              style={{ width: `${(displayCurrent / Math.max(displayTotal, 1)) * 100}%` }}
            />
          </div>
        </div>
      </header>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', overflowY: 'hidden', paddingInline: '0.2rem', minHeight: 0 }}>
        <section className="learning-main-row" style={{ flexShrink: 0, justifyItems: 'stretch', width: '100%', alignItems: 'stretch' }}>
          <article className="learning-panel learning-half" style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
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

            <textarea
              onChange={(event) => setSentence(event.target.value)}
              placeholder="표현을 사용해서 직접 문장을 만들어보세요."
              value={sentence}
              style={{ flex: 1, minHeight: '5rem', resize: 'none', padding: '0.75rem', fontSize: '0.9rem', borderRadius: '0.8rem', border: 'none', background: 'var(--surface-low)', outline: 'none', width: '100%', boxShadow: 'inset 0 0 0 1px rgba(198, 197, 212, 0.2)' }}
            />
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
                <p style={{ margin: 0, fontSize: '0.85rem', lineHeight: '1.4', color: 'var(--text)' }}>
                  {sentence.trim().length > 0
                    ? data.aiFeedback
                    : '문장을 입력하면 여기에서 피드백을 이어갈 수 있습니다.'}
                </p>
              </div>
              <button className="button primary" style={{ alignSelf: 'flex-end', padding: '0.4rem 0.8rem', fontSize: '0.8rem', minHeight: '2rem' }} type="button">
                확인
              </button>
            </div>
          </div>
        </section>
      </div>

      {mode === 'random' ? (
        <footer className="review-action-bar learning-review-bar" style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr)', gap: '0.5rem', padding: '0.8rem' }}>
          <button
            className="button primary wide"
            disabled={!answerResult}
            onClick={() => void handleReview('good')}
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
      ) : (
        <footer className="review-action-bar learning-review-bar" style={{ display: 'grid', gridTemplateColumns: 'repeat(4, minmax(0, 1fr))', gap: '0.5rem', padding: '0.8rem' }}>
          {reviewOptions.map((option) => (
            <button
              className={`review-pill review-pill-compact${reviewResult === option.result ? ' active' : ''}${option.result === 'exclude' ? ' exclude' : ''}`}
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
