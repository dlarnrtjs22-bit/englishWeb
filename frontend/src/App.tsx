import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './app/AuthContext';
import { ProtectedRoute } from './app/ProtectedRoute';
import { ToastProvider } from './app/ToastContext';
import { MainLayout } from './layouts/MainLayout';
import { DashboardPage } from './pages/DashboardPage';
import { FavoritesPage } from './pages/FavoritesPage';
import { LearningPage } from './pages/LearningPage';
import { LoginPage } from './pages/LoginPage';
import { MySeriesPage } from './pages/MySeriesPage';
import { ReviewQueuePage } from './pages/ReviewQueuePage';
import { SeriesDetailPage } from './pages/SeriesDetailPage';
import { SettingsPage } from './pages/SettingsPage';
import { SignupPage } from './pages/SignupPage';

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route
              element={
                <ProtectedRoute>
                  <MainLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/my-series" element={<MySeriesPage />} />
              <Route path="/series/:seriesId" element={<SeriesDetailPage />} />
              <Route path="/learning/:itemId" element={<LearningPage />} />
              <Route path="/favorites" element={<FavoritesPage />} />
              <Route path="/reviews" element={<ReviewQueuePage />} />
              <Route path="/settings" element={<SettingsPage />} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}
