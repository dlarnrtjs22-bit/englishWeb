import type { AuthResponse } from '../types/models';
import {
  clearStoredSession,
  getAccessToken,
  getRefreshToken,
  updateStoredSession,
} from '../utils/sessionStorage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';
const REFRESH_PATH = '/auth/refresh';

let refreshPromise: Promise<boolean> | null = null;

async function parseResponseBody(response: Response) {
  return response.headers.get('content-type')?.includes('application/json')
    ? await response.json()
    : null;
}

async function requestWithToken(path: string, init: RequestInit = {}, token?: string | null) {
  return fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init.headers ?? {}),
    },
  });
}

async function refreshAccessToken(): Promise<boolean> {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    clearStoredSession();
    return false;
  }

  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async () => {
    const response = await requestWithToken(REFRESH_PATH, {
      body: JSON.stringify({ refreshToken }),
      method: 'POST',
    });

    const data = await parseResponseBody(response);

    if (!response.ok || !data) {
      clearStoredSession();
      return false;
    }

    updateStoredSession(data as AuthResponse);
    return true;
  })().finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await requestWithToken(path, init, getAccessToken());
  const data = await parseResponseBody(response);

  if (response.ok) {
    return data as T;
  }

  if (response.status === 401 && path !== REFRESH_PATH) {
    const refreshed = await refreshAccessToken();

    if (refreshed) {
      const retryResponse = await requestWithToken(path, init, getAccessToken());
      const retryData = await parseResponseBody(retryResponse);

      if (retryResponse.ok) {
        return retryData as T;
      }

      throw new Error(retryData?.message ?? '인증을 다시 확인해주세요.');
    }
  }

  throw new Error(data?.message ?? '?붿껌??泥섎━?섏? 紐삵뻽?듬땲??');
}
