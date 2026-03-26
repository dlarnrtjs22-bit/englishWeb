import { useState } from 'react';
import { useToast } from '../app/ToastContext';

interface CorrectionResult {
  correctedText: string;
  keyVerbs: string[];
  keyWords: string[];
}

export function DiaryPage() {
  const { showToast } = useToast();
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().slice(0, 10));
  const [content, setContent] = useState('');
  const [isCorrecting, setIsCorrecting] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [correctionResult, setCorrectionResult] = useState<CorrectionResult | null>(null);

  const [showHistory, setShowHistory] = useState(false);

  // Mock data for written dates
  const writtenDates = [
    new Date(Date.now() - 86400000 * 2).toISOString().slice(0, 10), // 2 days ago
    new Date(Date.now() - 86400000 * 3).toISOString().slice(0, 10), // 3 days ago
  ];

  interface HistoryItem {
    date: string;
    snippet: string;
    keyWords: string[];
  }

  const mockHistory: HistoryItem[] = [
    { date: writtenDates[0], snippet: 'I finally finished the project I was working on. It feels so rewarding and I am very happy.', keyWords: ['finished', 'rewarding'] },
    { date: writtenDates[1], snippet: 'A quiet Sunday. I just stayed home and read a book while drinking my favorite tea.', keyWords: ['quiet', 'drinking'] },
    { date: '2026-03-15', snippet: 'Went out for dinner with old friends. We shared a lot of memories.', keyWords: ['dinner', 'memories'] },
    { date: '2026-02-28', snippet: 'It rained all day. The sound of rain against the window was very peaceful.', keyWords: ['rain', 'peaceful'] },
  ];

  // Generate 14 days heavily anchored around the selected date
  const selectedDateObj = new Date(selectedDate);
  const dates = Array.from({ length: 14 }).map((_, i) => {
    const d = new Date(selectedDateObj);
    // Selected date is 4th from right (index 10)
    d.setDate(d.getDate() - 10 + i); 
    return d;
  });

  const handleDateChange = (dateStr: string) => {
    if (dateStr > new Date().toISOString().slice(0, 10)) {
      showToast('미래의 일기는 작성할 수 없습니다.', 'error');
      return;
    }
    setSelectedDate(dateStr);
    setContent('');
    setCorrectionResult(null);
  };

  const handleCorrect = () => {
    if (!content.trim()) {
      showToast('일기 내용을 먼저 작성해주세요.', 'error');
      return;
    }
    setIsCorrecting(true);
    // Mock API call
    setTimeout(() => {
      setCorrectionResult({
        correctedText: content.includes('go') 
          ? content.replace(/go/g, 'went') + '\n\n* AI Note: Past tense is more suitable here.' 
          : 'I had a great time today. The weather was perfect for a walk.\n(AI has naturally refined your expressions.)',
        keyVerbs: ['have (had)', 'be (was)'],
        keyWords: ['weather', 'perfect', 'walk'],
      });
      setIsCorrecting(false);
      showToast('AI 첨삭이 완료되었습니다.', 'success');
    }, 1500);
  };

  const handleSave = () => {
    if (!content.trim()) {
      showToast('저장할 내용이 없습니다.', 'error');
      return;
    }
    setIsSaving(true);
    setTimeout(() => {
      setIsSaving(false);
      showToast('일기가 성공적으로 저장되었습니다.', 'success');
    }, 800);
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
        
        {/* Horizontal Calendar Strip */}
        <div 
          style={{ 
            display: 'flex', 
            gap: '0.8rem', 
            overflowX: 'auto', 
            paddingBottom: '0.5rem',
            scrollbarWidth: 'none', // Firefox
          }}
        >
          {/* Calendar Picker Button */}
          <div style={{ position: 'relative', display: 'flex', alignItems: 'stretch' }}>
            <input 
              id="hidden-date-picker"
              type="date"
              value={selectedDate}
              max={new Date().toISOString().slice(0, 10)}
              onChange={(e) => { 
                if (e.target.value) handleDateChange(e.target.value);
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
                pointerEvents: 'none'
              }}
            />
            <button
              type="button"
              onClick={() => {
                const picker = document.getElementById('hidden-date-picker') as HTMLInputElement;
                try {
                  if (picker && picker.showPicker) {
                    picker.showPicker();
                  } else if (picker) {
                    picker.focus();
                  }
                } catch (e) {
                  // Fallback for older browsers
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
            const dateStr = dateObj.toISOString().slice(0, 10);
            const isSelected = selectedDate === dateStr;
            const isWritten = writtenDates.includes(dateStr);
            const todayStr = new Date().toISOString().slice(0, 10);
            const isToday = todayStr === dateStr;
            const isFuture = dateStr > todayStr;
            
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
                
                {/* Indicator Dot */}
                <div 
                  style={{ 
                    width: '6px', 
                    height: '6px', 
                    borderRadius: '50%', 
                    background: isWritten ? (isSelected ? '#fff' : 'var(--mint-deep)') : 'transparent',
                    marginTop: '0.2rem'
                  }} 
                />
              </button>
            );
          })}
        </div>
      </section>

      <div className="learning-main-row" style={{ alignItems: 'start' }}>
        {/* Left Side: Writing Area */}
        <section className="content-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', height: '100%', minHeight: '500px' }}>
          <div className="split-line">
            <div>
              <p className="eyebrow">{selectedDate} 의 일기</p>
              <h3>오늘의 기록</h3>
            </div>
            <span className="material-symbols-outlined" style={{ color: 'var(--muted)' }}>edit_note</span>
          </div>
          <p style={{ fontSize: '0.95rem', color: 'var(--muted)' }}>
            영어로 하루를 기록해보세요. 완벽하지 않아도 괜찮습니다. AI가 자연스러운 표현으로 다듬어 드립니다.
          </p>
          <textarea
            onChange={(e) => setContent(e.target.value)}
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
            }}
          />
        </section>

        {/* Right Side: AI Correction Area */}
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
              border: '1px solid rgba(139, 241, 230, 0.2)'
            }}
          >
            {isCorrecting ? (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, color: 'var(--muted)', gap: '1rem' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '2rem', animation: 'spin 2s linear infinite' }}>autorenew</span>
                <p>문맥과 뉘앙스를 분석하며 첨삭 중입니다...</p>
              </div>
            ) : correctionResult ? (
              <>
                <div>
                  <h4 style={{ fontSize: '0.95rem', color: 'var(--mint-deep)', marginBottom: '0.5rem' }}>자연스러운 표현</h4>
                  <p style={{ fontSize: '1.05rem', lineHeight: '1.6', whiteSpace: 'pre-wrap' }}>
                    {correctionResult.correctedText}
                  </p>
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: 'auto', paddingTop: '1.5rem', borderTop: '1px dashed var(--border)' }}>
                  <div>
                    <h4 style={{ fontSize: '0.85rem', color: 'var(--muted)', marginBottom: '0.5rem' }}>핵심 동사 (Key Verbs)</h4>
                    <div className="tag-row">
                      {correctionResult.keyVerbs.map(verb => (
                        <span key={verb} className="tag neutral" style={{ background: 'var(--surface-low)' }}>
                          {verb}
                        </span>
                      ))}
                    </div>
                  </div>
                  <div>
                    <h4 style={{ fontSize: '0.85rem', color: 'var(--muted)', marginBottom: '0.5rem' }}>주요 단어 (Vocabulary)</h4>
                    <div className="tag-row">
                      {correctionResult.keyWords.map(word => (
                        <span key={word} className="tag warm" style={{ background: 'var(--surface-low)' }}>
                          {word}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, color: 'var(--muted)', textAlign: 'center', padding: '2rem' }}>
                <span className="material-symbols-outlined" style={{ fontSize: '3rem', opacity: 0.3, marginBottom: '1rem' }}>edit_document</span>
                <p>왼쪽에 일기를 작성하고<br/>AI 첨삭을 받아보세요.</p>
              </div>
            )}
          </div>
        </section>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
        <button 
          className="button secondary" 
          disabled={!content.trim() || isCorrecting} 
          onClick={handleCorrect} 
          type="button"
          style={{ minWidth: '120px' }}
        >
          {isCorrecting ? '첨삭 중...' : 'AI 첨삭하기'}
        </button>
        <button 
          className="button primary" 
          disabled={!content.trim() || isSaving} 
          onClick={handleSave} 
          type="button"
          style={{ minWidth: '120px' }}
        >
          {isSaving ? '저장 중...' : '저장하기'}
        </button>
      </div>

      {/* History Drawer Modal */}
      {showHistory && (
        <div 
          style={{ 
            position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', 
            background: 'rgba(0, 0, 0, 0.4)', zIndex: 9999,
            display: 'flex', justifyContent: 'flex-end',
            backdropFilter: 'blur(3px)'
          }}
          onClick={() => setShowHistory(false)}
        >
          <div 
            style={{
              width: '100%', maxWidth: '420px', height: '100%',
              background: 'var(--background)',
              boxShadow: '-4px 0 25px rgba(0,0,0,0.15)',
              padding: '2rem 1.5rem',
              display: 'flex', flexDirection: 'column', gap: '1.5rem',
              overflowY: 'auto'
            }}
            onClick={(e) => e.stopPropagation()}
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
              과거에 작성했던 일기들을 피드처럼 모아봅니다. 클릭하면 해당 날짜로 돌아갑니다.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '0.5rem' }}>
              {mockHistory.map((item) => (
                <button 
                  key={item.date}
                  type="button"
                  onClick={() => {
                    handleDateChange(item.date);
                    // Mock auto-fill the content to make it feel real
                    setContent(item.snippet);
                    setCorrectionResult(null);
                    setShowHistory(false);
                    showToast(`${item.date} 의 기록을 불러왔습니다.`, 'success');
                  }}
                  style={{
                    padding: '1.25rem', borderRadius: '1rem',
                    background: 'var(--surface-low)', border: '1px solid var(--border)',
                    display: 'flex', flexDirection: 'column', gap: '0.8rem',
                    textAlign: 'left', cursor: 'pointer', transition: 'all 0.2s ease',
                    boxShadow: '0 2px 5px rgba(0,0,0,0.02)'
                  }}
                >
                  <div className="split-line" style={{ width: '100%' }}>
                    <span style={{ fontSize: '0.95rem', color: 'var(--primary)', fontWeight: 600 }}>{item.date}</span>
                    <span className="material-symbols-outlined" style={{ fontSize: '1.1rem', color: 'var(--muted)' }}>chevron_right</span>
                  </div>
                  <p style={{ fontSize: '0.95rem', lineHeight: '1.5', margin: 0, color: 'var(--text)' }}>
                    {item.snippet}
                  </p>
                  <div className="tag-row" style={{ marginTop: '0.2rem' }}>
                    {item.keyWords.map((word: string) => (
                      <span key={word} className="tag neutral" style={{ background: 'var(--surface-high)', fontSize: '0.75rem' }}>
                        {word}
                      </span>
                    ))}
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
