import { useState, useEffect } from 'react';

function App() {
  const [hallData, setHallData] = useState(null);
  const [ticketTypes, setTicketTypes] = useState([]);

  // 模擬已售出座位
  const soldSeats = ['2-4', '2-5'];
  const [selectedSeats, setSelectedSeats] = useState([]);

  useEffect(() => {
    // 取得影廳資料
    fetch("http://localhost:8080/api/hall/1")
      .then((res) => {
        if (!res.ok) throw new Error("影廳讀取失敗");
        return res.json();
      })
      .then((data) => setHallData(data))
      .catch((err) => console.error("影廳 API 錯誤:", err));

    // 呼叫 API 取得全部票種清單
    fetch("http://localhost:8080/api/ticket-type?page=1&size=10")
      .then((res) => {
        if (!res.ok) throw new Error("票種讀取失敗");
        return res.json();
      })
      .then((data) => {
        setTicketTypes(data.content || []);
        console.log("票種資料載入成功:", data.content);
      })
      .catch((err) => console.error("票種 API 錯誤:", err));
  }, []);

  // 當影廳或票種資料尚未讀取完成時顯示載入中
  if (!hallData || ticketTypes.length === 0) {
    return <div style={{ padding: "40px", textAlign: "center" }}>資料載入中...</div>;
  }

  const totalRows = hallData.rowCount;
  const totalCols = hallData.firstRowSeats;

  // 點擊座位切換選取狀態
  const toggleSeat = (row, col) => {
    const seatId = `${row}-${col}`;
    if (soldSeats.includes(seatId)) return;

    const exists = selectedSeats.find((s) => s.id === seatId);

    if (exists) {
      // 取消選取該座位
      setSelectedSeats(selectedSeats.filter((s) => s.id !== seatId));
    } else {
      // 檢查目前是否已經選了雙人票
      const hasDoubleTicket = selectedSeats.some((s) => {
        const typeObj = ticketTypes.find((t) => t.typeId === s.typeId);
        return typeObj && typeObj.typeName.includes('雙人');
      });

      // 若已有雙人票，禁止選擇第 3 個座位
      if (hasDoubleTicket && selectedSeats.length >= 2) {
        alert("已選取雙人票，限定只能選 2 個座位！");
        return;
      }

      // 決定新選座位的預設票種
      let defaultTypeId = ticketTypes[0]?.typeId;
      if (hasDoubleTicket) {
        const doubleTicket = selectedSeats.find((s) => {
          const typeObj = ticketTypes.find((t) => t.typeId === s.typeId);
          return typeObj && typeObj.typeName.includes('雙人');
        });
        defaultTypeId = doubleTicket.typeId;
      }

      setSelectedSeats([
        ...selectedSeats,
        {
          id: seatId,
          row,
          col,
          typeId: defaultTypeId,
        },
      ]);
    }
  };

  // 切換選中座位的票種
  const handleTypeChange = (seatId, newTypeId) => {
    const selectedTypeObj = ticketTypes.find((t) => t.typeId === Number(newTypeId));
    const isDouble = selectedTypeObj && selectedTypeObj.typeName.includes('雙人');

    // 欲切換為雙人票的驗證
    if (isDouble) {
      if (selectedSeats.length !== 2) {
        alert("雙人票限定只能選取 2 個座位，請先選滿 2 個座位再切換！");
        return;
      }
      // 雙人票將選中的 2 個位置同步套用
      setSelectedSeats(
        selectedSeats.map((s) => ({ ...s, typeId: Number(newTypeId) }))
      );
      return;
    }

    // 一般票種直接更新該座位
    setSelectedSeats(
      selectedSeats.map((s) =>
        s.id === seatId ? { ...s, typeId: Number(newTypeId) } : s
      )
    );
  };

  // 計算總金額（雙人票一套 2 個座位僅計算一次價格）
  const totalPrice = selectedSeats.reduce((sum, seat, index, arr) => {
    const typeObj = ticketTypes.find((t) => t.typeId === seat.typeId);
    if (!typeObj) return sum;

    if (typeObj.typeName.includes('雙人')) {
      const firstDoubleIndex = arr.findIndex((s) => {
        const t = ticketTypes.find((item) => item.typeId === s.typeId);
        return t && t.typeName.includes('雙人');
      });
      return index === firstDoubleIndex ? sum + typeObj.basePrice : sum;
    }

    return sum + typeObj.basePrice;
  }, 0);

  // 送出訂單
  const handleBooking = () => {
    if (selectedSeats.length === 0) {
      alert("請先選擇座位！");
      return;
    }

    // 檢查是否有選了雙人票但只點了 1 個座位的情境
    const hasDoubleTicket = selectedSeats.some((s) => {
      const typeObj = ticketTypes.find((t) => t.typeId === s.typeId);
      return typeObj && typeObj.typeName.includes('雙人');
    });

    if (hasDoubleTicket && selectedSeats.length !== 2) {
      alert("選取雙人票時，必須剛好選取 2 個座位才能下單！");
      return;
    }

    const payload = {
      hallId: hallData.hallId,
      showtimeId: 1,
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

    console.log("送出 POST /api/orders Payload:", payload);
    alert(`訂票成功！共預訂 ${selectedSeats.length} 張票，總計 NT$${totalPrice}`);
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.header}>{hallData.hallName} 訂票選位 /booking/:showtimeId</h2>

      <div style={styles.mainContent}>
        {/* 左側：銀幕與座位 */}
        <div style={styles.leftSection}>
          <div style={styles.screenContainer}>
            <div style={styles.screenBar}></div>
            <div style={styles.screenText}>銀幕</div>
          </div>

          <div style={styles.seatGrid}>
            {Array.from({ length: totalRows }).map((_, rowIndex) => {
              const rowNum = rowIndex + 1;
              return (
                <div key={`row-${rowIndex}`} style={styles.seatRow}>
                  {Array.from({ length: totalCols }).map((_, colIndex) => {
                    const colNum = colIndex + 1;
                    const seatId = `${rowNum}-${colNum}`;
                    const isSold = soldSeats.includes(seatId);
                    const isSelected = selectedSeats.some((s) => s.id === seatId);

                    let seatStyle = styles.seatAvailable;
                    if (isSold) seatStyle = styles.seatSold;
                    if (isSelected) seatStyle = styles.seatSelected;

                    return (
                      <div
                        key={`seat-${seatId}`}
                        style={{ ...styles.seatBase, ...seatStyle }}
                        onClick={() => toggleSeat(rowNum, colNum)}
                        title={`${rowNum}排${colNum}號`}
                      />
                    );
                  })}
                </div>
              );
            })}
          </div>

          <div style={styles.legendContainer}>
            <div style={styles.legendItem}>
              <span style={{ ...styles.legendBox, ...styles.seatAvailable }}></span>
              <span>白：未售</span>
            </div>
            <div style={styles.legendItem}>
              <span style={{ ...styles.legendBox, ...styles.seatSelected }}></span>
              <span>藍：預訂</span>
            </div>
            <div style={styles.legendItem}>
              <span style={{ ...styles.legendBox, ...styles.seatSold }}></span>
              <span>灰：已售</span>
            </div>
          </div>

          <div style={styles.footerNote}>
            送出 POST /api/orders → 顯示電影票、列印（1 個座位 1 張票）
          </div>
        </div>

        {/* 右側：動態票種選單與結帳 */}
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>已選 {selectedSeats.length}</h3>

          <div style={styles.seatList}>
            {selectedSeats.length === 0 ? (
              <p style={{ color: '#94a3b8', fontSize: '14px' }}>請點擊左側座位進行選取</p>
            ) : (
              selectedSeats.map((seat) => (
                <div key={seat.id} style={styles.seatListItem}>
                  <span>{seat.row}排{seat.col}號</span>
                  <select
                    value={seat.typeId}
                    onChange={(e) => handleTypeChange(seat.id, e.target.value)}
                    style={styles.select}
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

          <div style={styles.totalSection}>
            <span>合計</span>
            <span style={styles.totalAmount}>NT${totalPrice}</span>
          </div>

          <button style={styles.confirmButton} onClick={handleBooking}>
            確認訂票
          </button>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    backgroundColor: '#f5f6f8',
    borderRadius: '16px',
    padding: '30px 40px',
    maxWidth: '920px',
    margin: '30px auto',
    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.05)',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
  header: {
    margin: '0 0 25px 0',
    fontSize: '22px',
    fontWeight: 'bold',
    color: '#1a1a1a',
  },
  mainContent: {
    display: 'flex',
    gap: '40px',
    alignItems: 'flex-start',
  },
  leftSection: {
    flex: '1',
  },
  screenContainer: {
    marginBottom: '25px',
    textAlign: 'center',
  },
  screenBar: {
    height: '6px',
    backgroundColor: '#9ca3af',
    borderRadius: '3px',
    width: '85%',
    margin: '0 auto 8px auto',
  },
  screenText: {
    color: '#9ca3af',
    fontSize: '14px',
    letterSpacing: '2px',
  },
  seatGrid: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    alignItems: 'center',
    marginBottom: '25px',
  },
  seatRow: {
    display: 'flex',
    gap: '8px',
  },
  seatBase: {
    width: '38px',
    height: '24px',
    borderRadius: '7px',
    boxSizing: 'border-box',
    cursor: 'pointer',
    transition: 'all 0.15s ease',
  },
  seatAvailable: {
    backgroundColor: '#ffffff',
    border: '1.5px solid #d1d5db',
  },
  seatSelected: {
    backgroundColor: '#1d63ed',
    border: '1.5px solid #1d63ed',
  },
  seatSold: {
    backgroundColor: '#cbd5e1',
    border: '1.5px solid #cbd5e1',
    cursor: 'not-allowed',
  },
  legendContainer: {
    display: 'flex',
    gap: '20px',
    fontSize: '14px',
    color: '#64748b',
    marginTop: '15px',
  },
  legendItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },
  legendBox: {
    width: '16px',
    height: '16px',
    borderRadius: '3px',
    display: 'inline-block',
  },
  footerNote: {
    marginTop: '15px',
    fontSize: '13px',
    color: '#94a3b8',
  },
  card: {
    width: '290px',
    backgroundColor: '#ffffff',
    borderRadius: '12px',
    padding: '24px',
    boxShadow: '0 2px 12px rgba(0, 0, 0, 0.06)',
    border: '1px solid #e2e8f0',
  },
  cardTitle: {
    margin: '0 0 16px 0',
    fontSize: '20px',
    fontWeight: 'bold',
    color: '#0f172a',
  },
  seatList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    minHeight: '80px',
    marginBottom: '20px',
  },
  seatListItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontSize: '15px',
    color: '#334155',
  },
  select: {
    padding: '3px 6px',
    borderRadius: '4px',
    border: '1px solid #cbd5e1',
    fontSize: '13px',
  },
  totalSection: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTop: '1px solid #f1f5f9',
    paddingTop: '15px',
    marginBottom: '20px',
    fontSize: '18px',
    fontWeight: 'bold',
  },
  totalAmount: {
    color: '#0f172a',
  },
  confirmButton: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#1d63ed',
    color: '#ffffff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    fontWeight: '600',
    cursor: 'pointer',
  },
};

export default App;