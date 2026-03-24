import { request } from './api';
import { createSignupResponse, mockCurrentUser } from './mockData';
import type { AuthResponse } from '../types/models';

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
  login(payload: LoginPayload) {
    return request<AuthResponse>(
      '/auth/login',
      {
        body: JSON.stringify(payload),
        method: 'POST',
      },
      async () => ({
        token: 'mock-token',
        user: {
          ...mockCurrentUser,
          email: payload.email,
        },
      }),
    );
  },
  signup(payload: SignupPayload) {
    return request<AuthResponse>(
      '/auth/signup',
      {
        body: JSON.stringify(payload),
        method: 'POST',
      },
      async () => createSignupResponse(payload),
    );
  },
};
