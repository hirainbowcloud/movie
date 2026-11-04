import { Link, useLocation, useNavigate } from 'react-router-dom';
import { getMember, clearMember } from '../api.js';
import '../css/Navbar.css';

// 每一頁最上方的導覽列
function Navbar() {
  useLocation();                 // 換頁時讓導覽列重新顯示，才會讀到最新的登入狀態
  const navigate = useNavigate();
  const member = getMember();

  const handleLogout = () => {
    clearMember();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        <img src="/logo.svg" alt="" className="navbar-logo" />
        MOVIE POS
      </Link>
      <div className="navbar-links">
        <Link to="/orders">我的訂單</Link>
        {member ? (
          <>
            <span className="navbar-user">{member.name}</span>
            <button className="navbar-logout" onClick={handleLogout}>登出</button>
          </>
        ) : (
          <Link to="/login">登入</Link>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
