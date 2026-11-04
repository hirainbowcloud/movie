import { Outlet } from 'react-router-dom';
import Navbar from './Navbar.jsx';

// 前台版型：上面導覽列，下面放各頁內容（<Outlet /> 是子頁面要顯示的位置）
function FrontLayout() {
  return (
    <>
      <Navbar />
      <Outlet />
    </>
  );
}

export default FrontLayout;
