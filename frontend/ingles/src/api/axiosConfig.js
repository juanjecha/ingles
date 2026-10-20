import axios from 'axios';
 
const api = axios.create({
    baseURL: 'http://localhost/ingles/backend/ingles_back/public/api'
});
 
// Interceptor para adjuntar JWT en cada request
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('jwt_token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);
 
export default api;