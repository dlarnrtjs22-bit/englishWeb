import { useEffect, useMemo, useState } from 'react';
import { useToast } from '../app/ToastContext';
import { diaryService } from '../services/diaryService';
import type { DiaryFeedbackResponse, DiaryHistoryItem } from '../types/models';
import { buildHighlightSegments } from '../utils/highlightDiff';

function formatDate(date: Date) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function toDisplayDate(entryDate: string) {
  return entryDate;
}

function renderHighlightedCorrection(original: string, corrected: string) {
  return buildHighlightSegments(original, corrected).map((segment, index) => (
    <span
      key={`${segment.text}-${index}`}
      style={segment.changed ? { color: 'var(--primary)', fontWeight: 700 } : undefined}
    >
      {segment.text}
    </span>
  ));
}

export function DiaryPage() {
  const { showToast } = useToast();
  const today = formatDate(new Date());
  const [selectedDate, setSelectedDate] = useState<string>(today);
  const [content, setContent] = useState('');
  const [isCorrecting, setIsCorrecting] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isEntryLoading, setIsEntryLoading] = useState(true);
  const [correctionResult, setCorrectionResult] = useState<DiaryFeedbackResponse | null>(null);
  const [writtenDates, setWrittenDates] = useState<string[]>([]);
  const [historyItems, setHistoryItems] = useState<DiaryHistoryItem[]>([]);
  const [showHistory, setShowHistory] = useState(false);

  const selectedDateObj = useMemo(() => new Date(`${selectedDate}T00:00:00`), [selectedDate]);
  const dates = useMemo(
    () =>
      Array.from({ length: 14 }).map((_, index) => {
        const value = new Date(selectedDateObj);
        value.setDate(value.getDate() - 10 + index);
        return value;
      }),
    [selectedDateObj],
  );

  const loadEntry = async (entryDate: string) => {
    try {
      setIsEntryLoading(true);
      const response = await diaryService.getEntry(entryDate);
      setContent(response.rawContent);
      setCorrectionResult(response.feedback);
    } catch (cause) {
      setContent('');
      setCorrectionResult(null);
      showToast(cause instanceof Error ? cause.message : '일기를 불러오지 못했습니다.', 'error');
    } finally {
      setIsEntryLoading(false);
    }
  };

  const loadCalendar = async (centerDate: string) => {
    try {
      const center = new Date(`${centerDate}T00:00:00`);
      const from = new Date(center);
      const to = new Date(center);
      from.setDate(from.getDate() - 10);
      to.setDate(to.getDate() + 3);
      const response = await diaryService.getCalendar(formatDate(from), formatDate(to));
      setWrittenDates(response.writtenDates);
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '작성 날짜를 불러오지 못했습니다.', 'error');
    }
  };

  const loadHistory = async () => {
    try {
      const response = await diaryService.getHistory(50);
      setHistoryItems(response.items);
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '과거 일기 목록을 불러오지 못했습니다.', 'error');
    }
  };

  useEffect(() => {
    void loadEntry(selectedDate);
    void loadCalendar(selectedDate);
  }, [selectedDate]);

  useEffect(() => {
    void loadHistory();
  }, []);

  const handleDateChange = (dateStr: string) => {
    if (dateStr > today) {
      showToast('미래의 일기는 작성할 수 없습니다.', 'error');
      return;
    }

    setSelectedDate(dateStr);
  };

  const handleCorrect = async () => {
    if (!content.trim()) {
      showToast('일기 내용을 먼저 작성해주세요.', 'error');
      return;
    }

    try {
      setIsCorrecting(true);
      const response = await diaryService.getFeedback(selectedDate, content.trim());
      setCorrectionResult(response);
      showToast('AI 첨삭이 완료되었습니다.', 'success');
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : 'AI 첨삭에 실패했습니다.', 'error');
    } finally {
      setIsCorrecting(false);
    }
  };

  const handleSave = async () => {
    if (!content.trim()) {
      showToast('저장할 내용이 없습니다.', 'error');
      return;
    }

    try {
      setIsSaving(true);
      const response = await diaryService.saveEntry(selectedDate, content.trim(), correctionResult);
      setContent(response.rawContent);
      setCorrectionResult(response.feedback);
      await Promise.all([loadCalendar(selectedDate), loadHistory()]);
      showToast('일기가 저장되었습니다.', 'success');
    } catch (cause) {
      showToast(cause instanceof Error ? cause.message : '일기 저장에 실패했습니다.', 'error');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="page-stack">
      <section className="section-header" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', alignItems: 'stretch' }}>
        <div className="split-line">
          <div>
            <p className="eyebrow">Read &amp; Write</p>
            <h3>나의 영어 일기</h3>
          </div>
          <button className="button secondary" type="button" onClick={() => setShowHistory(true)}>
            <span className="material-symbols-outlined" style={{ fontSize: '1.1rem', marginRight: '0.3rem' }}>menu_book</span>
            과거 일기 모아보기
          </button>
        </div>

        <div
          style={{
            display: 'flex',
            gap: '0.8rem',
            overflowX: 'auto',
            paddingBottom: '0.5rem',
            scrollbarWidth: 'none',
          }}
        >
          <div style={{ position: 'relative', display: 'flex', alignItems: 'stretch' }}>
            <input
              id="hidden-date-picker"
              type="date"
              value={selectedDate}
              max={today}
              onChange={(event) => {
                if (event.target.value) {
                  handleDateChange(event.target.value);
                }
              }}
              style={{
                position: 'absolute',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                width: '0',
                height: '0',
                padding: 0,
                border: 'none',
                opacity: 0,
                pointerEvents: 'none',
              }}
            />
            <button
              type="button"
              onClick={() => {
                const picker = document.getElementById('hidden-date-picker') as HTMLInputElement | null;
                try {
                  if (picker?.showPicker) {
                    picker.showPicker();
                  } else {
                    picker?.focus();
                  }
                } catch {
                  picker?.focus();
                }
              }}
              style={{
                minWidth: '4.5rem',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '0.8rem 0.5rem',
                borderRadius: '1rem',
                background: 'var(--surface-high)',
                color: 'var(--text)',
                border: '1px dashed var(--border)',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
              }}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '1.5rem', marginBottom: '0.2rem', color: 'var(--primary)' }}>calendar_month</span>
              <span style={{ fontSize: '0.75rem', fontWeight: 600 }}>날짜 검색</span>
            </button>
          </div>

          <div style={{ width: '1px', background: 'var(--border)', margin: '0.5rem 0' }} />

          {dates.map((dateObj) => {
            const dateStr = formatDate(dateObj);
            const isSelected = selectedDate === dateStr;
            const isWritten = writtenDates.includes(dateStr);
            const isToday = today === dateStr;
            const isFuture = dateStr > today;
            const dayName = new Intl.DateTimeFormat('en-US', { weekday: 'short' }).format(dateObj);
            const dayNum = dateObj.getDate();

            return (
              <button
                key={dateStr}
                onClick={() => handleDateChange(dateStr)}
                disabled={isFuture}
                type="button"
                style={{
                  minWidth: '4.5rem',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  padding: '0.8rem 0.5rem',
                  borderRadius: '1rem',
                  background: isSelected ? 'var(--primary)' : 'var(--surface-high)',
                  color: isSelected ? '#fff' : 'var(--text)',
                  border: isToday && !isSelected ? '2px solid var(--primary-light)' : '2px solid transparent',
                  cursor: isFuture ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s ease',
                  position: 'relative',
                  opacity: isFuture ? 0.3 : 1,
                }}
              >
                <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600, opacity: isSelected ? 0.9 : 0.6 }}>
                  {dayName}
                </span>
                <span style={{ fontSize: '1.2rem', fontWeight: 700, margin: '0.2rem 0' }}>
                  {dayNum}
                </span>
                <div
                  style={{
                    width: '6px',
                    height: '6px',
                    borderRadius: '50%',
                    background: isWritten ? (isSelected ? '#fff' : 'var(--mint-deep)') : 'transparent',
                    marginTop: '0.2rem',
                  }}
                />
              </button>
            );
          })}
        </div>
      </section>

      <div className="learning-main-row" style={{ alignItems: 'start' }}>
        <section className="content-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', height: '100%', minHeight: '500px' }}>
          <div className="split-line">
            <div>
              <p className="eyebrow">{toDisplayDate(selectedDate)} 의 일기</p>
              <h3>오늘의 기록</h3>
            </div>
            <span className="material-symbols-outlined" style={{ color: 'var(--muted)' }}>edit_note</span>
          </div>
          <p style={{ fontSize: '0.95rem', color: 'var(--muted)' }}>
            영어로 하루를 기록해보세요. 완벽하지 않아도 괜찮습니다. AI가 자연스러운 표현으로 다듬어 드립니다.
          </p>
          <textarea
            onChange={(event) => setContent(event.target.value)}
            placeholder="I went to the park and saw a cute dog..."
            value={content}
            style={{
              flex: 1,
              width: '100%',
              minHeight: '250px',
              padding: '1.25rem',
              borderRadius: '0.8rem',
              border: '1px solid var(--border)',
              background: 'var(--background)',
              fontSize: '1.05rem',
              lineHeight: '1.6',
              resize: 'none',
              outline: 'none',
              fontFamily: 'inherit',
              opacity: isEntryLoading ? 0.7 : 1,
            }}
          />
        </section>

        <section className="content-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', minHeight: '500px', background: 'rgba(139, 241, 230, 0.05)' }}>
          <div className="split-line">
            <div>
              <p className="eyebrow">AI Feedback</p>
              <h3 style={{ color: 'var(--mint-deep)' }}>AI 첨삭 결과</h3>
            </div>
            <div className="icon-badge mint">
              <span className="material-symbols-outlined">auto_awesome</span>
            </div>
          </div>

          <div
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              gap: '1.5rem',
              padding: '1.25rem',
              borderRadius: '0.8rem',
              background: 'var(--surface-high)',
              border: '1px solid rgba(139, 241, 230, 0.2)',
            }}
          >
            {isCorrecting ? (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, color: 'var(--muted)', gap: '1rem' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '2rem', animation: 'spin 2s linear infinite' }}>autorenew</span>
                <p>문장과 표현을 분석하고 첨삭 중입니다...</p>
              </div>
            ) : correctionResult ? (
              <>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                  <h4 style={{ fontSize: '1rem', color: 'var(--mint-deep)', margin: 0 }}>{correctionResult.headline}</h4>
                  <p style={{ margin: 0, fontSize: '0.95rem', color: 'var(--text)', lineHeight: '1.5' }}>{correctionResult.summary}</p>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {correctionResult.lines.map((line, index) => (
                    <div key={`${line.correctedLine}-${index}`} style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', paddingBottom: '0.9rem', borderBottom: '1px dashed var(--border)' }}>
                      <p style={{ margin: 0, fontSize: '1rem', lineHeight: '1.6', color: 'var(--text)', fontWeight: 600 }}>
                        {renderHighlightedCorrection(line.originalLine, line.correctedLine)}
                      </p>
                      <p style={{ margin: 0, fontSize: '0.9rem', lineHeight: '1.5', color: 'var(--muted)' }}>
                        {line.translationLine}
                      </p>
                    </div>
                  ))}
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: 'auto', paddingTop: '1.5rem', borderTop: '1px dashed var(--border)' }}>
                  {correctionResult.keywords.length > 0 ? (
                    <div>
                      <h4 style={{ fontSize: '0.85rem', color: 'var(--muted)', marginBottom: '0.5rem' }}>주요 사용 단어</h4>
                      <div className="tag-row">
                        {correctionResult.keywords.map((keyword) => (
                          <span key={keyword} className="tag neutral" style={{ background: 'var(--surface-low)' }}>
                            {keyword}
                          </span>
                        ))}
                      </div>
                    </div>
                  ) : null}

                  {correctionResult.tips.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                      <h4 style={{ fontSize: '0.85rem', color: 'var(--muted)', marginBottom: '0.15rem' }}>팁</h4>
                      {correctionResult.tips.map((tip) => (
                        <p key={tip} style={{ margin: 0, fontSize: '0.88rem', lineHeight: '1.45', color: 'var(--text)' }}>· {tip}</p>
                      ))}
                    </div>
                  ) : null}

                  {correctionResult.advice.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                      <h4 style={{ fontSize: '0.85rem', color: 'var(--muted)', marginBottom: '0.15rem' }}>조언</h4>
                      {correctionResult.advice.map((advice) => (
                        <p key={advice} style={{ margin: 0, fontSize: '0.88rem', lineHeight: '1.45', color: 'var(--text)' }}>· {advice}</p>
                      ))}
                    </div>
                  ) : null}
                </div>
              </>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, color: 'var(--muted)', textAlign: 'center', padding: '2rem' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '3rem', opacity: 0.3, marginBottom: '1rem' }}>edit_document</span>
                <p>왼쪽에 일기를 작성하고<br />AI 첨삭을 받아보세요.</p>
              </div>
            )}
          </div>
        </section>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
        <button
          className="button secondary"
          disabled={!content.trim() || isCorrecting}
          onClick={() => void handleCorrect()}
          type="button"
          style={{ minWidth: '120px' }}
        >
          {isCorrecting ? '첨삭 중...' : 'AI 첨삭하기'}
        </button>
        <button
          className="button primary"
          disabled={!content.trim() || isSaving}
          onClick={() => void handleSave()}
          type="button"
          style={{ minWidth: '120px' }}
        >
          {isSaving ? '저장 중...' : '저장하기'}
        </button>
      </div>

      {showHistory ? (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100vw',
            height: '100vh',
            background: 'rgba(0, 0, 0, 0.4)',
            zIndex: 9999,
            display: 'flex',
            justifyContent: 'flex-end',
            backdropFilter: 'blur(3px)',
          }}
          onClick={() => setShowHistory(false)}
        >
          <div
            style={{
              width: '100%',
              maxWidth: '420px',
              height: '100%',
              background: 'var(--background)',
              boxShadow: '-4px 0 25px rgba(0,0,0,0.15)',
              padding: '2rem 1.5rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '1.5rem',
              overflowY: 'auto',
            }}
            onClick={(event) => event.stopPropagation()}
          >
            <div className="split-line">
              <div>
                <p className="eyebrow" style={{ fontSize: '0.8rem' }}>History</p>
                <h3 style={{ margin: 0, fontSize: '1.25rem' }}>과거 일기 모아보기</h3>
              </div>
              <button
                className="icon-button"
                onClick={() => setShowHistory(false)}
                type="button"
                style={{ background: 'var(--surface-high)' }}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <p style={{ fontSize: '0.9rem', color: 'var(--muted)', margin: 0 }}>
              과거에 작성했던 일기들을 피드처럼 모아봅니다. 클릭하면 해당 날짜 기록을 불러옵니다.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '0.5rem' }}>
              {historyItems.map((item) => (
                <button
                  key={item.entryDate}
                  type="button"
                  onClick={() => {
                    setSelectedDate(item.entryDate);
                    setShowHistory(false);
                    showToast(`${item.entryDate} 기록을 불러왔습니다.`, 'success');
                  }}
                  style={{
                    padding: '1.25rem',
                    borderRadius: '1rem',
                    background: 'var(--surface-low)',
                    border: '1px solid var(--border)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.8rem',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease',
                    boxShadow: '0 2px 5px rgba(0,0,0,0.02)',
                  }}
                >
                  <div className="split-line" style={{ width: '100%' }}>
                    <span style={{ fontSize: '0.95rem', color: 'var(--primary)', fontWeight: 600 }}>{item.entryDate}</span>
                    <span className="material-symbols-outlined" style={{ fontSize: '1.1rem', color: 'var(--muted)' }}>chevron_right</span>
                  </div>
                  <p style={{ fontSize: '0.95rem', lineHeight: '1.5', margin: 0, color: 'var(--text)' }}>
                    {item.rawSnippet}
                  </p>
                  {item.correctedSnippet ? (
                    <p style={{ fontSize: '0.82rem', lineHeight: '1.45', margin: 0, color: 'var(--muted)' }}>
                      {item.correctedSnippet}
                    </p>
                  ) : null}
                  {item.keywords.length > 0 ? (
                    <div className="tag-row" style={{ marginTop: '0.2rem' }}>
                      {item.keywords.map((word) => (
                        <span key={`${item.entryDate}-${word}`} className="tag neutral" style={{ background: 'var(--surface-high)', fontSize: '0.75rem' }}>
                          {word}
                        </span>
                      ))}
                    </div>
                  ) : null}
                </button>
              ))}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
