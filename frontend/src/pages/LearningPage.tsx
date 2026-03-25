import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
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
  const { showToast } = useToast();
  const { data, error, loading, reload } = useRemoteData(
    () => contentService.getLearningItem(itemId),
    [itemId],
  );
  const [answer, setAnswer] = useState('');
  const [answerResult, setAnswerResult] = useState<CheckAnswerResponse | null>(null);
  const [sentence, setSentence] = useState('');
  const [favorite, setFavorite] = useState(false);
  const [reviewResult, setReviewResult] = useState<ReviewResult | null>(null);

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

  const speak = (text: string) => {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 0.95;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  };

  const handleCheckAnswer = async () => {
    try {
      const response = await contentService.checkAnswer(itemId, answer);
      setAnswerResult(response);
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
    try {
      const response = await contentService.submitReview(itemId, result);
      setReviewResult(result);
      showToast(
        result === 'exclude'
          ? '복습 목록에서 제외했습니다.'
          : `복습 결과를 저장했습니다. 다음 복습은 ${response.nextReviewAt ? new Date(response.nextReviewAt).toLocaleString('ko-KR') : '-'} 입니다.`,
        'success',
      );

      if (response.nextItemId) {
        navigate(`/learning/${response.nextItemId}`, { replace: true });
        return;
      }

      navigate('/reviews', { replace: true });
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '복습 저장에 실패했습니다.', 'error');
    }
  };

  return (
    <div className="learning-workspace">
      <header className="learning-topbar compact">
        <div className="learning-topbar-copy">
          <p className="eyebrow">Meaning &amp; Context</p>
          <h3>핵심 표현 학습</h3>
        </div>
        <div className="learning-progress">
          <div className="split-line">
            <span>Progress</span>
            <strong>
              {data.progress.current} / {data.progress.total}
            </strong>
          </div>
          <div className="progress-track">
            <span
              className="progress-fill"
              style={{ width: `${(data.progress.current / data.progress.total) * 100}%` }}
            />
          </div>
        </div>
      </header>

      <section className="learning-main-row">
        <article className="learning-panel learning-half">
          <div className="learning-head-copy">
            <h1>{data.sourceText}</h1>
            <p>{data.nuanceNote}</p>
          </div>

          <div className="split-line">
            <h3>정답 입력</h3>
            <button
              className={`icon-button bordered${favorite ? ' active' : ''}`}
              onClick={() => void handleFavorite()}
              type="button"
            >
              <span className="material-symbols-outlined">bookmark</span>
            </button>
          </div>

          <textarea
            className="answer-input"
            onChange={(event) => setAnswer(event.target.value)}
            rows={5}
            value={answer}
          />

          <button className="button primary wide" onClick={() => void handleCheckAnswer()} type="button">
            정답 확인
          </button>
        </article>

        <article className={`learning-panel learning-half answer-card${answerResult?.isCorrect ? ' success' : ''}${!answerCard ? ' answer-card-hidden' : ''}`}>
          <div className="split-line">
            <div>
              <p className="eyebrow">Answer</p>
              <h3>{answerCard ? answerCard.text : '정답 확인 후 표시됩니다.'}</h3>
            </div>
            <button
              className="icon-button"
              disabled={!answerCard}
              onClick={() => answerCard ? speak(answerCard.text) : undefined}
              type="button"
            >
              <span className="material-symbols-outlined">volume_up</span>
            </button>
          </div>

          <div className="example-block">
            <div className="split-line">
              <strong>Example</strong>
              <button
                className="icon-button"
                disabled={!answerCard}
                onClick={() => answerCard ? speak(answerCard.sentence) : undefined}
                type="button"
              >
                <span className="material-symbols-outlined">volume_up</span>
              </button>
            </div>
            <p className="example-text">{answerCard ? answerCard.sentence : '정답 확인 후 예문이 표시됩니다.'}</p>
            <p className="example-translation">{answerCard ? answerCard.translation : '해석도 함께 표시됩니다.'}</p>
          </div>
        </article>
      </section>

      <section className="learning-panel sentence-panel">
        <div className="split-line">
          <div>
            <p className="eyebrow">Sentence Practice</p>
            <h3>내 문장 만들기</h3>
          </div>
        </div>

        <textarea
          onChange={(event) => setSentence(event.target.value)}
          placeholder="표현을 사용해서 직접 문장을 만들어보세요."
          rows={4}
          value={sentence}
        />

        <div className="ai-feedback">
          <div className="icon-badge mint">
            <span className="material-symbols-outlined">auto_awesome</span>
          </div>
          <div>
            <strong>AI 문장 피드백</strong>
            <p>
              {sentence.trim().length > 0
                ? data.aiFeedback
                : '문장을 입력하면 여기에서 피드백을 이어갈 수 있습니다.'}
            </p>
          </div>
        </div>
      </section>

      <footer className="review-action-bar learning-review-bar">
        {reviewOptions.map((option) => (
          <button
            className={`review-pill review-pill-compact${reviewResult === option.result ? ' active' : ''}${option.result === 'exclude' ? ' exclude' : ''}`}
            key={option.result}
            onClick={() => void handleReview(option.result)}
            type="button"
          >
            <strong>{option.label}</strong>
            <span>{option.subtitle}</span>
          </button>
        ))}
      </footer>
    </div>
  );
}
