import { Fragment, useEffect, useState } from 'react';
import { request, requestPage } from '../../api.js';
import Pager from '../../components/admin/Pager.jsx';

const PAGE_SIZE = 10;

// 訂單查詢：/admin/orders（只能查詢，不能修改、刪除）
function AdminOrdersPage() {
  const [memberInput, setMemberInput] = useState('');
  const [orderInput, setOrderInput] = useState('');
  const [memberId, setMemberId] = useState('');     // 按下查詢才生效
  const [page, setPage] = useState(1);
  const [data, setData] = useState(null);
  const [single, setSingle] = useState(null);       // 用訂單編號查到的單筆
  const [error, setError] = useState('');
  const [openId, setOpenId] = useState(null);       // 展開看票的訂單

  useEffect(() => {
    if (single) return;   // 正在看單筆訂單時不抓清單
    const filter = memberId ? `&memberId=${memberId}` : '';
    requestPage(`/api/orders?page=${page}&size=${PAGE_SIZE}${filter}`)
      .then((d) => { setData(d); setError(''); })
      .catch((err) => setError(err.message));
  }, [memberId, page, single]);

  const handleSearch = async (e) => {
    e.preventDefault();
    setOpenId(null);
    // 有填訂單編號 → 查單筆
    if (orderInput.trim()) {
      try {
        const o = await request(`/api/orders/${orderInput.trim()}`);
        setSingle(o);
        setOpenId(o.orderId);
        setError('');
      } catch {
        setSingle(null);
        setError(`找不到訂單 #${orderInput.trim()}`);
      }
      return;
    }
    setSingle(null);
    setMemberId(memberInput.trim());
    setPage(1);
  };

  const handleClear = () => {
    setMemberInput('');
    setOrderInput('');
    setMemberId('');
    setSingle(null);
    setPage(1);
    setError('');
  };

  const rows = single ? [single] : data?.content || [];
  const fmtTime = (t) => (t ? t.replace('T', ' ').slice(0, 16) : '');

  return (
    <div className="admin-page">
      <div className="page-head">
        <div>
          <h1>訂單查詢</h1>
          <p>查看全部訂單、依會員或訂單編號查詢，點一列可以看這筆訂單的票</p>
        </div>
      </div>

      <form className="toolbar" onSubmit={handleSearch}>
        <label className="field inline">
          <span>會員編號</span>
          <input value={memberInput} onChange={(e) => setMemberInput(e.target.value)} type="number" min="1" />
        </label>
        <label className="field inline">
          <span>訂單編號</span>
          <input value={orderInput} onChange={(e) => setOrderInput(e.target.value)} type="number" min="1" />
        </label>
        <button className="btn btn-primary btn-sm">查詢</button>
        <button type="button" className="btn btn-ghost btn-sm" onClick={handleClear}>清除</button>
      </form>

      {error && <div className="alert">{error}</div>}

      <div className="panel">
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>訂單</th>
                <th>會員</th>
                <th>電影</th>
                <th>場次</th>
                <th>張數</th>
                <th className="right">實收</th>
                <th>下單時間</th>
              </tr>
            </thead>
            <tbody>
              {!single && data === null && !error && <tr><td colSpan="7" className="empty">載入中...</td></tr>}
              {rows.length === 0 && (single || data) && <tr><td colSpan="7" className="empty">沒有訂單</td></tr>}
              {rows.map((o) => (
                <Fragment key={o.orderId}>
                  <tr className="clickable" onClick={() => setOpenId(openId === o.orderId ? null : o.orderId)}>
                    <td className="mono strong">
                      <span className="caret">{openId === o.orderId ? '▾' : '▸'}</span> #{o.orderId}
                    </td>
                    <td className="mono">#{o.memberId}</td>
                    <td className="strong">{o.movieTitle}</td>
                    <td className="nowrap">{o.showDate} {o.startTime?.slice(0, 5)}　{o.hallName}</td>
                    <td>{o.tickets.length} 張</td>
                    <td className="right strong">NT${o.totalAmount}</td>
                    <td className="mono muted nowrap">{fmtTime(o.orderTime)}</td>
                  </tr>
                  {openId === o.orderId && (
                    <tr className="detail-row">
                      <td colSpan="7">
                        <div className="ticket-chips">
                          {o.tickets.map((t) => (
                            <span key={t.ticketId} className="ticket-chip">
                              {t.rowNo} 排 {t.seatNo} 號・{t.typeName}・NT${t.price}
                            </span>
                          ))}
                        </div>
                        <div className="detail-sum">
                          原價 NT${o.originalAmount}　折扣 NT${o.discountAmount}　實收 <strong>NT${o.totalAmount}</strong>
                        </div>
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {!single && data && <Pager page={page} totalPages={data.totalPages} onChange={setPage} />}
    </div>
  );
}

export default AdminOrdersPage;
