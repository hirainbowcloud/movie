import { useEffect, useState } from 'react';
import { API, request, requestPage } from '../../api.js';
import Modal from '../../components/admin/Modal.jsx';
import Pager from '../../components/admin/Pager.jsx';
import MovieForm, { toMovieRequest, buildMovieFormData } from '../../components/admin/MovieForm.jsx';

const PAGE_SIZE = 10;

// 電影管理（上下架）：/admin/movies
function AdminMoviesPage() {
  const [tab, setTab] = useState('active');        // active 上架中 / inactive 已下架
  const [page, setPage] = useState(1);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(null);    // null 不顯示表單；'new' 新增；電影物件 = 編輯
  const [reload, setReload] = useState(0);         // 數字一改就重新抓資料

  useEffect(() => {
    const url = tab === 'active' ? '/api/movie' : '/api/movie/inactive';
    requestPage(`${url}?page=${page}&size=${PAGE_SIZE}`)
      .then((d) => { setData(d); setError(''); })
      .catch((err) => setError(err.message));
  }, [tab, page, reload]);

  const switchTab = (t) => {
    setTab(t);
    setPage(1);
    setData(null);
  };

  // 上架 ↔ 下架：送出原本的資料，只改 isActive（不傳圖片 = 圖片不動）
  const toggleActive = async (movie) => {
    const action = movie.isActive ? '下架' : '上架';
    if (!window.confirm(`確定要${action}「${movie.title}」？`)) return;
    try {
      const fd = buildMovieFormData(toMovieRequest({ ...movie, isActive: !movie.isActive }));
      await request(`/api/movie/${movie.movieId}`, { method: 'PUT', body: fd });
      setReload((n) => n + 1);
    } catch (err) {
      alert(`${action}失敗：${err.message}`);
    }
  };

  const poster = (m) => (m.images || []).find((img) => img.imageType === 'POSTER');

  return (
    <div className="admin-page">
      <div className="page-head">
        <div>
          <h1>電影管理</h1>
          <p>新增、編輯電影資料與圖片，控制上架 / 下架</p>
        </div>
        <button className="btn btn-primary" onClick={() => setEditing('new')}>＋ 新增電影</button>
      </div>

      <div className="tabs">
        <button className={tab === 'active' ? 'active' : ''} onClick={() => switchTab('active')}>上架中</button>
        <button className={tab === 'inactive' ? 'active' : ''} onClick={() => switchTab('inactive')}>已下架</button>
      </div>

      {error && <div className="alert">{error}</div>}

      <div className="panel">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>海報</th>
                <th>片名</th>
                <th>分級</th>
                <th>類型</th>
                <th>片長</th>
                <th>上映日期</th>
                <th>狀態</th>
                <th className="right">操作</th>
              </tr>
            </thead>
            <tbody>
              {data === null && <tr><td colSpan="8" className="empty">載入中...</td></tr>}
              {data && data.content.length === 0 && <tr><td colSpan="8" className="empty">沒有資料</td></tr>}
              {data && data.content.map((m) => (
                <tr key={m.movieId}>
                  <td>
                    {poster(m)
                      ? <img className="table-thumb" src={API + poster(m).imageUrl} alt="" />
                      : <div className="table-thumb" />}
                  </td>
                  <td className="strong">{m.title}</td>
                  <td>{m.rating}</td>
                  <td>{m.genre}</td>
                  <td>{m.durationMin} 分</td>
                  <td className="mono">{m.releaseDate}</td>
                  <td>
                    <span className={m.isActive ? 'badge badge-green' : 'badge'}>{m.isActive ? '上架' : '下架'}</span>
                  </td>
                  <td className="right nowrap">
                    <button className="btn btn-ghost btn-sm" onClick={() => setEditing(m)}>編輯</button>
                    <button className="btn btn-ghost btn-sm" onClick={() => toggleActive(m)}>
                      {m.isActive ? '下架' : '上架'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}

      {editing && (
        <Modal title={editing === 'new' ? '新增電影' : `編輯：${editing.title}`} onClose={() => setEditing(null)} wide>
          <MovieForm
            movie={editing === 'new' ? null : editing}
            onCancel={() => setEditing(null)}
            onSaved={() => { setEditing(null); setReload((n) => n + 1); }}
          />
        </Modal>
      )}
    </div>
  );
}

export default AdminMoviesPage;
