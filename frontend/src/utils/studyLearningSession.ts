const STORAGE_PREFIX = 'nativeflow-study-learning';

export interface StudyLearningSession {
  initialCount: number;
  itemIds: string[];
  packId: string;
  seriesId: string;
}

function getKey(sessionId: string) {
  return `${STORAGE_PREFIX}:${sessionId}`;
}

export function createStudyLearningSession(session: StudyLearningSession) {
  const sessionId = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
  sessionStorage.setItem(getKey(sessionId), JSON.stringify(session));
  return sessionId;
}

export function readStudyLearningSession(sessionId: string) {
  const raw = sessionStorage.getItem(getKey(sessionId));

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as StudyLearningSession;
  } catch {
    sessionStorage.removeItem(getKey(sessionId));
    return null;
  }
}

export function writeStudyLearningSession(sessionId: string, session: StudyLearningSession) {
  sessionStorage.setItem(getKey(sessionId), JSON.stringify(session));
}

export function clearStudyLearningSession(sessionId: string) {
  sessionStorage.removeItem(getKey(sessionId));
}
