import { useEffect, useState } from 'react';
import { flushSync } from 'react-dom';
import { Navigate } from 'react-router-dom';
import { API, getMember } from '../api.js';
import '../css/Orders.css';

const PAGE_SIZE = 5;

// 我的訂單與電影票：/orders（1 個座位 1 張票）
function OrdersPage() {
  const member = getMember();
  const [orders, setOrders] = useState(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [error, setError] = useState('');
  const [printId, setPrintId] = useState(null);   // 要列印哪一筆訂單
  const memberId = member?.memberId;

  useEffect(() => {
    if (!memberId) return;
    fetch(`${API}/api/orders?memberId=${memberId}&page=${page}&size=${PAGE_SIZE}`)
      .then((res) => {
        if (res.status === 404) return { content: [], totalPages: 1 };   // 沒有訂單
        if (!res.ok) throw new Error('訂單讀取失敗（HTTP ' + res.status + '）');
        return res.json();
      })
      .then((data) => {
        setOrders(data.content);
        setTotalPages(data.totalPages || 1);
      })
      .catch((err) => setError(err.message));
  }, [memberId, page]);

  // 列印某一筆訂單的電影票：先讓畫面只留下這筆，再叫出列印視窗
  const handlePrint = (orderId) => {
    flushSync(() => setPrintId(orderId));   // 立刻更新畫面（不等 React 排程）
    window.print();
    setPrintId(null);
  };

  // 沒登入 → 導到登入頁，登入後回來這裡
  if (!member) return <Navigate to="/login" state={{ from: '/orders' }} replace />;

  if (error) return <div className="page-message">{error}</div>;
  if (orders === null) return <div className="page-message">資料載入中...</div>;

  const shortTime = (t) => (t ? t.slice(0, 5) : '');
  // "2026-11-11" → "三"（用年月日建立日期，避免時區差一天）
  const weekday = (d) => {
    const [y, m, day] = d.split('-').map(Number);
    return '日一二三四五六'[new Date(y, m - 1, day).getDay()];
  };
  const formatOrderTime = (t) => (t ? t.replace('T', ' ').slice(0, 16) : '');
  const pageList = Array.from({ length: totalPages }, (_, i) => i + 1);

  return (
    <div className={printId === null ? 'orders' : 'orders printing'}>
      <h2 className="orders-title">我的訂單</h2>

      {orders.length === 0 && <p className="hint">目前沒有訂單</p>}

      {orders.map((o) => (
        <div key={o.orderId} className={o.orderId === printId ? 'order-card print-target' : 'order-card'}>
          <div className="order-header">
            <div>
              <div className="order-movie">{o.movieTitle}</div>
              <div className="order-meta">
                {o.showDate} {shortTime(o.startTime)}　{o.hallName}
              </div>
            </div>
            <div className="order-right">
              <div className="order-meta">訂單 #{o.orderId}</div>
              <div className="order-meta">{formatOrderTime(o.orderTime)}</div>
            </div>
          </div>

          {/* 電影票：一個座位一張，名片大小（列印時 9cm × 5.4cm） */}
          <div className="ticket-list">
            {o.tickets.map((t) => (
              <div key={t.ticketId} className="ticket">
                <div className="ticket-main">
                  <div className="ticket-brand">
                    <img src="/logo.svg" alt="" />
                    <span>MOVIE POS</span>
                  </div>
                  <div className="ticket-movie">{o.movieTitle}</div>
                  <div className="ticket-time">
                    {o.showDate}（{weekday(o.showDate)}）<strong>{shortTime(o.startTime)}</strong>
                  </div>
                  <div className="ticket-bottom">
                    <span className="ticket-type">{t.typeName}</span>
                    <span>NT${t.price}</span>
                  </div>
                </div>

                {/* 右邊票根：廳別、座位 */}
                <div className="ticket-stub">
                  <span className="stub-label">{o.hallName}</span>
                  <span className="stub-row">{t.rowNo}<small>排</small></span>
                  <span className="stub-seat">{t.seatNo}<small>號</small></span>
                  <span className="stub-no">No.{String(t.ticketId).padStart(6, '0')}</span>
                </div>
              </div>
            ))}
          </div>

          <div className="order-footer">
            <span className="order-amount">
              {o.discountAmount > 0 && <span className="order-discount">原價 NT${o.originalAmount}，折扣 NT${o.discountAmount}　</span>}
              合計 <strong>NT${o.totalAmount}</strong>
            </span>
            <button className="print-button" onClick={() => handlePrint(o.orderId)}>列印電影票</button>
          </div>
        </div>
      ))}

      {totalPages > 1 && (
        <div className="pager">
          <button disabled={page === 1} onClick={() => setPage(page - 1)}>‹</button>
          {pageList.map((p) => (
            <button key={p} className={p === page ? 'active' : ''} onClick={() => setPage(p)}>{p}</button>
          ))}
          <button disabled={page === totalPages} onClick={() => setPage(page + 1)}>›</button>
        </div>
      )}
    </div>
  );
}

export default OrdersPage;
