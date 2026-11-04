import { useState } from 'react';
import { API, request } from '../../api.js';
import FormErrors from './FormErrors.jsx';

export const RATINGS = ['普遍級', '保護級', '輔12級', '輔15級', '限制級'];
const IMAGE_TYPES = [
  { value: 'poster', text: '海報' },
  { value: 'still', text: '劇照' },
  { value: 'banner', text: '橫幅' },
];
const IMAGE_EXT = ['jpg', 'jpeg', 'png', 'webp'];
const VIDEO_EXT = ['mp4', 'webm'];

// 取副檔名（小寫）
const extOf = (name) => (name.includes('.') ? name.split('.').pop().toLowerCase() : '');

// 把電影資料轉成後端 MovieRequest 需要的欄位
export function toMovieRequest(f) {
  return {
    title: f.title.trim(),
    durationMin: Number(f.durationMin),
    rating: f.rating,
    synopsis: f.synopsis,
    director: f.director,
    actors: f.actors,
    genre: f.genre,
    releaseDate: f.releaseDate,
    isActive: f.isActive,
  };
}

// 組出 multipart/form-data：movie（JSON）+ images（JSON 陣列）+ files（圖檔）+ trailer（影片）
export function buildMovieFormData(movieReq, imageRows = [], trailer = null) {
  const fd = new FormData();
  // @RequestPart 收 JSON 時，要包成 type 是 application/json 的 Blob
  fd.append('movie', new Blob([JSON.stringify(movieReq)], { type: 'application/json' }));
  if (imageRows.length > 0) {
    const types = imageRows.map((r) => ({ imageType: r.type }));
    fd.append('images', new Blob([JSON.stringify(types)], { type: 'application/json' }));
    imageRows.forEach((r) => fd.append('files', r.file));   // 同一個名字 files 放多個檔案
  }
  if (trailer) fd.append('trailer', trailer);
  return fd;
}

// 新增 / 編輯電影表單；movie 有值 = 編輯，沒值 = 新增
function MovieForm({ movie, onSaved, onCancel }) {
  const isEdit = Boolean(movie);
  const [form, setForm] = useState({
    title: movie?.title || '',
    durationMin: movie?.durationMin || '',
    rating: movie?.rating || RATINGS[0],
    genre: movie?.genre || '',
    director: movie?.director || '',
    actors: movie?.actors || '',
    releaseDate: movie?.releaseDate || '',
    synopsis: movie?.synopsis || '',
    isActive: movie ? movie.isActive : true,
  });
  // 新增時預設一列海報；編輯時預設不換圖
  const [imageRows, setImageRows] = useState(isEdit ? [] : [{ type: 'poster', file: null }]);
  const [trailer, setTrailer] = useState(null);
  const [errors, setErrors] = useState([]);
  const [sending, setSending] = useState(false);

  const onChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  };

  const updateRow = (i, key, value) =>
    setImageRows(imageRows.map((r, idx) => (idx === i ? { ...r, [key]: value } : r)));
  const addRow = () => setImageRows([...imageRows, { type: 'still', file: null }]);
  const removeRow = (i) => setImageRows(imageRows.filter((_, idx) => idx !== i));

  // 前端驗證（規則跟後端 MovieRequest、MovieService 一致）
  const validate = () => {
    const errs = [];
    if (!form.title.trim()) errs.push('請輸入片名');
    if (form.title.length > 100) errs.push('片名最多 100 字');
    if (!form.durationMin || Number(form.durationMin) < 1) errs.push('片長至少 1 分鐘');
    if (!form.releaseDate) errs.push('請輸入上映日期');
    if (form.director.length > 100) errs.push('導演最多 100 字');
    if (form.actors.length > 255) errs.push('演員最多 255 字');
    if (form.genre.length > 50) errs.push('類型最多 50 字');
    if (!isEdit && imageRows.length === 0) errs.push('至少要上傳一張圖片');
    imageRows.forEach((r, i) => {
      if (!r.file) errs.push(`第 ${i + 1} 張圖片還沒選檔案`);
      else if (!IMAGE_EXT.includes(extOf(r.file.name))) errs.push(`第 ${i + 1} 張圖片只能是 jpg、jpeg、png、webp`);
    });
    if (trailer && !VIDEO_EXT.includes(extOf(trailer.name))) errs.push('預告片只能是 mp4、webm');
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (errs.length > 0) return;

    const fd = buildMovieFormData(toMovieRequest(form), imageRows, trailer);
    setSending(true);
    try {
      if (isEdit) {
        await request(`/api/movie/${movie.movieId}`, { method: 'PUT', body: fd });
      } else {
        await request('/api/movie/add', { method: 'POST', body: fd });
      }
      onSaved();
    } catch (err) {
      setErrors([err.message]);
    } finally {
      setSending(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <FormErrors errors={errors} />

      <div className="form-grid">
        <label className="field span-2">
          <span>片名 *</span>
          <input name="title" value={form.title} onChange={onChange} />
        </label>
        <label className="field">
          <span>片長（分鐘）*</span>
          <input name="durationMin" type="number" min="1" value={form.durationMin} onChange={onChange} />
        </label>
        <label className="field">
          <span>分級 *</span>
          <select name="rating" value={form.rating} onChange={onChange}>
            {RATINGS.map((r) => <option key={r}>{r}</option>)}
          </select>
        </label>
        <label className="field">
          <span>類型</span>
          <input name="genre" value={form.genre} onChange={onChange} placeholder="科幻、動畫…" />
        </label>
        <label className="field">
          <span>上映日期 *</span>
          <input name="releaseDate" type="date" value={form.releaseDate} onChange={onChange} />
        </label>
        <label className="field">
          <span>導演</span>
          <input name="director" value={form.director} onChange={onChange} />
        </label>
        <label className="field">
          <span>演員</span>
          <input name="actors" value={form.actors} onChange={onChange} placeholder="用逗號分隔" />
        </label>
        <label className="field span-2">
          <span>簡介</span>
          <textarea name="synopsis" rows="4" value={form.synopsis} onChange={onChange} />
        </label>
        <label className="check span-2">
          <input name="isActive" type="checkbox" checked={form.isActive} onChange={onChange} />
          <span>上架（購票人看得到）</span>
        </label>
      </div>

      {/* 圖片 */}
      <div className="form-section">
        <div className="form-section-head">
          <h4>圖片</h4>
          <button type="button" className="btn btn-ghost btn-sm" onClick={addRow}>＋ 加一張</button>
        </div>

        {isEdit && (
          <>
            <div className="thumb-list">
              {(movie.images || []).map((img) => (
                <figure key={img.imageId}>
                  <img src={API + img.imageUrl} alt="" />
                  <figcaption>{img.imageType}</figcaption>
                </figure>
              ))}
            </div>
            <p className="form-hint">
              {imageRows.length === 0
                ? '不換圖片就不用加；按「加一張」上傳新圖片時，原本的圖片會全部被取代。'
                : '注意：送出後原本的圖片會全部換成下面這些。'}
            </p>
          </>
        )}

        {imageRows.map((row, i) => (
          <div className="image-row" key={i}>
            <select value={row.type} onChange={(e) => updateRow(i, 'type', e.target.value)}>
              {IMAGE_TYPES.map((t) => <option key={t.value} value={t.value}>{t.text}</option>)}
            </select>
            <input type="file" accept=".jpg,.jpeg,.png,.webp" onChange={(e) => updateRow(i, 'file', e.target.files[0] || null)} />
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => removeRow(i)}>移除</button>
          </div>
        ))}
      </div>

      {/* 預告片 */}
      <div className="form-section">
        <div className="form-section-head">
          <h4>預告片</h4>
        </div>
        {isEdit && movie.trailerUrl && <p className="form-hint">目前：{movie.trailerUrl}（選新檔案才會更換）</p>}
        <input type="file" accept=".mp4,.webm" onChange={(e) => setTrailer(e.target.files[0] || null)} />
      </div>

      <div className="form-actions">
        <button type="button" className="btn btn-ghost" onClick={onCancel}>取消</button>
        <button className="btn btn-primary" disabled={sending}>{sending ? '儲存中...' : '儲存'}</button>
      </div>
    </form>
  );
}

export default MovieForm;
