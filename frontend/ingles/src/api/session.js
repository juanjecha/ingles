export function getTokenExp(token) {
  if (!token) return null;
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));
    return payload.exp ? payload.exp * 1000 : null;
  } catch {
    return null;
  }
}

export function clearSession(expired = false) {
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user');
  if (expired) {
    sessionStorage.setItem('expired_session', '1');
  }
}