import { useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { ErrorPanel, LoadingPanel } from '../components/StatePanels';
import { useRemoteData } from '../hooks/useRemoteData';
import { contentService } from '../services/contentService';
import type { ReviewResult } from '../types/models';

const reviewOptions: Array<{ label: string; result: ReviewResult; subtitle: string }> = [
  { label: '다시', result: 'again', subtitle: '1일' },
  { label: '어려움', result: 'hard', subtitle: '2일' },
  { label: '좋음', result: 'good', subtitle: '4일' },
  { label: '쉬움', result: 'easy', subtitle: '7일' },
];

export function LearningPage() {
  const { itemId = '' } = useParams();
  const { data, error, loading, reload } = useRemoteData(
    () => contentService.getLearningItem(itemId),
    [itemId],
  );
  const [answer, setAnswer] = useState('');
  const [revealed, setRevealed] = useState(false);
  const [sentence, setSentence] = useState('');
  const [favorite, setFavorite] = useState(false);
  const [reviewResult, setReviewResult] = useState<ReviewResult | null>(null);

  const isCorrect = useMemo(() => {
    const normalizedAnswer = answer.trim().toLowerCase();
    return normalizedAnswer.length > 0 && normalizedAnswer === data?.targetText.toLowerCase();
  }, [answer, data?.targetText]);

  if (loading) {
    return <LoadingPanel message="학습 카드를 준비하고 있습니다." />;
  }

  if (error || !data) {
    return (
      <ErrorPanel
        actionLabel="다시 시도"
        message={error ?? '학습 카드를 찾을 수 없습니다.'}
        onAction={() => void reload()}
        title="학습 화면을 준비할 수 없습니다."
      />
    );
  }

  const handleReveal = () => {
    setRevealed(true);
  };

  const handleSpeak = () => {
    const utterance = new SpeechSynthesisUtterance(data.targetText);
    utterance.lang = 'en-US';
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  };

  return (
    <div className="learning-page">
      <header className="learning-topbar">
        <div className="learning-topbar-copy">
          <p className="eyebrow">Meaning &amp; Context</p>
          <h3>오늘의 핵심 표현 복습</h3>
        </div>
        <div className="learning-progress">
          <div className="split-line">
            <span>Progress</span>
            <strong>
              {data.progress.current} / {data.progress.total} items
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

      <section className="learning-card">
        <div className="center-copy">
          <p className="eyebrow">Meaning &amp; Context</p>
          <h1>{data.sourceText}</h1>
          <p>{data.nuanceNote}</p>
        </div>

        <label className="field-block">
          영어 표현을 직접 입력해보세요
          <textarea
            onChange={(event) => setAnswer(event.target.value)}
            placeholder="예: doze off"
            rows={2}
            value={answer}
          />
        </label>

        <div className="actions-row">
          <button className="button primary" onClick={handleReveal} type="button">
            정답 확인
          </button>
          <button
            className={`icon-button bordered${favorite ? ' active' : ''}`}
            onClick={() => setFavorite((prev) => !prev)}
            type="button"
          >
            <span className="material-symbols-outlined">bookmark</span>
          </button>
        </div>

        {revealed ? (
          <section className={`answer-panel${isCorrect ? ' success' : ''}`}>
            <div>
              <p className="eyebrow">Answer</p>
              <h3>{data.targetText}</h3>
              <p>{isCorrect ? '정답입니다. 표현 감각이 좋습니다.' : data.exampleSentence}</p>
            </div>
            <button className="icon-button" onClick={handleSpeak} type="button">
              <span className="material-symbols-outlined">volume_up</span>
            </button>
          </section>
        ) : null}

        <section className="practice-panel">
          <div className="split-line">
            <div>
              <p className="eyebrow">Sentence Practice</p>
              <h3>내 문장 만들기</h3>
            </div>
            <button className="button secondary" onClick={handleSpeak} type="button">
              발음 듣기
            </button>
          </div>

          <textarea
            onChange={(event) => setSentence(event.target.value)}
            placeholder="이 표현을 사용해서 문장을 만들어보세요."
            rows={4}
            value={sentence}
          />

          <div className="ai-feedback">
            <div className="icon-badge mint">
              <span className="material-symbols-outlined">auto_awesome</span>
            </div>
            <div>
              <strong>AI 피드백</strong>
              <p>
                {sentence.trim().length > 0
                  ? data.aiFeedback
                  : '문장을 입력하면 AI 첨삭 결과가 이 영역에 표시되도록 구조를 잡아두었습니다.'}
              </p>
            </div>
          </div>
        </section>
      </section>

      <footer className="review-action-bar">
        {reviewOptions.map((option) => (
          <button
            className={`review-pill${reviewResult === option.result ? ' active' : ''}`}
            key={option.result}
            onClick={() => setReviewResult(option.result)}
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
