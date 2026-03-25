import {
  createContext,
  startTransition,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authService, type LoginPayload, type SignupPayload } from '../services/authService';
import type { AuthResponse, UserProfile } from '../types/models';
import { clearStoredSession, readStoredSession, writeStoredSession } from '../utils/sessionStorage';

interface AuthContextValue {
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (payload: LoginPayload, rememberMe: boolean) => Promise<void>;
  logout: () => Promise<void>;
  signup: (payload: SignupPayload) => Promise<void>;
  user: UserProfile | null;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    const bootstrap = async () => {
      const storedSession = readStoredSession();

      if (!storedSession) {
        setIsInitializing(false);
        return;
      }

      try {
        const me = await authService.getMe();
        setUser(me);
      } catch {
        clearStoredSession();
        setUser(null);
      } finally {
        setIsInitializing(false);
      }
    };

    void bootstrap();
  }, []);

  const syncSession = (response: AuthResponse, rememberMe: boolean) => {
    writeStoredSession(response, rememberMe);
    startTransition(() => {
      setUser(response.user);
    });
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: Boolean(user),
      isInitializing,
      async login(payload, rememberMe) {
        const response = await authService.login(payload);
        syncSession(response, rememberMe);
      },
      async logout() {
        try {
          await authService.logout();
        } finally {
          clearStoredSession();
          startTransition(() => {
            setUser(null);
          });
        }
      },
      async signup(payload) {
        const response = await authService.signup(payload);
        syncSession(response, true);
      },
      user,
    }),
    [isInitializing, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}
