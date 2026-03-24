interface StatePanelProps {
  actionLabel?: string;
  message: string;
  onAction?: () => void;
  title: string;
}

export function LoadingPanel({ message = '화면을 준비하고 있습니다.' }: { message?: string }) {
  return (
    <section className="state-panel">
      <div className="loading-dot" />
      <div>
        <h2>불러오는 중</h2>
        <p>{message}</p>
      </div>
    </section>
  );
}

export function ErrorPanel({ actionLabel, message, onAction, title }: StatePanelProps) {
  return (
    <section className="state-panel error-panel">
      <div>
        <h2>{title}</h2>
        <p>{message}</p>
      </div>
      {actionLabel && onAction ? (
        <button className="button tertiary" onClick={onAction} type="button">
          {actionLabel}
        </button>
      ) : null}
    </section>
  );
}
