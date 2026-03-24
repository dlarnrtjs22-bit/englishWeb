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
import type { UserProfile } from '../types/models';

interface AuthContextValue {
  isAuthenticated: boolean;
  user: UserProfile | null;
  login: (payload: LoginPayload) => Promise<void>;
  logout: () => void;
  signup: (payload: SignupPayload) => Promise<void>;
}

const STORAGE_KEY = 'nativeflow-session';
const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredUser(): UserProfile | null {
  const raw = localStorage.getItem(STORAGE_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as UserProfile;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);

  useEffect(() => {
    setUser(readStoredUser());
  }, []);

  const syncUser = (nextUser: UserProfile | null) => {
    if (nextUser) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextUser));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }

    startTransition(() => {
      setUser(nextUser);
    });
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: Boolean(user),
      user,
      async login(payload) {
        const response = await authService.login(payload);
        syncUser(response.user);
      },
      logout() {
        syncUser(null);
      },
      async signup(payload) {
        const response = await authService.signup(payload);
        syncUser(response.user);
      },
    }),
    [user],
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
