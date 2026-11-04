import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { request, saveAdmin } from '../../api.js';
import FormErrors from '../../components/admin/FormErrors.jsx';
import '../../css/Admin.css';

// 管理者登入：/admin/login
function AdminLoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
  const [errors, setErrors] = useState([]);
  const [sending, setSending] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = [];
    if (!form.username.trim()) errs.push('請輸入帳號');
    if (!form.password) errs.push('請輸入密碼');
    setErrors(errs);
    if (errs.length > 0) return;

    setSending(true);
    try {
      const admin = await request('/api/admin/login', {
        method: 'POST',
        body: { username: form.username.trim(), password: form.password },
      });
      saveAdmin(admin);
      navigate('/admin/movies', { replace: true });
    } catch (err) {
      setErrors([err.message]);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="admin-login">
      <form className="admin-login-card" onSubmit={handleSubmit}>
        <img className="admin-login-logo" src="/logo.svg" alt="" />
        <h2>MOVIE POS 後台</h2>
        <p className="admin-login-sub">請使用管理者帳號登入</p>

        <FormErrors errors={errors} />

        <label className="field">
          <span>帳號</span>
          <input name="username" value={form.username} onChange={onChange} autoFocus autoComplete="username" />
        </label>
        <label className="field">
          <span>密碼</span>
          <input name="password" type="password" value={form.password} onChange={onChange} autoComplete="current-password" />
        </label>

        <button className="btn admin-login-submit" disabled={sending}>
          {sending ? '登入中...' : '登入'}
        </button>
      </form>

      <p className="admin-login-foot">
        僅限內部人員使用・<a href="/">回到前台</a>
      </p>
    </div>
  );
}

export default AdminLoginPage;
