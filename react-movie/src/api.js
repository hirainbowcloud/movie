// 前端共用：後端網址、錯誤訊息、登入會員（存在瀏覽器 localStorage）

export const API = 'http://localhost:8080';

// 把後端回傳的錯誤轉成一句話
// 後端錯誤有三種格式：純文字、{ error: "..." }、{ errors: ["...", "..."] }
export async function readError(res) {
  const text = await res.text();
  try {
    const body = JSON.parse(text);
    if (Array.isArray(body.errors)) return body.errors.join('、');
    if (body.error) return body.error;
  } catch {
    // 不是 JSON，就是純文字
  }
  return text || `發生錯誤（HTTP ${res.status}）`;
}

const MEMBER_KEY = 'member';

// 取得目前登入的會員：{ memberId, username, name, phone, email }，沒登入回傳 null
export function getMember() {
  try {
    const json = localStorage.getItem(MEMBER_KEY);
    return json ? JSON.parse(json) : null;
  } catch {
    return null;
  }
}

export function saveMember(member) {
  try {
    localStorage.setItem(MEMBER_KEY, JSON.stringify(member));
  } catch {
    // 瀏覽器不允許儲存時就略過
  }
}

export function clearMember() {
  try {
    localStorage.removeItem(MEMBER_KEY);
  } catch {
    // 略過
  }
}

// ==================== 後台：管理者登入 ====================
const ADMIN_KEY = 'admin';

// 取得目前登入的管理者：{ adminId, username, name }，沒登入回傳 null
export function getAdmin() {
  try {
    const json = localStorage.getItem(ADMIN_KEY);
    return json ? JSON.parse(json) : null;
  } catch {
    return null;
  }
}

export function saveAdmin(admin) {
  try {
    localStorage.setItem(ADMIN_KEY, JSON.stringify(admin));
  } catch {
    // 略過
  }
}

export function clearAdmin() {
  try {
    localStorage.removeItem(ADMIN_KEY);
  } catch {
    // 略過
  }
}

// ==================== 呼叫後端的共用函式 ====================

// 送出請求：成功回傳 JSON，失敗丟出錯誤訊息（後台各頁共用）
// body 是一般物件 → 自動轉 JSON；body 是 FormData（上傳檔案）→ 直接送
export async function request(url, { method = 'GET', body } = {}) {
  const options = { method };
  if (body instanceof FormData) {
    options.body = body;   // 瀏覽器會自己加 multipart 的 Content-Type
  } else if (body !== undefined) {
    options.headers = { 'Content-Type': 'application/json' };
    options.body = JSON.stringify(body);
  }

  const res = await fetch(API + url, options);
  if (!res.ok) throw new Error(await readError(res));

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// 取得分頁清單：後端沒有資料時回 404，這裡當成空清單
export async function requestPage(url) {
  const res = await fetch(API + url);
  if (res.status === 404) return { content: [], page: 1, totalPages: 1, totalElements: 0 };
  if (!res.ok) throw new Error(await readError(res));
  return res.json();
}
