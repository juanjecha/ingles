import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { useNavigate } from 'react-router-dom';

export default function AuthView() {
  // Redirection control to the main view
  const navigate = useNavigate();

  // Form login states
  const [loginData, setLoginData] = useState({
    teacher_key: '',
    password: '',
    //remember: false
  });

  const handleLoginChange = (e) => {
    const { name, value } = e.target;
    setLoginData((prev) => ({...prev, [name]:value }));
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    try {
        const res = await api.post('login/teacher', {
            teacher_key: loginData.teacher_key.trim(),
            password: loginData.password
        });
        localStorage.setItem('jwt_token', res.data.token);
        localStorage.setItem('user', JSON.stringify(res.data.user ?? {}));
        navigate('/inicio');
    } catch (err) {
        const msg = err.response?.data?.messages?.error || "Could not log in";
        alert(msg);
    }
  };

  useEffect(() => {
    if (sessionStorage.getItem('expired_session')) {
      sessionStorage.removeItem('expired_session');
      alert('Your session has expired. Try to log in again');
    }
  }, []);

  return (
    <div className="container min-vh-100 d-flex justify-content-center align-items-center py-5">
      {/* Login card */}
      <div className="card shadow-sm w-100" style={{ maxWidth: '420px' }}>
        <div className="card-body p-4">
          <div className="text-center mb-4">
            <h3 className="card-title fw-bold">Login</h3>
            <p className="text-muted small">Put your credentials to get access</p>
          </div>

          <form onSubmit={handleLoginSubmit}>
            <div className="mb-3">
              <label htmlFor="loginKey" className="form-label">
                Teacher Key
              </label>
              <input
                type="text"
                className="form-control"
                id="loginKey"
                name="teacher_key"
                placeholder="NSS1478529"
                value={loginData.teacher_key}
                onChange={handleLoginChange}
                required
              />
            </div>

            <div className="mb-3">
              <label htmlFor="loginPassword" className="form-label">
                Password
              </label>
              <input
                type="password"
                className="form-control"
                id="loginPassword"
                name="password"
                placeholder="••••••••"
                autoComplete='current-password'
                value={loginData.password}
                onChange={handleLoginChange}
                required
              />
            </div>

            <div className="d-grid mb-3">
              <button type="submit" className="btn btn-primary">
                Log in
              </button>
            </div>
          </form>

          <hr />

        </div>
      </div>

    </div>
  );
}