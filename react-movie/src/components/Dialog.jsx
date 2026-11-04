import '../css/Dialog.css';

// 置中的對話框，取代瀏覽器的 alert()
// type：'success' 綠色打勾、'error' 紅色驚嘆號、'info' 藍色
// actions：按鈕陣列 [{ text: '確定', primary: true, onClick: () => {} }]
function Dialog({ type = 'info', title, children, actions }) {
  const icon = { success: '✓', error: '!', info: 'i' }[type];

  return (
    <div className="dialog-backdrop">
      <div className="dialog" role="dialog" aria-modal="true">
        <div className={`dialog-icon ${type}`}>{icon}</div>
        <h3 className="dialog-title">{title}</h3>
        {children && <div className="dialog-body">{children}</div>}
        <div className="dialog-actions">
          {actions.map((a) => (
            <button
              key={a.text}
              className={a.primary ? 'dialog-btn primary' : 'dialog-btn'}
              onClick={a.onClick}
              autoFocus={a.primary}
            >
              {a.text}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

export default Dialog;
