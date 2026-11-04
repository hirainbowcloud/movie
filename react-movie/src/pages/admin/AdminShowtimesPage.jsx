import { useEffect, useState } from 'react';
import { request, requestPage } from '../../api.js';
import Modal from '../../components/admin/Modal.jsx';
import Pager from '../../components/admin/Pager.jsx';
import FormErrors from '../../components/admin/FormErrors.jsx';

const PAGE_SIZE = 10;
const EMPTY_FORM = { movieId: '', hallId: '', showDate: '', startTime: '' };

// 場次管理：/admin/showtimes
function AdminShowtimesPage() {
  const [movies, setMovies] = useState([]);      // 下拉選單：上架中的電影
  const [halls, setHalls] = useState([]);        // 下拉選單：影廳
  const [movieFilter, setMovieFilter] = useState('');
  const [page, setPage] = useState(1);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null);  // null / 'new' / 場次物件
  const [form, setForm] = useState(EMPTY_FORM);
  const [formErrors, setFormErrors] = useState([]);
  const [reload, setReload] = useState(0);

  // 下拉選單資料只抓一次
  useEffect(() => {
    requestPage('/api/movie?page=1&size=100').then((d) => setMovies(d.content)).catch(() => {});
    requestPage('/api/hall?page=1&size=100').then((d) => setHalls(d.content)).catch(() => {});
  }, []);

  useEffect(() => {
    const filter = movieFilter ? `&movieId=${movieFilter}` : '';
    requestPage(`/api/showtime?page=${page}&size=${PAGE_SIZE}${filter}`)
      .then((d) => { setData(d); setError(''); })
      .catch((err) => setError(err.message));
  }, [page, movieFilter, reload]);

  const openNew = () => {
    setForm(EMPTY_FORM);
    setFormErrors([]);
    setEditing('new');
  };

  const openEdit = (s) => {
    setForm({
      movieId: s.movieId,
      hallId: s.hallId,
      showDate: s.showDate,
      startTime: s.startTime.slice(0, 5),   // "19:30:00" → "19:30"
    });
    setFormErrors([]);
    setEditing(s);
  };

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async (e) => {
    e.preventDefault();
    const errs = [];
    if (!form.movieId) errs.push('請選擇電影');
    if (!form.hallId) errs.push('請選擇影廳');
    if (!form.showDate) errs.push('請輸入放映日期');
    if (!form.startTime) errs.push('請輸入開演時間');
    setFormErrors(errs);
    if (errs.length > 0) return;

    const body = { ...form, movieId: Number(form.movieId), hallId: Number(form.hallId) };
    try {
      if (editing === 'new') {
        await request('/api/showtime', { method: 'POST', body });
      } else {
        await request(`/api/showtime/${editing.showtimeId}`, { method: 'PUT', body });
      }
      setEditing(null);
      setReload((n) => n + 1);
    } catch (err) {
      setFormErrors([err.message]);   // 例如：此影廳在這個時間已有場次
    }
  };

  const handleDelete = async (s) => {
    if (!window.confirm(`確定刪除 ${s.showDate} ${s.startTime.slice(0, 5)}「${s.movieTitle}」？`)) return;
    try {
      await request(`/api/showtime/${s.showtimeId}`, { method: 'DELETE' });
      setReload((n) => n + 1);
    } catch (err) {
      // 已經有人訂票的場次，資料庫外鍵會擋下刪除
      alert('刪除失敗：' + err.message);
    }
  };

  return (
    <div className="admin-page">
      <div className="page-head">
        <div>
          <h1>場次管理</h1>
          <p>安排電影在哪一廳、哪一天幾點放映</p>
        </div>
        <button className="btn btn-primary" onClick={openNew}>＋ 新增場次</button>
      </div>

      <div className="toolbar">
        <label className="field inline">
          <span>電影</span>
          <select value={movieFilter} onChange={(e) => { setMovieFilter(e.target.value); setPage(1); }}>
            <option value="">全部電影</option>
            {movies.map((m) => <option key={m.movieId} value={m.movieId}>{m.title}</option>)}
          </select>
        </label>
      </div>

      {error && <div className="alert">{error}</div>}

      <div className="panel">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>編號</th>
                <th>電影</th>
                <th>影廳</th>
                <th>放映日期</th>
                <th>開演時間</th>
                <th className="right">操作</th>
              </tr>
            </thead>
            <tbody>
              {data === null && <tr><td colSpan="6" className="empty">載入中...</td></tr>}
              {data && data.content.length === 0 && <tr><td colSpan="6" className="empty">沒有場次</td></tr>}
              {data && data.content.map((s) => (
                <tr key={s.showtimeId}>
                  <td className="mono muted">#{s.showtimeId}</td>
                  <td className="strong">{s.movieTitle}</td>
                  <td>{s.hallName}</td>
                  <td className="mono">{s.showDate}</td>
                  <td className="mono">{s.startTime.slice(0, 5)}</td>
                  <td className="right nowrap">
                    <button className="btn btn-ghost btn-sm" onClick={() => openEdit(s)}>編輯</button>
                    <button className="btn btn-ghost btn-sm danger" onClick={() => handleDelete(s)}>刪除</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}

      {editing && (
        <Modal title={editing === 'new' ? '新增場次' : `編輯場次 #${editing.showtimeId}`} onClose={() => setEditing(null)}>
          <form onSubmit={handleSave}>
            <FormErrors errors={formErrors} />
            <div className="form-grid">
              <label className="field span-2">
                <span>電影 *</span>
                <select name="movieId" value={form.movieId} onChange={onChange}>
                  <option value="">請選擇</option>
                  {movies.map((m) => <option key={m.movieId} value={m.movieId}>{m.title}（{m.releaseDate} 上映）</option>)}
                </select>
              </label>
              <label className="field span-2">
                <span>影廳 *</span>
                <select name="hallId" value={form.hallId} onChange={onChange}>
                  <option value="">請選擇</option>
                  {halls.map((h) => <option key={h.hallId} value={h.hallId}>{h.hallName}</option>)}
                </select>
              </label>
              <label className="field">
                <span>放映日期 *</span>
                <input name="showDate" type="date" value={form.showDate} onChange={onChange} />
              </label>
              <label className="field">
                <span>開演時間 *</span>
                <input name="startTime" type="time" value={form.startTime} onChange={onChange} />
              </label>
            </div>
            <p className="form-hint">放映日期不能早於電影上映日期；同一廳同一時間只能有一個場次。</p>
            <div className="form-actions">
              <button type="button" className="btn btn-ghost" onClick={() => setEditing(null)}>取消</button>
              <button className="btn btn-primary">儲存</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

export default AdminShowtimesPage;
