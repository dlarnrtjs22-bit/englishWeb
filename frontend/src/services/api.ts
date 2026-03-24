const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

export async function request<T>(
  path: string,
  init: RequestInit,
  fallback: () => T | Promise<T>,
): Promise<T> {
  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        ...(init.headers ?? {}),
      },
    });

    if (!response.ok) {
      throw new Error(`${response.status} 응답을 받았습니다.`);
    }

    return (await response.json()) as T;
  } catch {
    return fallback();
  }
}
