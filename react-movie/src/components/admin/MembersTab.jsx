import { useEffect, useState } from 'react';
import { request, requestPage } from '../../api.js';
import Modal from './Modal.jsx';
import Pager from './Pager.jsx';
import FormErrors from './FormErrors.jsx';

// 會員管理：搜尋、修改、刪除
function MembersTab() {
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');      // 按下搜尋才生效
  const [page, setPage] = useState(1);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: '', phone: '', email: '', password: '' });
  const [formErrors, setFormErrors] = useState([]);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const url = keyword
      ? `/api/member/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`
      : `/api/member?page=${page}&size=10`;
    requestPage(url)
      .then((d) => { setData(d); setError(''); })
      .catch((err) => setError(err.message));
  }, [keyword, page, reload]);

  const handleSearch = (e) => {
    e.preventDefault();
    setKeyword(keywordInput.trim());
    setPage(1);
  };

  const open = (m) => {
    setForm({ name: m.name, phone: m.phone, email: m.email, password: '' });
    setFormErrors([]);
    setEditing(m);
  };

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async (e) => {
    e.preventDefault();
    const errs = [];
    if (!form.name.trim()) errs.push('請輸入姓名');
    if (!/^09\d{8}$/.test(form.phone)) errs.push('手機格式為 09 開頭共 10 碼');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) errs.push('Email 格式不正確');
    if (form.password && form.password.length < 6) errs.push('新密碼至少 6 個字');
    setFormErrors(errs);
    if (errs.length > 0) return;

    try {
      // 密碼空白 = 不改密碼（後端 MemberService.update 的規則）
      await request(`/api/member/${editing.memberId}`, { method: 'PUT', body: form });
      setEditing(null);
      setReload((n) => n + 1);
    } catch (err) {
      setFormErrors([err.message]);
    }
  };

  const handleDelete = async (m) => {
    if (!window.confirm(`確定刪除會員「${m.name}（${m.username}）」？`)) return;
    try {
      await request(`/api/member/${m.memberId}`, { method: 'DELETE' });
      setReload((n) => n + 1);
    } catch (err) {
      alert('刪除失敗（有訂單的會員不能刪除）：' + err.message);
    }
  };

  return (
    <>
      <form className="toolbar" onSubmit={handleSearch}>
        <input
          className="search-input"
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
          placeholder="搜尋帳號、姓名、電話或 Email"
        />
        <button className="btn btn-primary btn-sm">搜尋</button>
        {keyword && (
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => { setKeyword(''); setKeywordInput(''); setPage(1); }}>
            清除
          </button>
        )}
      </form>

      {error && <div className="alert">{error}</div>}

      <div className="panel">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>編號</th><th>帳號</th><th>姓名</th><th>手機</th><th>Email</th><th className="right">操作</th></tr>
            </thead>
            <tbody>
              {data === null && <tr><td colSpan="6" className="empty">載入中...</td></tr>}
              {data && data.content.length === 0 && <tr><td colSpan="6" className="empty">找不到會員</td></tr>}
              {data && data.content.map((m) => (
                <tr key={m.memberId}>
                  <td className="mono muted">#{m.memberId}</td>
                  <td className="mono">{m.username}</td>
                  <td className="strong">{m.name}</td>
                  <td className="mono">{m.phone}</td>
                  <td>{m.email}</td>
                  <td className="right nowrap">
                    <button className="btn btn-ghost btn-sm" onClick={() => open(m)}>編輯</button>
                    <button className="btn btn-ghost btn-sm danger" onClick={() => handleDelete(m)}>刪除</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}

      {editing && (
        <Modal title={`編輯會員：${editing.username}`} onClose={() => setEditing(null)}>
          <form onSubmit={handleSave}>
            <FormErrors errors={formErrors} />
            <div className="form-grid">
              <label className="field">
                <span>姓名 *</span>
                <input name="name" value={form.name} onChange={onChange} />
              </label>
              <label className="field">
                <span>手機 *</span>
                <input name="phone" value={form.phone} onChange={onChange} />
              </label>
              <label className="field span-2">
                <span>Email *</span>
                <input name="email" value={form.email} onChange={onChange} />
              </label>
              <label className="field span-2">
                <span>新密碼（不改請留空）</span>
                <input name="password" type="password" value={form.password} onChange={onChange} />
              </label>
            </div>
            <div className="form-actions">
              <button type="button" className="btn btn-ghost" onClick={() => setEditing(null)}>取消</button>
              <button className="btn btn-primary">儲存</button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}

export default MembersTab;
