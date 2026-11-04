import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { API, readError, saveMember } from '../api.js';
import '../css/Login.css';

const EMPTY_REGISTER = { username: '', password: '', confirm: '', name: '', phone: '', email: '' };

// 登入 / 註冊：/login
function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  // 從哪一頁被導過來（例如訂票頁），登入後回到那一頁；沒有就回首頁
  const from = location.state?.from || '/';

  const [mode, setMode] = useState('login');   // 'login' 或 'register'
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [regForm, setRegForm] = useState(EMPTY_REGISTER);
  const [errors, setErrors] = useState([]);
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);

  const switchMode = (m) => {
    setMode(m);
    setErrors([]);
    setMessage('');
  };

  // ---------- 登入 ----------
  const handleLogin = (e) => {
    e.preventDefault();   // 不要讓瀏覽器整頁重新整理

    // 前端驗證
    const errs = [];
    if (!loginForm.username.trim()) errs.push('請輸入帳號');
    if (!loginForm.password) errs.push('請輸入密碼');
    setErrors(errs);
    if (errs.length > 0) return;

    setSending(true);
    fetch(`${API}/api/member/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: loginForm.username.trim(), password: loginForm.password }),
    })
      .then(async (res) => {
        if (!res.ok) throw new Error(await readError(res));
        return res.json();
      })
      .then((member) => {
        saveMember(member);                    // 記住登入的會員
        navigate(from, { replace: true });
      })
      .catch((err) => setErrors([err.message]))
      .finally(() => setSending(false));
  };

  // ---------- 註冊 ----------
  const validateRegister = () => {
    const f = regForm;
    const errs = [];
    if (!/^[A-Za-z0-9_]{4,20}$/.test(f.username)) errs.push('帳號需為 4～20 個英文、數字或底線');
    if (f.password.length < 6) errs.push('密碼至少 6 個字');
    if (f.password !== f.confirm) errs.push('兩次輸入的密碼不一樣');
    if (!f.name.trim()) errs.push('請輸入姓名');
    if (!/^09\d{8}$/.test(f.phone)) errs.push('手機格式為 09 開頭共 10 碼');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) errs.push('Email 格式不正確');
    return errs;
  };

  const handleRegister = (e) => {
    e.preventDefault();
    const errs = validateRegister();
    setErrors(errs);
    if (errs.length > 0) return;

    const body = {
      username: regForm.username,
      password: regForm.password,
      name: regForm.name.trim(),
      phone: regForm.phone,
      email: regForm.email.trim(),
    };

    setSending(true);
    fetch(`${API}/api/member`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
      .then(async (res) => {
        if (res.status === 409) throw new Error('這個帳號已經有人使用');
        if (!res.ok) throw new Error(await readError(res));
        return res.json();
      })
      .then((member) => {
        // 註冊成功：切回登入，帳號先幫忙填好
        setLoginForm({ username: member.username, password: '' });
        setRegForm(EMPTY_REGISTER);
        setMode('login');
        setErrors([]);
        setMessage('註冊成功，請登入');
      })
      .catch((err) => setErrors([err.message]))
      .finally(() => setSending(false));
  };

  // 輸入框共用：name 對應 state 裡的欄位
  const onLoginChange = (e) => setLoginForm({ ...loginForm, [e.target.name]: e.target.value });
  const onRegChange = (e) => setRegForm({ ...regForm, [e.target.name]: e.target.value });

  return (
    <div className="login">
      <div className="login-card">
        <div className="login-tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => switchMode('login')}>登入</button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => switchMode('register')}>註冊</button>
        </div>

        {message && <p className="login-message">{message}</p>}
        {errors.length > 0 && (
          <ul className="login-errors">
            {errors.map((msg) => <li key={msg}>{msg}</li>)}
          </ul>
        )}

        {mode === 'login' ? (
          <form onSubmit={handleLogin}>
            <label>帳號<input name="username" value={loginForm.username} onChange={onLoginChange} /></label>
            <label>密碼<input name="password" type="password" value={loginForm.password} onChange={onLoginChange} /></label>
            <button className="login-submit" disabled={sending}>{sending ? '登入中...' : '登入'}</button>
          </form>
        ) : (
          <form onSubmit={handleRegister}>
            <label>帳號<input name="username" value={regForm.username} onChange={onRegChange} /></label>
            <label>密碼<input name="password" type="password" value={regForm.password} onChange={onRegChange} /></label>
            <label>確認密碼<input name="confirm" type="password" value={regForm.confirm} onChange={onRegChange} /></label>
            <label>姓名<input name="name" value={regForm.name} onChange={onRegChange} /></label>
            <label>手機<input name="phone" value={regForm.phone} onChange={onRegChange} placeholder="0912345678" /></label>
            <label>Email<input name="email" value={regForm.email} onChange={onRegChange} /></label>
            <button className="login-submit" disabled={sending}>{sending ? '送出中...' : '註冊'}</button>
          </form>
        )}
      </div>
    </div>
  );
}

export default LoginPage;
