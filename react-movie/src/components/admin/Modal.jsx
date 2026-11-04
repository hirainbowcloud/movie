// 彈出視窗：新增 / 編輯表單共用
// 用法：<Modal title="新增影廳" onClose={...}>表單內容</Modal>
function Modal({ title, onClose, children, wide = false }) {
  return (
    // 點灰色背景關閉；點視窗本身不關閉（stopPropagation）
    <div className="modal-backdrop" onClick={onClose}>
      <div className={wide ? 'modal modal-wide' : 'modal'} onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h3>{title}</h3>
          <button className="modal-close" onClick={onClose} aria-label="關閉">×</button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}

export default Modal;
