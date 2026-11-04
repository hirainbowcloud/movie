import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { API } from '../api.js';
import SectionHeading from '../components/SectionHeading.jsx';
import '../css/MovieDetail.css';

// 電影介紹與場次：/movie/:movieId
function MovieDetailPage() {
  const { movieId } = useParams();
  const navigate = useNavigate();
  const [movie, setMovie] = useState(null);
  const [showtimes, setShowtimes] = useState([]);
  const [error, setError] = useState('');
  const [stillIndex, setStillIndex] = useState(0);   // 目前顯示第幾張劇照（從 0 開始）

  useEffect(() => {
    // 1. 電影資料
    fetch(`${API}/api/movie/${movieId}`)
      .then((res) => {
        if (res.status === 404) throw new Error('找不到這部電影');
        if (!res.ok) throw new Error('電影讀取失敗（HTTP ' + res.status + '）');
        return res.json();
      })
      .then((data) => setMovie(data))
      .catch((err) => setError(err.message));

    // 2. 今天以後的場次（回傳陣列，沒有場次是 []）
    fetch(`${API}/api/showtime/upcoming?movieId=${movieId}`)
      .then((res) => {
        if (!res.ok) throw new Error('場次讀取失敗（HTTP ' + res.status + '）');
        return res.json();
      })
      .then((data) => setShowtimes(data))
      .catch((err) => setError(err.message));
  }, [movieId]);

  if (error) return <div className="page-message">{error}</div>;
  if (!movie) return <div className="page-message">資料載入中...</div>;

  const images = movie.images || [];
  const poster = images.find((img) => img.imageType === 'POSTER');

  // 劇照：imageType = STILL，依 sortOrder 排序（1、2、3…）
  const stills = images
    .filter((img) => img.imageType === 'STILL')
    .sort((a, b) => a.sortOrder - b.sortOrder);

  // 上一張 / 下一張：到最後一張再按下一張會回到第一張
  const prevStill = () => setStillIndex((stillIndex - 1 + stills.length) % stills.length);
  const nextStill = () => setStillIndex((stillIndex + 1) % stills.length);

  // "2026-11-10" → "11/10"；"13:00:00" → "13:00"
  const shortDate = (d) => d.slice(5).replace('-', '/');
  const shortTime = (t) => t.slice(0, 5);

  // 資料可能是空的，用陣列把有值的組起來：普遍級 · 128 分鐘 · 科幻
  const infoLine = [
    movie.rating,
    movie.durationMin ? `${movie.durationMin} 分鐘` : null,
    movie.genre,
  ].filter(Boolean).join(' · ');

  return (
    <div className="detail">
      {/* 上半部：海報 + 電影資料 */}
      <div className="detail-top">
        {/* 左邊：劇照，可以按左右箭頭或下方縮圖切換 */}
        <div className="still-gallery">
          {stills.length > 0 ? (
            <div className="still-viewer">
              <img
                className="still-main"
                src={API + stills[stillIndex].imageUrl}
                alt={`${movie.title} 劇照 ${stillIndex + 1}`}
              />

              {stills.length > 1 && (
                <>
                  <button className="still-arrow left" onClick={prevStill}>‹</button>
                  <button className="still-arrow right" onClick={nextStill}>›</button>
                  <span className="still-counter">{stillIndex + 1} / {stills.length}</span>
                </>
              )}
            </div>
          ) : poster ? (
            // 沒有劇照時改顯示海報
            <img className="detail-poster" src={API + poster.imageUrl} alt={movie.title} />
          ) : (
            <div className="still-main no-image">暫無圖片</div>
          )}

          {stills.length > 1 && (
            <div className="still-thumbs">
              {stills.map((img, i) => (
                <img
                  key={img.imageId}
                  className={i === stillIndex ? 'still-thumb active' : 'still-thumb'}
                  src={API + img.imageUrl}
                  alt={`縮圖 ${i + 1}`}
                  onClick={() => setStillIndex(i)}
                />
              ))}
            </div>
          )}
        </div>

        <div className="detail-info">
          <h2 className="detail-title">{movie.title}</h2>
          {infoLine && <p className="detail-meta">{infoLine}</p>}
          {movie.director && <p className="detail-meta">導演 {movie.director}</p>}
          {movie.actors && <p className="detail-meta">演員 {movie.actors}</p>}
          {movie.releaseDate && <p className="detail-meta">上映日期 {movie.releaseDate}</p>}
          <div className="detail-synopsis">{movie.synopsis || '暫無簡介'}</div>
        </div>
      </div>

      {/* 預告片（有才顯示） */}
      {movie.trailerUrl && (
        <video className="detail-trailer" src={API + movie.trailerUrl} controls />
      )}

      {/* 下半部：場次按鈕 */}
      <SectionHeading label="場次" title="選擇觀看時間" />
      {showtimes.length === 0 ? (
        <p className="hint">目前沒有可訂票的場次</p>
      ) : (
        <div className="showtime-list">
          {showtimes.map((s) => (
            <button
              key={s.showtimeId}
              className="showtime-button"
              onClick={() => navigate(`/booking/${s.showtimeId}`)}
            >
              {shortDate(s.showDate)} {shortTime(s.startTime)} {s.hallName}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export default MovieDetailPage;
