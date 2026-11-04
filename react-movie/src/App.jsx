import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import FrontLayout from './components/FrontLayout.jsx';
import HomePage from './pages/HomePage.jsx';
import MovieDetailPage from './pages/MovieDetailPage.jsx';
import BookingPage from './pages/BookingPage.jsx';
import OrdersPage from './pages/OrdersPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import AdminLayout from './components/admin/AdminLayout.jsx';
import AdminLoginPage from './pages/admin/AdminLoginPage.jsx';
import AdminMoviesPage from './pages/admin/AdminMoviesPage.jsx';
import AdminShowtimesPage from './pages/admin/AdminShowtimesPage.jsx';
import AdminSettingsPage from './pages/admin/AdminSettingsPage.jsx';
import AdminOrdersPage from './pages/admin/AdminOrdersPage.jsx';
import AdminReportsPage from './pages/admin/AdminReportsPage.jsx';
import './css/common.css';

// 網址 → 頁面 的對照表
function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 前台：購票人（共用上方導覽列） */}
        <Route element={<FrontLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/movie/:movieId" element={<MovieDetailPage />} />
          <Route path="/booking/:showtimeId" element={<BookingPage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/login" element={<LoginPage />} />
        </Route>

        {/* 後台：管理者 */}
        <Route path="/admin/login" element={<AdminLoginPage />} />
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/movies" replace />} />
          <Route path="movies" element={<AdminMoviesPage />} />
          <Route path="showtimes" element={<AdminShowtimesPage />} />
          <Route path="settings" element={<AdminSettingsPage />} />
          <Route path="orders" element={<AdminOrdersPage />} />
          <Route path="reports" element={<AdminReportsPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
