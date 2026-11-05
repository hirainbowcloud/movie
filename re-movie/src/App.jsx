import { useState, useEffect } from 'react';

function AdminList() {
  const [member, setMemberData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const response = await fetch('http://localhost:8080/api/member?page=1&size=10');

        if (!response.ok) {
          throw new Error(`HTTP 錯誤！狀態碼：${response.status}`);
        }
        // 修正 1：加上 await
        const result = await response.json();

        // 依據後端格式調整（若後端回傳包在 content 或 data 屬性中，請改為 result.content 或 result.data）
        setMemberData(result.content); //存入 content 陣列
        console.log('取得資料：', result.content);
        setTotalPages(result.totalPages)

      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    // 修正 2：fetchData() 移出函式本體，在此處正式呼叫執行
    fetchData();
  }, [page]);

 const handlePrevPage = () => {
    if (page > 1) {
      setPage((prev) => prev - 1);
    }
  };

  const handleNextPage = () => {
    if (page < totalPages) {
      setPage((prev) => prev + 1);
    }
  };

  // 狀態判斷
  if (loading) return <p>載入中...</p>;
  if (error) return <p style={{ color: 'red' }}>錯誤訊息：{error}</p>;

  // 修正 3：加入成功載入時渲染畫面的 JSX
  return (
 <div style={{ padding: '20px' }}>
    <h2>會員列表</h2>
    <table border="1" cellPadding="8" style={{ borderCollapse: 'collapse' }}>
      <thead>
        <tr>
          <th>編號</th>
          <th>姓名</th>
          <th>帳號</th>
          <th>電話</th>
          <th>Email</th>
        </tr>
      </thead>
      <tbody>
        {member.map((member) => (
          <tr key={member.memberId}>
            <td>{member.memberId}</td>
            <td>{member.name}</td>
            <td>{member.username}</td>
            <td>{member.phone}</td>
            <td>{member.email}</td>
          </tr>
        ))}
      </tbody>
    </table>
    
    {/* 4. 分頁控制區塊 */}
      <div style={{ marginTop: '16px', display: 'flex', alignItems: 'center', gap: '12px' }}>
        <button onClick={handlePrevPage} disabled={page <= 1}>
          上一頁
        </button>

        <span>
          第 <strong>{page}</strong> 頁 / 共 <strong>{totalPages}</strong> 頁
        </span>

        <button onClick={handleNextPage} disabled={page >= totalPages}>
          下一頁
        </button>
      </div>

  </div>
  );
}

// 修正 4：匯出元件
export default AdminList;