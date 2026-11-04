import { useEffect, useState } from 'react';
import { request, requestPage } from '../../api.js';
import Modal from './Modal.jsx';
import Pager from './Pager.jsx';
import FormErrors from './FormErrors.jsx';

// 座位總數：第一排 n 個，之後每排多 1 個（和後端 SeatRule 相同）
const totalSeats = (rows, first) => rows * first + (rows * (rows - 1)) / 2;
const EMPTY = { hallName: '', rowCount: 10, firstRowSeats: 8 };

// 影廳管理
function HallsTab() {
  const [page, setPage] = useState(1);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [formErrors, setFormErrors] = useState([]);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    requestPage(`/api/hall?page=${page}&size=10`)
      .then((d) => { setData(d); setError(''); })
      .catch((err) => setError(err.message));
  }, [page, reload]);

  const open = (hall) => {
    setForm(hall ? { hallName: hall.hallName, rowCount: hall.rowCount, firstRowSeats: hall.firstRowSeats } : EMPTY);
    setFormErrors([]);
    setEditing(hall || 'new');
  };

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async (e) => {
    e.preventDefault();
    const errs = [];
    if (!form.hallName.trim()) errs.push('請輸入廳名');
    if (form.hallName.length > 20) errs.push('廳名最多 20 字');
    if (Number(form.rowCount) < 10) errs.push('排數至少 10 排');
    if (Number(form.firstRowSeats) < 1) errs.push('第一排座位數至少 1 個');
    setFormErrors(errs);
    if (errs.length > 0) return;

    const body = { hallName: form.hallName.trim(), rowCount: Number(form.rowCount), firstRowSeats: Number(form.firstRowSeats) };
    try {
      if (editing === 'new') await request('/api/hall', { method: 'POST', body });
      else await request(`/api/hall/${editing.hallId}`, { method: 'PUT', body });
      setEditing(null);
      setReload((n) => n + 1);
    } catch (err) {
      setFormErrors([err.message]);
    }
  };

  const handleDelete = async (h) => {
    if (!window.confirm(`確定刪除「${h.hallName}」？`)) return;
    try {
      await request(`/api/hall/${h.hallId}`, { method: 'DELETE' });
      setReload((n) => n + 1);
    } catch (err) {
      alert('刪除失敗（已有場次的影廳不能刪除）：' + err.message);
    }
  };

  return (
    <>
      <div className="tab-head">
        <p>影廳的排數與第一排座位數，決定座位圖的大小（每往後一排多 1 個座位）</p>
        <button className="btn btn-primary btn-sm" onClick={() => open(null)}>＋ 新增影廳</button>
      </div>

      {error && <div className="alert">{error}</div>}

      <div className="panel">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>編號</th><th>廳名</th><th>排數</th><th>第一排座位</th><th>座位總數</th><th className="right">操作</th></tr>
            </thead>
            <tbody>
              {data === null && <tr><td colSpan="6" className="empty">載入中...</td></tr>}
              {data && data.content.length === 0 && <tr><td colSpan="6" className="empty">還沒有影廳</td></tr>}
              {data && data.content.map((h) => (
                <tr key={h.hallId}>
                  <td className="mono muted">#{h.hallId}</td>
                  <td className="strong">{h.hallName}</td>
                  <td>{h.rowCount} 排</td>
                  <td>{h.firstRowSeats} 個</td>
                  <td>{totalSeats(h.rowCount, h.firstRowSeats)} 位</td>
                  <td className="right nowrap">
                    <button className="btn btn-ghost btn-sm" onClick={() => open(h)}>編輯</button>
                    <button className="btn btn-ghost btn-sm danger" onClick={() => handleDelete(h)}>刪除</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}

      {editing && (
        <Modal title={editing === 'new' ? '新增影廳' : `編輯：${editing.hallName}`} onClose={() => setEditing(null)}>
          <form onSubmit={handleSave}>
            <FormErrors errors={formErrors} />
            <div className="form-grid">
              <label className="field span-2">
                <span>廳名 *</span>
                <input name="hallName" value={form.hallName} onChange={onChange} placeholder="第1廳" />
              </label>
              <label className="field">
                <span>排數 *（至少 10）</span>
                <input name="rowCount" type="number" min="10" value={form.rowCount} onChange={onChange} />
              </label>
              <label className="field">
                <span>第一排座位數 *</span>
                <input name="firstRowSeats" type="number" min="1" value={form.firstRowSeats} onChange={onChange} />
              </label>
            </div>
            <p className="form-hint">
              座位總數：<strong>{totalSeats(Number(form.rowCount) || 0, Number(form.firstRowSeats) || 0)}</strong> 位
            </p>
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

export default HallsTab;
