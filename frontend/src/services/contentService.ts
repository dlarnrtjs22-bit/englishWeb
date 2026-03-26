import { apiRequest } from './api';
import type {
  CheckAnswerResponse,
  BillingTransactionResponse,
  DashboardResponse,
  FavoriteToggleResponse,
  FavoritesResponse,
  LearningItemResponse,
  MySubscriptionResponse,
  ReviewQueueResponse,
  ReviewResult,
  ReviewScheduleResponse,
  SentenceFeedbackResponse,
  SeriesDetailResponse,
  SeriesSummary,
  SettingsResponse,
  UpdateSettingsPayload,
} from '../types/models';

export const contentService = {
  getDashboard() {
    return apiRequest<DashboardResponse>('/dashboard', { method: 'GET' });
  },
  getFavorites() {
    return apiRequest<FavoritesResponse>('/favorites', { method: 'GET' });
  },
  getLearningItem(itemId: string, mode = 'study') {
    const params = new URLSearchParams({ mode });
    return apiRequest<LearningItemResponse>(`/learning-items/${itemId}?${params.toString()}`, { method: 'GET' });
  },
  checkAnswer(itemId: string, typedAnswer: string, mode = 'study') {
    return apiRequest<CheckAnswerResponse>(`/learning-items/${itemId}/check-answer`, {
      body: JSON.stringify({ mode, typedAnswer }),
      method: 'POST',
    });
  },
  getSentenceFeedback(itemId: string, sentence: string, mode = 'study') {
    return apiRequest<SentenceFeedbackResponse>(`/learning-items/${itemId}/sentence-feedback`, {
      body: JSON.stringify({ mode, sentence }),
      method: 'POST',
    });
  },
  favoriteItem(itemId: string) {
    return apiRequest<FavoriteToggleResponse>(`/learning-items/${itemId}/favorite`, { method: 'POST' });
  },
  unfavoriteItem(itemId: string) {
    return apiRequest<FavoriteToggleResponse>(`/learning-items/${itemId}/favorite`, { method: 'DELETE' });
  },
  getReviewQueue() {
    return apiRequest<ReviewQueueResponse>('/reviews/queue', { method: 'GET' });
  },
  getStudyPackQueue(packId: string) {
    return apiRequest<{ itemIds: string[] }>(`/packs/${packId}/study-queue`, { method: 'GET' });
  },
  getRandomPackItem(packId: string) {
    return apiRequest<{ itemId: string }>(`/packs/${packId}/random-item`, { method: 'GET' });
  },
  getRandomPackQueue(packId: string) {
    return apiRequest<{ itemIds: string[] }>(`/packs/${packId}/random-queue`, { method: 'GET' });
  },
  resetLearningItem(itemId: string) {
    return apiRequest<{ success: boolean }>(`/learning-items/${itemId}/reset`, { method: 'POST' });
  },
  submitReview(itemId: string, result: ReviewResult, mode = 'study') {
    return apiRequest<ReviewScheduleResponse>(`/learning-items/${itemId}/review`, {
      body: JSON.stringify({ mode, result }),
      method: 'POST',
    });
  },
  getSeriesDetail(seriesId: string) {
    return apiRequest<SeriesDetailResponse>(`/series/${seriesId}`, { method: 'GET' });
  },
  getSeriesList() {
    return apiRequest<SeriesSummary[]>('/series', { method: 'GET' });
  },
  getSettings() {
    return apiRequest<SettingsResponse>('/settings', { method: 'GET' });
  },
  getMySubscription() {
    return apiRequest<MySubscriptionResponse>('/me/subscription', { method: 'GET' });
  },
  getBillingTransactions() {
    return apiRequest<BillingTransactionResponse[]>('/me/billing-transactions', { method: 'GET' });
  },
  updateSettings(payload: UpdateSettingsPayload) {
    return apiRequest<SettingsResponse>('/settings', {
      body: JSON.stringify(payload),
      method: 'PATCH',
    });
  },
};
