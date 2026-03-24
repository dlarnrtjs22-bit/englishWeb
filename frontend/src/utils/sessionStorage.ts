import type { AuthResponse, UserProfile } from '../types/models';

const STORAGE_KEY = 'nativeflow-session';

interface StoredSession {
  accessToken: string;
  refreshToken: string;
  user: UserProfile;
}

export function readStoredSession(): StoredSession | null {
  const raw = localStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as StoredSession;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function writeStoredSession(session: AuthResponse) {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      accessToken: session.accessToken,
      refreshToken: session.refreshToken,
      user: session.user,
    }),
  );
}

export function clearStoredSession() {
  localStorage.removeItem(STORAGE_KEY);
}

export function getAccessToken() {
  return readStoredSession()?.accessToken ?? null;
}
