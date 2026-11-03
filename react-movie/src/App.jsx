import { useState, useEffect } from 'react';
import './css/Booking.css';

const SHOWTIME_ID = 1;   // 之後可改成從網址 /booking/:showtimeId 取得

function App() {
  const [seatMap, setSeatMap] = useState(null);       // 場次 + 影廳 + 每個座位狀態
  const [ticketTypes, setTicketTypes] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    // 取得座位圖（含已售 / 未售）
    fetch(`http://localhost:8080/api/showtime/${SHOWTIME_ID}/seats`)
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
    fetch('http://localhost:8080/api/ticket-type?page=1&size=10')
      .then((res) => {
        if (!res.ok) throw new Error('票種讀取失敗（HTTP ' + res.status + '）');
        return res.json();
      })
      .then((data) => setTicketTypes(data.content || []))
      .catch((err) => {
        console.error('票種 API 錯誤:', err);
        setError(err.message);
      });
  }, []);

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

  // 點擊座位切換選取狀態
  const toggleSeat = (row, col) => {
    const seatId = `${row}-${col}`;
    if (soldSeats.includes(seatId)) return;

    const exists = selectedSeats.find((s) => s.id === seatId);
    if (exists) {
      setSelectedSeats(selectedSeats.filter((s) => s.id !== seatId));
      return;
    }

    const doubleSeat = selectedSeats.find((s) => isDoubleType(s.typeId));

    // 若已有雙人票，禁止選擇第 3 個座位
    if (doubleSeat && selectedSeats.length >= 2) {
      alert('已選取雙人票，限定只能選 2 個座位！');
      return;
    }

    // 新座位的預設票種：已有雙人票就跟著用雙人票，否則用第一個票種
    const defaultTypeId = doubleSeat ? doubleSeat.typeId : ticketTypes[0]?.typeId;

    setSelectedSeats([
      ...selectedSeats,
      { id: seatId, row, col, typeId: defaultTypeId },
    ]);
  };

  // 切換選中座位的票種
  const handleTypeChange = (seatId, newTypeId) => {
    const typeId = Number(newTypeId);

    if (isDoubleType(typeId)) {
      if (selectedSeats.length !== 2) {
        alert('雙人票限定只能選取 2 個座位，請先選滿 2 個座位再切換！');
        return;
      }
      // 雙人票：2 個座位同步套用
      setSelectedSeats(selectedSeats.map((s) => ({ ...s, typeId })));
      return;
    }

    setSelectedSeats(
      selectedSeats.map((s) => (s.id === seatId ? { ...s, typeId } : s))
    );
  };

  // 計算總金額（雙人票 2 個座位只算一次）
  const totalPrice = selectedSeats.reduce((sum, seat, index, arr) => {
    const typeObj = ticketTypes.find((t) => t.typeId === seat.typeId);
    if (!typeObj) return sum;

    if (isDoubleType(seat.typeId)) {
      const firstDoubleIndex = arr.findIndex((s) => isDoubleType(s.typeId));
      return index === firstDoubleIndex ? sum + typeObj.basePrice : sum;
    }
    return sum + typeObj.basePrice;
  }, 0);

  // 送出訂單
  const handleBooking = () => {
    if (selectedSeats.length === 0) {
      alert('請先選擇座位！');
      return;
    }

    const hasDoubleTicket = selectedSeats.some((s) => isDoubleType(s.typeId));
    if (hasDoubleTicket && selectedSeats.length !== 2) {
      alert('選取雙人票時，必須剛好選取 2 個座位才能下單！');
      return;
    }

    const payload = {
      hallId: seatMap.hallId,
      showtimeId: seatMap.showtimeId,
      seats: selectedSeats.map((s) => {
        const typeObj = ticketTypes.find((t) => t.typeId === s.typeId);
        return {
          seatName: `${s.row}排${s.col}號`,
          typeId: s.typeId,
          typeName: typeObj ? typeObj.typeName : '',
          price: typeObj ? typeObj.basePrice : 0,
        };
      }),
      totalAmount: totalPrice,
    };

    console.log('送出 POST /api/orders Payload:', payload);
    alert(`訂票成功！共預訂 ${selectedSeats.length} 張票，總計 NT$${totalPrice}`);
  };

  return (
    <div className="booking-container">
      <h2 className="booking-header">
        {seatMap.movieTitle}｜{seatMap.hallName}
      </h2>
      <p className="booking-info">
        {seatMap.showDate} {seatMap.startTime.slice(0, 5)}　
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
                    {ticketTypes.map((type) => (
                      <option key={type.typeId} value={type.typeId}>
                        {type.typeName} (NT${type.basePrice})
                      </option>
                    ))}
                  </select>
                </div>
              ))
            )}
          </div>

          <div className="total-section">
            <span>合計</span>
            <span>NT${totalPrice}</span>
          </div>

          <button className="confirm-button" onClick={handleBooking}>
            確認訂票
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;