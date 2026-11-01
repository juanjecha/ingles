import axios from 'axios';
 
const api = axios.create({
    baseURL: 'http://localhost/ingles/backend/ingles_back/public/api'
});
 
// to attach JWT to every request
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

// Once server responds 401 logout is executed
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;
        const url = error.config?.url || '';
        const isLoggedIn = url.includes('login/');

        if (status === 401 && !isLoggedIn) {
            localStorage.removeItem('jwt_token');
            localStorage.removeItem('user');
            sessionStorage.setItem('expired_session', '1');
            if (window.location.pathname !== '/') {
                window.localStorage.assign('/');
            }
        }
        return Promise.reject(error);
    })
 
export default api;