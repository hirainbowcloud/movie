// 表單錯誤訊息清單（前端驗證或後端回傳的錯誤）
function FormErrors({ errors }) {
  if (!errors || errors.length === 0) return null;
  return (
    <ul className="form-errors">
      {errors.map((msg) => <li key={msg}>{msg}</li>)}
    </ul>
  );
}

export default FormErrors;
