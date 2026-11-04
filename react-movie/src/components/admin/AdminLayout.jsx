import { NavLink, Navigate, Outlet, useNavigate } from 'react-router-dom';
import { getAdmin, clearAdmin } from '../../api.js';
import '../../css/Admin.css';

// 左側選單的項目
const MENU = [
  { to: '/admin/movies', text: '電影管理' },
  { to: '/admin/showtimes', text: '場次管理' },
  { to: '/admin/settings', text: '影廳 / 票種 / 會員' },
  { to: '/admin/orders', text: '訂單查詢' },
  { to: '/admin/reports', text: '銷售報表' },
];

// 後台版型：左邊選單、右邊各頁內容
function AdminLayout() {
  const navigate = useNavigate();
  const admin = getAdmin();

  // 沒登入 → 導到管理者登入頁
  if (!admin) return <Navigate to="/admin/login" replace />;

  const handleLogout = () => {
    clearAdmin();
    navigate('/admin/login');
  };

  return (
    <div className="admin">
      <aside className="admin-side">
        <div className="admin-brand">
          <img src="/logo.svg" alt="" />
          <span>MOVIE POS<small>後台管理</small></span>
        </div>

        <nav className="admin-menu">
          {MENU.map((m) => (
            // NavLink 會在目前頁面自動加上 active 的 class
            <NavLink key={m.to} to={m.to}>{m.text}</NavLink>
          ))}
        </nav>

        <div className="admin-user">
          <div className="admin-user-name">{admin.name || admin.username}</div>
          <div className="admin-user-actions">
            <a href="/" target="_blank" rel="noreferrer">前台</a>
            <button onClick={handleLogout}>登出</button>
          </div>
        </div>
      </aside>

      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}

export default AdminLayout;
