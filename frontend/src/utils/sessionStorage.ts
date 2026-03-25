import type { AuthResponse, UserProfile } from '../types/models';

const STORAGE_KEY = 'nativeflow-session';

interface StoredSession {
  accessToken: string;
  refreshToken: string;
  user: UserProfile;
}

function parseStoredSession(raw: string | null): StoredSession | null {
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as StoredSession;
  } catch {
    return null;
  }
}

export function readStoredSession(): StoredSession | null {
  const local = parseStoredSession(localStorage.getItem(STORAGE_KEY));

  if (local) {
    return local;
  }

  const session = parseStoredSession(sessionStorage.getItem(STORAGE_KEY));

  if (session) {
    return session;
  }

  localStorage.removeItem(STORAGE_KEY);
  sessionStorage.removeItem(STORAGE_KEY);
  return null;
}

export function writeStoredSession(session: AuthResponse, rememberMe: boolean) {
  const payload = JSON.stringify({
    accessToken: session.accessToken,
    refreshToken: session.refreshToken,
    user: session.user,
  });

  if (rememberMe) {
    localStorage.setItem(STORAGE_KEY, payload);
    sessionStorage.removeItem(STORAGE_KEY);
    return;
  }

  sessionStorage.setItem(STORAGE_KEY, payload);
  localStorage.removeItem(STORAGE_KEY);
}

export function clearStoredSession() {
  localStorage.removeItem(STORAGE_KEY);
  sessionStorage.removeItem(STORAGE_KEY);
}

export function getAccessToken() {
  return readStoredSession()?.accessToken ?? null;
}
