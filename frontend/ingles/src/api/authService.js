//const API_URL = 'http://localhost/backend/public';
const API_URL = './api';
export const loginUser = async (email, password) => {
  const response = await fetch(`${API_URL}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });
 
  const data = await response.json();
 
  if (!response.ok) {
    throw new Error(data.error || 'Error al iniciar sesión');
  }
 
  // Guardar el JWT en el almacenamiento local
  if (data.token) {
    localStorage.setItem('token', data.token);
  }
 
  return data;
};
 
export const getPerfilPrivado = async () => {
  const token = localStorage.getItem('token');
const API_URL = "http://localhost/backend/public";
  const response = await fetch(`${API_URL}/perfil`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
 
  if (!response.ok) {
    throw new Error('No autorizado');
  }
 
  return await response.json();
};