import { request } from './api';
import {
  getDashboardFallback,
  getFavoritesFallback,
  getLearningItemFallback,
  getReviewQueueFallback,
  getSeriesDetailFallback,
  getSeriesListFallback,
  getSettingsFallback,
} from './mockData';
import type {
  DashboardResponse,
  FavoritesResponse,
  LearningItemResponse,
  ReviewQueueResponse,
  SeriesDetailResponse,
  SeriesSummary,
  SettingsResponse,
} from '../types/models';

export const contentService = {
  getDashboard() {
    return request<DashboardResponse>('/dashboard', { method: 'GET' }, getDashboardFallback);
  },
  getFavorites() {
    return request<FavoritesResponse>('/favorites', { method: 'GET' }, getFavoritesFallback);
  },
  getLearningItem(itemId: string) {
    return request<LearningItemResponse>(
      `/learning-items/${itemId}`,
      { method: 'GET' },
      () => getLearningItemFallback(itemId),
    );
  },
  getReviewQueue() {
    return request<ReviewQueueResponse>('/reviews/queue', { method: 'GET' }, getReviewQueueFallback);
  },
  getSeriesDetail(seriesId: string) {
    return request<SeriesDetailResponse>(
      `/series/${seriesId}`,
      { method: 'GET' },
      () => getSeriesDetailFallback(seriesId),
    );
  },
  getSeriesList() {
    return request<SeriesSummary[]>('/series', { method: 'GET' }, getSeriesListFallback);
  },
  getSettings() {
    return request<SettingsResponse>('/settings', { method: 'GET' }, getSettingsFallback);
  },
};
