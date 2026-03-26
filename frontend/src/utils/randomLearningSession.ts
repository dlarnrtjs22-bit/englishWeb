const STORAGE_PREFIX = 'nativeflow-random-learning';

export interface RandomLearningSession {
  itemIds: string[];
  packId: string;
  seriesId: string;
}

function getKey(sessionId: string) {
  return `${STORAGE_PREFIX}:${sessionId}`;
}

export function createRandomLearningSession(session: RandomLearningSession) {
  const sessionId = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
  sessionStorage.setItem(getKey(sessionId), JSON.stringify(session));
  return sessionId;
}

export function readRandomLearningSession(sessionId: string) {
  const raw = sessionStorage.getItem(getKey(sessionId));

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as RandomLearningSession;
  } catch {
    sessionStorage.removeItem(getKey(sessionId));
    return null;
  }
}

export function clearRandomLearningSession(sessionId: string) {
  sessionStorage.removeItem(getKey(sessionId));
}
