import { apiRequest } from './api';
import type {
  DiaryCalendarResponse,
  DiaryEntryResponse,
  DiaryFeedbackResponse,
  DiaryHistoryResponse,
} from '../types/models';

export const diaryService = {
  getEntry(entryDate: string) {
    return apiRequest<DiaryEntryResponse>(`/diary/entries/${entryDate}`, { method: 'GET' });
  },
  getCalendar(from: string, to: string) {
    const params = new URLSearchParams({ from, to });
    return apiRequest<DiaryCalendarResponse>(`/diary/calendar?${params.toString()}`, { method: 'GET' });
  },
  getHistory(limit = 50) {
    return apiRequest<DiaryHistoryResponse>(`/diary/history?limit=${limit}`, { method: 'GET' });
  },
  getFeedback(entryDate: string, rawContent: string) {
    return apiRequest<DiaryFeedbackResponse>('/diary/entries/feedback', {
      body: JSON.stringify({ entryDate, rawContent }),
      method: 'POST',
    });
  },
  saveEntry(entryDate: string, rawContent: string, feedback: DiaryFeedbackResponse | null) {
    return apiRequest<DiaryEntryResponse>(`/diary/entries/${entryDate}`, {
      body: JSON.stringify({ rawContent, feedback }),
      method: 'PUT',
    });
  },
};
