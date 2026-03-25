export type ReviewResult = 'again' | 'complete' | 'easy' | 'good' | 'hard' | 'minute' | 'month' | 'year';

export interface UserProfile {
  email: string;
  id: string;
  membershipLabel: string;
  name: string;
  nativeLanguage?: string;
  role: 'admin' | 'user';
  subscriptionEndsAt?: string;
  subscriptionStatus?: string;
  targetLanguage?: string;
}

export interface AuthResponse {
  accessToken: string;
  expiresIn: number;
  refreshToken: string;
  tokenType: string;
  user: UserProfile;
}

export interface SignupResponsePayload {
  email: string;
  name: string;
  password: string;
  targetLanguage: string;
}

export interface SeriesSummary {
  badge?: string;
  categoryLabel: string;
  description: string;
  id: string;
  isSubscribed: boolean;
  packCount: number;
  progress: number;
  subtitle: string;
  thumbnailUrl: string;
  title: string;
}

export interface DashboardResponse {
  activeSeries: SeriesSummary[];
  progressMessage: string;
  progressPercent: number;
  recommendedSeries: SeriesSummary[];
  reviewSummary: {
    description: string;
    dueCount: number;
    priorityLabels: string[];
  };
  stats: Array<{
    label: string;
    value: string;
  }>;
  userName: string;
}

export interface SeriesPack {
  completed: boolean;
  description: string;
  firstItemId?: string;
  id: string;
  itemCount: number;
  locked: boolean;
  progress: number;
  statusLabel: string;
  title: string;
  unitLabel: string;
}

export interface SeriesDetailResponse {
  categoryLabel: string;
  coachNote: string;
  description: string;
  id: string;
  instructor: string;
  levelLabel: string;
  packs: SeriesPack[];
  progress: number;
  progressMessage: string;
  tags: string[];
  thumbnailUrl: string;
  title: string;
  updatedAt: string;
}

export interface LearningItemResponse {
  aiFeedback: string;
  exampleSentence: string;
  exampleTranslation: string;
  id: string;
  nuanceNote: string;
  progress: {
    current: number;
    total: number;
  };
  sourceText: string;
  targetText: string;
}

export interface CheckAnswerResponse {
  acceptedAnswers: string[];
  correctAnswer: string;
  exampleSentence: string;
  exampleTranslation: string;
  isCorrect: boolean;
}

export interface FavoriteToggleResponse {
  isFavorited: boolean;
  success: boolean;
}

export interface ReviewScheduleResponse {
  easeFactor: number;
  intervalDays: number;
  nextItemId: string | null;
  nextReviewAt: string | null;
  result: ReviewResult;
  success: boolean;
}

export interface FavoriteItem {
  itemId: string;
  packTitle: string;
  seriesTitle: string;
  sourceText: string;
  targetText: string;
}

export interface FavoritesResponse {
  items: FavoriteItem[];
}

export interface ReviewQueueItem {
  contextText?: string;
  itemId: string;
  level?: number;
  sourceText: string;
}

export interface ReviewGroup {
  description: string;
  items: ReviewQueueItem[];
  seriesId: string;
  seriesTitle: string;
}

export interface ReviewSummaryCard {
  caption: string;
  icon: string;
  label: string;
  value: string;
  variant: 'mint' | 'neutral' | 'warning';
}

export interface ReviewQueueResponse {
  groups: ReviewGroup[];
  items: ReviewQueueItem[];
  summaryCards: ReviewSummaryCard[];
  weeklyHistory: number[];
}

export interface SettingsResponse {
  accountItems: Array<{
    actionLabel: string;
    description: string;
    title: string;
  }>;
  dailyGoal: string;
  learningLevels: Array<{
    active: boolean;
    description: string;
    label: string;
  }>;
  notifications: Array<{
    description: string;
    enabled: boolean;
    title: string;
  }>;
  profile: {
    bio: string;
    email: string;
    name: string;
  };
}

export interface UpdateSettingsPayload {
  notifications: {
    dailyReminderEnabled: boolean;
    newContentEnabled: boolean;
    reviewDueEnabled: boolean;
  };
  preference: {
    dailyGoal: number;
    interfaceLanguage: string;
    level: 'advanced' | 'beginner' | 'intermediate';
    targetLanguage: string;
  };
  profile: {
    bio: string;
    name: string;
  };
}

export interface MySubscriptionResponse {
  cancelAtPeriodEnd: boolean;
  currentPeriodEnd: string;
  currentPeriodStart: string;
  daysRemaining: number;
  planName: string;
  status: string;
}

export interface BillingTransactionResponse {
  amount: string;
  currency: string;
  id: string;
  paidAt: string | null;
  provider: string;
  providerOrderId: string | null;
  status: string;
}
