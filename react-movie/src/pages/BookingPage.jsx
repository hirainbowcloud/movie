import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { API, readError, getMember } from '../api.js';
import Dialog from '../components/Dialog.jsx';
import '../css/Booking.css';

const GROUP_MIN = 15;   // 團體票：同一筆訂單至少 15 人

// 訂票選位頁：網址 /booking/:showtimeId
function BookingPage() {
  const { showtimeId } = useParams();                  // 從網址 /booking/1 取得 1
  const navigate = useNavigate();
  const location = useLocation();
  const [sending, setSending] = useState(false);       // 送出訂單中，避免重複按
  const [seatMap, setSeatMap] = useState(null);       // 場次 + 影廳 + 每個座位狀態
  const [ticketTypes, setTicketTypes] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [error, setError] = useState('');
  const [dialog, setDialog] = useState(null);          // 對話框內容，null = 不顯示
  const [lastTypeId, setLastTypeId] = useState(null);  // 最近選的票種，新加的座位沿用它

  useEffect(() => {
    // 取得座位圖（含已售 / 未售）
    fetch(`${API}/api/showtime/${showtimeId}/seats`)
      .then((res) => {
        if (!res.ok) throw new Error('座位圖讀取失敗（HTTP ' + res.status + '）');
        return res.json();
      })
      .then((data) => setSeatMap(data))
      .catch((err) => {
        console.error('座位圖 API 錯誤:', err);
        setError(err.message);
      });

    // 取得票種清單
    fetch(`${API}/api/ticket-type?page=1&size=10`)
      .then((res) => {
        if (!res.ok) throw new Error('票種讀取失敗（HTTP ' + res.status + '）');
        return res.json();
      })
      .then((data) => setTicketTypes(data.content || []))
      .catch((err) => {
        console.error('票種 API 錯誤:', err);
        setError(err.message);
      });
  }, [showtimeId]);

  if (error) {
    return <div className="loading">{error}</div>;
  }
  if (!seatMap || ticketTypes.length === 0) {
    return <div className="loading">資料載入中...</div>;
  }

  // 已售出的座位，例如 "2-4" 代表第 2 排第 4 號（status 由後端判斷）
  const soldSeats = seatMap.seats
    .filter((s) => s.status === 'SOLD')
    .map((s) => `${s.rowNo}-${s.seatNo}`);

  // 依排號把座位分組：rows[0] = 第 1 排的所有座位…
  // 每排座位數不一定一樣，所以照後端給的 seats 畫
  const rows = Array.from({ length: seatMap.rowCount }, (_, i) =>
    seatMap.seats.filter((s) => s.rowNo === i + 1)
  );

  // 判斷某個票種是不是雙人票
  const isDoubleType = (typeId) => {
    const typeObj = ticketTypes.find((t) => t.typeId === typeId);
    return typeObj && typeObj.typeName.includes('雙人');
  };

  // 判斷某個票種是不是團體票
  const isGroupType = (typeId) => {
    const typeObj = ticketTypes.find((t) => t.typeId === typeId);
    return typeObj && typeObj.typeName.includes('團體');
  };

  // 選單裡的票種：未滿 15 人時，團體票不能選
  const groupLocked = selectedSeats.length < GROUP_MIN;
  const typeOptions = ticketTypes.map((type) => {
    const locked = groupLocked && isGroupType(type.typeId);
    return (
      <option key={type.typeId} value={type.typeId} disabled={locked}>
        {type.typeName} (NT${type.basePrice}){locked ? `（${GROUP_MIN}人以上）` : ''}
      </option>
    );
  });

  // 點擊座位切換選取狀態（可以一直加選，票種在右側各自設定）
  const toggleSeat = (row, col) => {
    const seatId = `${row}-${col}`;
    if (soldSeats.includes(seatId)) return;

    const exists = selectedSeats.find((s) => s.id === seatId);
    if (exists) {
      setSelectedSeats(selectedSeats.filter((s) => s.id !== seatId));
      return;
    }

    // 新座位預設用第一個「不是雙人票」的票種（例如全票）
    // 新座位的票種：沿用最近選的票種；還沒選過就用「不打折」的一般票
    const defaultType =
      ticketTypes.find((t) => t.typeId === lastTypeId && !(isGroupType(t.typeId) && selectedSeats.length + 1 < GROUP_MIN)) ||
      ticketTypes.find((t) => Number(t.discountRate) >= 1 && !t.typeName.includes('雙人')) ||
      ticketTypes.find((t) => !t.typeName.includes('雙人')) ||
      ticketTypes[0];

    setSelectedSeats([
      ...selectedSeats,
      { id: seatId, row, col, typeId: defaultType.typeId },
    ]);
  };

  // 切換選中座位的票種（每個座位各自設定）
  const handleTypeChange = (seatId, newTypeId) => {
    const typeId = Number(newTypeId);
    setLastTypeId(typeId);
    setSelectedSeats(
      selectedSeats.map((s) => (s.id === seatId ? { ...s, typeId } : s))
    );
  };

  // 一次把全部已選座位設成同一種票（例如 18 人團體票）
  const applyTypeToAll = (newTypeId) => {
    if (!newTypeId) return;
    const typeId = Number(newTypeId);
    if (isGroupType(typeId) && selectedSeats.length < GROUP_MIN) return;   // 選單已經擋住，這裡再保險一次
    setLastTypeId(typeId);
    setSelectedSeats(selectedSeats.map((s) => ({ ...s, typeId })));
  };

  // 雙人票座位數（必須是偶數，每 2 位算一組）
  const doubleCount = selectedSeats.filter((s) => isDoubleType(s.typeId)).length;
  const groupCount = selectedSeats.filter((s) => isGroupType(s.typeId)).length;   // 選了團體票的座位數

  // 每個座位的價格：雙人票一組 540 → 每位 270
  const seatPrice = (typeId) => {
    const typeObj = ticketTypes.find((t) => t.typeId === typeId);
    if (!typeObj) return 0;
    return isDoubleType(typeId) ? typeObj.basePrice / 2 : typeObj.basePrice;
  };

  // 計算總金額
  const totalPrice = selectedSeats.reduce((sum, s) => sum + seatPrice(s.typeId), 0);

  // 送出訂單
  const handleBooking = () => {
    if (selectedSeats.length === 0) {
      setDialog({ type: 'info', title: '請先選擇座位', message: '點左邊的座位圖選位子，再按確認訂票。', actions: [{ text: '確定', primary: true, onClick: () => setDialog(null) }] });
      return;
    }

    // 雙人票必須 2 位一組
    if (doubleCount % 2 !== 0) {
      setDialog({ type: 'info', title: '雙人票要 2 位一組', message: `目前雙人票有 ${doubleCount} 位，請再調整成雙數。`, actions: [{ text: '確定', primary: true, onClick: () => setDialog(null) }] });
      return;
    }

    // 團體票：同一筆訂單要 15 人以上
    if (groupCount > 0 && selectedSeats.length < GROUP_MIN) {
      setDialog({
        type: 'info',
        title: `團體票需 ${GROUP_MIN} 人以上`,
        message: `目前只選了 ${selectedSeats.length} 個座位，請再加選座位，或把團體票改成其他票種。`,
        actions: [{ text: '確定', primary: true, onClick: () => setDialog(null) }],
      });
      return;
    }

    // 要先登入才能訂票；登入後回到這個選位頁
    const member = getMember();
    if (!member) {
      setDialog({
        type: 'info',
        title: '請先登入會員',
        message: '登入後會回到這個選位頁，已選的座位需要重新選。',
        actions: [
          { text: '取消', onClick: () => setDialog(null) },
          { text: '前往登入', primary: true, onClick: () => navigate('/login', { state: { from: location.pathname } }) },
        ],
      });
      return;
    }

    // 後端 OrdersRequest 格式：價格由後端依票種計算，前端只送座位和票種
    const payload = {
      memberId: member.memberId,
      showtimeId: seatMap.showtimeId,
      tickets: selectedSeats.map((s) => ({
        rowNo: s.row,
        seatNo: s.col,
        typeId: s.typeId,
      })),
    };

    setSending(true);
    fetch(`${API}/api/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
      .then(async (res) => {
        if (!res.ok) throw new Error(await readError(res));
        return res.json();
      })
      .then((order) => {
        setSending(false);
        setDialog({
          type: 'success',
          title: '訂票成功',
          order,
          actions: [{ text: '查看我的訂單', primary: true, onClick: () => navigate('/orders') }],
        });
      })
      .catch((err) => {
        setDialog({ type: 'error', title: '訂票失敗', message: err.message, actions: [{ text: '確定', primary: true, onClick: () => setDialog(null) }] });
        setSending(false);
      });
  };

  return (
    <div className="booking-container">
      <h2 className="booking-header">
        {seatMap.movieTitle}｜{seatMap.hallName}
      </h2>
      <p className="booking-info">
        {seatMap.showDate} {seatMap.startTime.slice(0, 5)}{'　'}
        剩餘 {seatMap.availableCount} / {seatMap.totalSeats} 位
      </p>

      <div className="main-content">
        {/* 左側：銀幕與座位 */}
        <div className="left-section">
          <div className="screen-container">
            <div className="screen-bar"></div>
            <div className="screen-text">銀幕</div>
          </div>

          <div className="seat-grid">
            {rows.map((rowSeats, rowIndex) => (
              <div key={`row-${rowIndex + 1}`} className="seat-row">
                <span className="row-label">{rowIndex + 1}</span>

                {rowSeats.map((seat) => {
                  const seatId = `${seat.rowNo}-${seat.seatNo}`;
                  const isSold = seat.status === 'SOLD';
                  const isSelected = selectedSeats.some((s) => s.id === seatId);

                  let seatClass = 'seat available';
                  if (isSold) seatClass = 'seat sold';
                  if (isSelected) seatClass = 'seat selected';

                  return (
                    <div
                      key={seatId}
                      className={seatClass}
                      onClick={() => toggleSeat(seat.rowNo, seat.seatNo)}
                      title={`${seat.rowNo}排${seat.seatNo}號${isSold ? '（已售出）' : ''}`}
                    />
                  );
                })}
              </div>
            ))}
          </div>

          <div className="legend-container">
            <div className="legend-item">
              <span className="legend-box available"></span>
              <span>白：未售</span>
            </div>
            <div className="legend-item">
              <span className="legend-box selected"></span>
              <span>藍：預訂</span>
            </div>
            <div className="legend-item">
              <span className="legend-box sold"></span>
              <span>灰：已售</span>
            </div>
          </div>
        </div>

        {/* 右側：票種選單與結帳 */}
        <div className="card">
          <h3 className="card-title">已選 {selectedSeats.length}</h3>

          {/* 票種規則 */}
          <div className="ticket-rules">
            <div className="ticket-rules-title">票種規則</div>
            <ul>
              <li className={groupLocked ? '' : 'ok'}>
                <strong>團體票</strong>：同一筆訂單 {GROUP_MIN} 人以上才能選
                {groupLocked ? `（還差 ${GROUP_MIN - selectedSeats.length} 人）` : '（已達人數）'}
              </li>
              <li><strong>雙人票</strong>：2 個座位為一組</li>
            </ul>
          </div>

          {/* 2 個座位以上：可以一次設定全部票種 */}
          {selectedSeats.length >= 2 && (
            <label className="apply-all">
              <span>全部座位改成</span>
              <select value="" onChange={(e) => applyTypeToAll(e.target.value)}>
                <option value="">選擇票種…</option>
                {typeOptions}
              </select>
            </label>
          )}

          <div className="seat-list">
            {selectedSeats.length === 0 ? (
              <p className="hint">請點擊左側座位進行選取</p>
            ) : (
              selectedSeats.map((seat) => (
                <div key={seat.id} className="seat-list-item">
                  <span>{seat.row}排{seat.col}號</span>
                  <select
                    value={seat.typeId}
                    onChange={(e) => handleTypeChange(seat.id, e.target.value)}
                  >
                    {typeOptions}
                  </select>
                </div>
              ))
            )}
          </div>

          {groupCount > 0 && groupLocked && (
            <p className="warn">團體票需 {GROUP_MIN} 人以上，目前 {selectedSeats.length} 人，請加選座位或改票種</p>
          )}

          {doubleCount % 2 !== 0 && (
            <p className="warn">雙人票需 2 位一組，請再選 1 位雙人票</p>
          )}

          <div className="total-section">
            <span>合計</span>
            <span>NT${totalPrice}</span>
          </div>

          <button className="confirm-button" onClick={handleBooking} disabled={sending}>
            {sending ? '送出中...' : '確認訂票'}
          </button>
        </div>
      </div>

      {dialog && (
        <Dialog type={dialog.type} title={dialog.title} actions={dialog.actions}>
          {dialog.message}
          {dialog.order && (
            <dl className="dialog-rows">
              <div><dt>訂單編號</dt><dd>#{dialog.order.orderId}</dd></div>
              <div><dt>電影</dt><dd>{seatMap.movieTitle}</dd></div>
              <div><dt>場次</dt><dd>{seatMap.showDate} {seatMap.startTime.slice(0, 5)}</dd></div>
              <div><dt>張數</dt><dd>{dialog.order.tickets.length} 張</dd></div>
              <div className="total"><dt>實收</dt><dd>NT${dialog.order.totalAmount.toLocaleString('zh-TW')}</dd></div>
            </dl>
          )}
        </Dialog>
      )}
    </div>
  );
}

export default BookingPage;