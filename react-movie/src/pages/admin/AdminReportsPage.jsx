import { useEffect, useState } from 'react';
import { API, readError, request } from '../../api.js';

// 金額加上千分位：12345 → 12,345
const money = (n) => Number(n || 0).toLocaleString('zh-TW');

// 銷售報表：/admin/reports
function AdminReportsPage() {
  const [period, setPeriod] = useState('week');    // week 本週 / month 本月
  const [report, setReport] = useState(null);
  const [error, setError] = useState('');
  const [titleInput, setTitleInput] = useState('');
  const [movieSales, setMovieSales] = useState(null);   // 依片名查的電影銷售
  const [salesError, setSalesError] = useState('');
  const [hoverIdx, setHoverIdx] = useState(null);      // 滑鼠目前指著第幾天

  useEffect(() => {
    request(`/api/admin/report/sales?period=${period}`)
      .then((d) => { setReport(d); setError(''); })
      .catch((err) => setError(err.message));
  }, [period]);

  // 依片名查電影銷售（空白 = 全部電影）
  const searchMovieSales = async (e) => {
    e.preventDefault();
    const q = titleInput.trim();
    const res = await fetch(`${API}/api/admin/report/movie-sales${q ? `?title=${encodeURIComponent(q)}` : ''}`);
    if (res.status === 404) {
      setMovieSales([]);
      setSalesError(q ? `找不到片名包含「${q}」的電影` : '還沒有銷售資料');
      return;
    }
    if (!res.ok) {
      setSalesError(await readError(res));
      return;
    }
    setMovieSales(await res.json());
    setSalesError('');
  };

  const daily = report?.daily || [];
  const movies = movieSales ?? report?.movies ?? [];
  const maxDaily = Math.max(1, ...daily.map((d) => d.totalAmount));
  const maxMovie = Math.max(1, ...movies.map((m) => m.totalAmount));
  const hovered = hoverIdx !== null ? daily[hoverIdx] : null;   // 切換本週/本月後可能是 undefined

  return (
    <div className="admin-page">
      <div className="page-head">
        <div>
          <h1>銷售報表</h1>
          <p>{report ? `${report.startDate} ～ ${report.endDate}` : '　'}</p>
        </div>
        <div className="segmented">
          <button className={period === 'week' ? 'active' : ''} onClick={() => setPeriod('week')}>本週</button>
          <button className={period === 'month' ? 'active' : ''} onClick={() => setPeriod('month')}>本月</button>
        </div>
      </div>

      {error && <div className="alert">{error}</div>}

      {/* 重點數字 */}
      <div className="stat-grid">
        <div className="stat">
          <span>實收金額</span>
          <strong>NT${money(report?.totalAmount)}</strong>
        </div>
        <div className="stat">
          <span>訂單數</span>
          <strong>{money(report?.orderCount)}</strong>
        </div>
        <div className="stat">
          <span>售出票數</span>
          <strong>{money(report?.ticketCount)}</strong>
        </div>
        <div className="stat">
          <span>折扣金額</span>
          <strong>NT${money(report?.discountAmount)}</strong>
          <small>原價小計 NT${money(report?.originalAmount)}</small>
        </div>
      </div>

      {/* 每日實收：直條圖 */}
      <div className="panel chart-panel">
        <div className="panel-title-row">
          <h3 className="panel-title">每日實收金額</h3>
          {/* 明細固定顯示在標題列右邊，不會被長條擋住或超出框外 */}
          {daily.length > 0 && (
            <div className={hovered ? 'chart-readout active' : 'chart-readout'}>
              {hovered ? (
                <>
                  <span className="readout-date">{hovered.saleDate}</span>
                  <strong>NT${money(hovered.totalAmount)}</strong>
                  <span>{hovered.ticketCount} 張・{hovered.orderCount} 筆</span>
                </>
              ) : (
                <span>滑鼠移到長條上（手機點一下）看當天明細</span>
              )}
            </div>
          )}
        </div>

        {daily.length === 0 ? (
          <p className="empty">這段期間還沒有銷售</p>
        ) : (
          <div className="bar-chart" onMouseLeave={() => setHoverIdx(null)}>
            {daily.map((d, i) => (
              <div
                key={d.saleDate}
                className={i === hoverIdx ? 'bar-col active' : 'bar-col'}
                onMouseEnter={() => setHoverIdx(i)}
                onClick={() => setHoverIdx(i)}
              >
                <div className="bar-track">
                  <div className="bar" style={{ height: `${(d.totalAmount / maxDaily) * 100}%` }} />
                </div>
                <span className="bar-label">{d.saleDate.slice(5)}</span>
              </div>
            ))}
          </div>
        )}

        {/* 同樣的資料用表格呈現，方便看精確數字 */}
        {daily.length > 0 && (
          <details className="table-toggle">
            <summary>看表格</summary>
            <div className="table-wrap">
              <table className="table">
                <thead><tr><th>日期</th><th className="right">訂單</th><th className="right">票數</th><th className="right">實收</th></tr></thead>
                <tbody>
                  {daily.map((d) => (
                    <tr key={d.saleDate}>
                      <td className="mono">{d.saleDate}</td>
                      <td className="right">{d.orderCount}</td>
                      <td className="right">{d.ticketCount}</td>
                      <td className="right strong">NT${money(d.totalAmount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </details>
        )}
      </div>

      {/* 各電影銷售：橫條圖 + 數字 */}
      <div className="panel chart-panel">
        <div className="panel-title-row">
          <h3 className="panel-title">各電影銷售</h3>
          <form className="toolbar compact" onSubmit={searchMovieSales}>
            <input className="search-input" value={titleInput} onChange={(e) => setTitleInput(e.target.value)} placeholder="片名關鍵字（全部期間）" />
            <button className="btn btn-primary btn-sm">查詢</button>
            {movieSales && (
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => { setMovieSales(null); setTitleInput(''); setSalesError(''); }}>
                回到{period === 'week' ? '本週' : '本月'}
              </button>
            )}
          </form>
        </div>
        <p className="panel-sub">{movieSales ? '依片名查詢（全部期間）' : `${period === 'week' ? '本週' : '本月'}各電影`}</p>

        {salesError && <p className="empty">{salesError}</p>}
        {!salesError && movies.length === 0 && <p className="empty">這段期間還沒有銷售</p>}

        <div className="hbar-list">
          {movies.map((m) => (
            <div key={m.movieId} className="hbar-row">
              <span className="hbar-name">{m.title}</span>
              <div className="hbar-track">
                <div className="hbar" style={{ width: `${(m.totalAmount / maxMovie) * 100}%` }} />
              </div>
              <span className="hbar-value">NT${money(m.totalAmount)}<small>{m.ticketCount} 張</small></span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default AdminReportsPage;
