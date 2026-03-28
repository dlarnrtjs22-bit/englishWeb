import { apiRequest } from './api';
import type { AuthResponse, UserProfile } from '../types/models';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface SignupPayload {
  email: string;
  name: string;
  password: string;
  targetLanguage: string;
}

export const authService = {
  getMe() {
    return apiRequest<UserProfile>('/auth/me', { method: 'GET' });
  },
  refresh(refreshToken: string) {
    return apiRequest<AuthResponse>('/auth/refresh', {
      body: JSON.stringify({ refreshToken }),
      method: 'POST',
    });
  },
  login(payload: LoginPayload) {
    return apiRequest<AuthResponse>('/auth/login', {
      body: JSON.stringify(payload),
      method: 'POST',
    });
  },
  logout() {
    return apiRequest<{ success: boolean }>('/auth/logout', {
      method: 'POST',
    });
  },
  signup(payload: SignupPayload) {
    return apiRequest<AuthResponse>('/auth/signup', {
      body: JSON.stringify(payload),
      method: 'POST',
    });
  },
};
