import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { API } from '../api.js';
import SectionHeading from '../components/SectionHeading.jsx';
import '../css/HomePage.css';

// 分級 → 顏色（台灣電影分級的代表色）
const RATING_CLASS = {
  普遍級: 'rating-g',
  保護級: 'rating-p',
  輔12級: 'rating-pg12',
  輔15級: 'rating-pg15',
  限制級: 'rating-r',
};

const PAGE_SIZE = 3;    // 每頁最多 3 部電影

function HomePage() {
  const [movies, setMovies] = useState(null);   // null = 還沒載入
  const [total, setTotal] = useState(0);        // 上映中的電影總數
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  // page 改變就重新呼叫 API，每次拿 3 部
  useEffect(() => {
    fetch(`${API}/api/movie?page=${page}&size=${PAGE_SIZE}`)
      .then((res) => {
        // 後端沒有上架電影時回 404，當成空清單
        if (res.status === 404) return { content: [], totalElements: 0, totalPages: 1 };
        if (!res.ok) throw new Error('電影讀取失敗（HTTP ' + res.status + '）');
        return res.json();
      })
      .then((data) => {
        setMovies(data.content);
        setTotal(data.totalElements ?? data.content.length);
        setTotalPages(data.totalPages || 1);
      })
      .catch((err) => setError(err.message));
  }, [page]);

  // 找出海報（imageType = POSTER），網址前面加上 http://localhost:8080
  const getPosterUrl = (movie) => {
    const poster = (movie.images || []).find((img) => img.imageType === 'POSTER');
    return poster ? API + poster.imageUrl : null;
  };

  // 卡片上的小字：科幻 · 128 分鐘
  const metaLine = (movie) =>
    [movie.genre, movie.durationMin ? `${movie.durationMin} 分鐘` : null].filter(Boolean).join(' · ');

  // 電影清單區塊：依狀態顯示訊息或卡片
  const renderMovies = () => {
    if (error) return <div className="page-message">{error}</div>;
    if (movies === null) return <div className="page-message">資料載入中...</div>;
    if (movies.length === 0) return <div className="page-message">目前沒有上架中的電影</div>;

    // 頁碼 [1, 2, ...]
    const pageList = Array.from({ length: totalPages }, (_, i) => i + 1);

    return (
      <>
        <div className="movie-grid">
          {movies.map((movie) => {
            const posterUrl = getPosterUrl(movie);
            return (
              <div
                key={movie.movieId}
                className="movie-card"
                onClick={() => navigate(`/movie/${movie.movieId}`)}
              >
                {/* 海報：滑鼠移上去會微微放大 */}
                <div className="poster-frame">
                  {posterUrl ? (
                    <img className="movie-poster" src={posterUrl} alt={movie.title} />
                  ) : (
                    <div className="movie-poster no-image">暫無海報</div>
                  )}
                </div>

                <div className="movie-body">
                  <div className="movie-tags">
                    {movie.rating && (
                      <span className={`rating-chip ${RATING_CLASS[movie.rating] || ''}`}>{movie.rating}</span>
                    )}
                    <span className="movie-meta">{metaLine(movie)}</span>
                  </div>

                  <h3 className="movie-title">{movie.title}</h3>
                  {movie.synopsis && <p className="movie-synopsis">{movie.synopsis}</p>}

                  <div className="movie-foot">
                    <span className="movie-date">{movie.releaseDate ? `${movie.releaseDate} 上映` : ''}</span>
                    <span className="movie-cta">訂票</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* 頁碼 1 2 3，只有一頁時不顯示 */}
        {totalPages > 1 && (
          <div className="pager">
            {pageList.map((p) => (
              <button key={p} className={p === page ? 'active' : ''} onClick={() => setPage(p)}>
                {p}
              </button>
            ))}
          </div>
        )}
      </>
    );
  };

  return (
    <>
      {/* 深色主視覺 */}
      <section className="hero">
        <div className="hero-inner">
          <p className="hero-eyebrow">MOVIE POS 線上訂票</p>
          <h1 className="hero-title">
            好電影，<br />從選一個好位子開始。
          </h1>
          <p className="hero-text">挑一部電影、選場次、選座位，三個步驟完成訂票，電影票直接在「我的訂單」列印。</p>
          <div className="hero-actions">
            <a href="#now-showing" className="btn btn-primary">立即訂票</a>
            <button className="btn btn-outline-light" onClick={() => navigate('/orders')}>我的訂單</button>
          </div>
        </div>
      </section>

      <div className="page-container" id="now-showing">
        <div className="section-row">
          <SectionHeading label="現正熱映" title="本週上映電影" />
          {movies !== null && movies.length > 0 && (
            <p className="movie-count">
              目前上映 <strong>{total}</strong> 部電影
            </p>
          )}
        </div>
        {renderMovies()}
      </div>
    </>
  );
}

export default HomePage;
