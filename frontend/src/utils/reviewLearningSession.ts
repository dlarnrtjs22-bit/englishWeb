const STORAGE_PREFIX = 'nativeflow-review-learning';

export interface ReviewLearningSession {
  initialCount: number;
  itemIds: string[];
}

function getKey(sessionId: string) {
  return `${STORAGE_PREFIX}:${sessionId}`;
}

export function createReviewLearningSession(session: ReviewLearningSession) {
  const sessionId = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
  sessionStorage.setItem(getKey(sessionId), JSON.stringify(session));
  return sessionId;
}

export function readReviewLearningSession(sessionId: string) {
  const raw = sessionStorage.getItem(getKey(sessionId));

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as ReviewLearningSession;
  } catch {
    sessionStorage.removeItem(getKey(sessionId));
    return null;
  }
}

export function writeReviewLearningSession(sessionId: string, session: ReviewLearningSession) {
  sessionStorage.setItem(getKey(sessionId), JSON.stringify(session));
}

export function clearReviewLearningSession(sessionId: string) {
  sessionStorage.removeItem(getKey(sessionId));
}
