import { useEffect, useState } from 'react';
import { request, requestPage } from '../../api.js';
import Modal from './Modal.jsx';
import Pager from './Pager.jsx';
import FormErrors from './FormErrors.jsx';

const EMPTY = { typeName: '', basePrice: '', discountRate: '1.00', description: '' };

// 折扣率 0.8 → 「8 折」；1 → 「原價」
const rateText = (r) => (Number(r) >= 1 ? '原價' : `${Math.round(Number(r) * 100) / 10} 折`);

// 票種管理
function TicketTypesTab() {
  const [page, setPage] = useState(1);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [formErrors, setFormErrors] = useState([]);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    requestPage(`/api/ticket-type?page=${page}&size=10`)
      .then((d) => { setData(d); setError(''); })
      .catch((err) => setError(err.message));
  }, [page, reload]);

  const open = (t) => {
    setForm(t
      ? { typeName: t.typeName, basePrice: t.basePrice, discountRate: t.discountRate, description: t.description || '' }
      : EMPTY);
    setFormErrors([]);
    setEditing(t || 'new');
  };

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async (e) => {
    e.preventDefault();
    const rate = Number(form.discountRate);
    const errs = [];
    if (!form.typeName.trim()) errs.push('請輸入票種名稱');
    if (form.typeName.length > 30) errs.push('票種名稱最多 30 字');
    if (form.basePrice === '' || Number(form.basePrice) < 0) errs.push('售價不能是負數');
    if (!(rate >= 0.01 && rate <= 1)) errs.push('折扣率要在 0.01 ～ 1.00 之間');
    if (form.description.length > 100) errs.push('說明最多 100 字');
    setFormErrors(errs);
    if (errs.length > 0) return;

    const body = {
      typeName: form.typeName.trim(),
      basePrice: Number(form.basePrice),
      discountRate: rate,
      description: form.description,
    };
    try {
      if (editing === 'new') await request('/api/ticket-type', { method: 'POST', body });
      else await request(`/api/ticket-type/${editing.typeId}`, { method: 'PUT', body });
      setEditing(null);
      setReload((n) => n + 1);
    } catch (err) {
      setFormErrors([err.message]);
    }
  };

  const handleDelete = async (t) => {
    if (!window.confirm(`確定刪除「${t.typeName}」？`)) return;
    try {
      await request(`/api/ticket-type/${t.typeId}`, { method: 'DELETE' });
      setReload((n) => n + 1);
    } catch (err) {
      alert('刪除失敗（已經賣出的票種不能刪除）：' + err.message);
    }
  };

  return (
    <>
      <div className="tab-head">
        <p>售價是實際收的金額；折扣率用來回推原價（例如 150 元、0.5 → 原價 300 元）</p>
        <button className="btn btn-primary btn-sm" onClick={() => open(null)}>＋ 新增票種</button>
      </div>

      {error && <div className="alert">{error}</div>}

      <div className="panel">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>編號</th><th>票種</th><th>售價</th><th>折扣</th><th>說明</th><th className="right">操作</th></tr>
            </thead>
            <tbody>
              {data === null && <tr><td colSpan="6" className="empty">載入中...</td></tr>}
              {data && data.content.length === 0 && <tr><td colSpan="6" className="empty">還沒有票種</td></tr>}
              {data && data.content.map((t) => (
                <tr key={t.typeId}>
                  <td className="mono muted">#{t.typeId}</td>
                  <td className="strong">{t.typeName}</td>
                  <td>NT${t.basePrice}</td>
                  <td>{rateText(t.discountRate)}</td>
                  <td className="muted">{t.description}</td>
                  <td className="right nowrap">
                    <button className="btn btn-ghost btn-sm" onClick={() => open(t)}>編輯</button>
                    <button className="btn btn-ghost btn-sm danger" onClick={() => handleDelete(t)}>刪除</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}

      {editing && (
        <Modal title={editing === 'new' ? '新增票種' : `編輯：${editing.typeName}`} onClose={() => setEditing(null)}>
          <form onSubmit={handleSave}>
            <FormErrors errors={formErrors} />
            <div className="form-grid">
              <label className="field span-2">
                <span>票種名稱 *</span>
                <input name="typeName" value={form.typeName} onChange={onChange} placeholder="全票" />
              </label>
              <label className="field">
                <span>售價 *</span>
                <input name="basePrice" type="number" min="0" value={form.basePrice} onChange={onChange} />
              </label>
              <label className="field">
                <span>折扣率 *（0.01～1.00）</span>
                <input name="discountRate" type="number" step="0.01" min="0.01" max="1" value={form.discountRate} onChange={onChange} />
              </label>
              <label className="field span-2">
                <span>說明</span>
                <input name="description" value={form.description} onChange={onChange} />
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

export default TicketTypesTab;
