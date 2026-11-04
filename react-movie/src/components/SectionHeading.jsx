import '../css/SectionHeading.css';

// 區塊標題：藍色外框膠囊「現正熱映」，再接一行大標題
// 用法：<SectionHeading label="現正熱映" title="本週上映電影" />
function SectionHeading({ label, title }) {
  return (
    <div className="section-heading">
      <span className="section-pill">
        <span className="section-label">{label}</span>
      </span>
      {title && <h2 className="section-title">{title}</h2>}
    </div>
  );
}

export default SectionHeading;
