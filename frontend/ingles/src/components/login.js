import React, { useState } from 'react';
import api from '../api/axiosConfig';
import { useNavigate } from 'react-router-dom';

export default function AuthView() {
  // Control de redirección hacia la vista principal
  const navigate = useNavigate();

  // Estado para controlar la visibilidad del modal
  const [showRegisterModal, setShowRegisterModal] = useState(false);

  // Estados para el formulario de Login
  const [loginData, setLoginData] = useState({
    email: '',
    password: '',
    remember: false
  });

  // Estados para el formulario de Registro
  const [registerData, setRegisterData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const handleLoginChange = (e) => {
    const { name, value, type, checked } = e.target;
    setLoginData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleRegisterChange = (e) => {
    const { name, value } = e.target;
    setRegisterData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    try {
        const res = await api.post('login/teacher', {
            email: loginData.email,
            password: loginData.password
        });
        localStorage.setItem('jwt_token', res.data.token);
        localStorage.setItem('user', JSON.stringify(res.data.user ?? {}));
        navigate('/inicio');
    } catch (err) {
        const msg = err.response?.data?.messages?.error || "No se pudo iniciar sesión";
        alert(msg);
    }
  };

  const handleRegisterSubmit = async (e) => {
  e.preventDefault();
  if (registerData.password !== registerData.confirmPassword) {
    alert('Las contraseñas no coinciden');
    return;
  }
  try {
    await api.post('register/teacher', {
      name: registerData.name,
      email: registerData.email,
      password: registerData.password
    });
    alert('Cuenta creada. Ya puedes iniciar sesión.');
    setRegisterData({ name: '', email: '', password: '', confirmPassword: '' });
    setShowRegisterModal(false);
  } catch (err) {
    const msg = err.response?.data?.messages?.error || 'No se pudo crear la cuenta';
    alert(msg);
  }
};

  return (
    <div className="container min-vh-100 d-flex justify-content-center align-items-center py-5">
      {/* Tarjeta de Login */}
      <div className="card shadow-sm w-100" style={{ maxWidth: '420px' }}>
        <div className="card-body p-4">
          <div className="text-center mb-4">
            <h3 className="card-title fw-bold">Iniciar Sesión</h3>
            <p className="text-muted small">Ingresa tus credenciales para acceder</p>
          </div>

          <form onSubmit={handleLoginSubmit}>
            <div className="mb-3">
              <label htmlFor="loginEmail" className="form-label">
                Correo electrónico
              </label>
              <input
                type="email"
                className="form-control"
                id="loginEmail"
                name="email"
                placeholder="nombre@ejemplo.com"
                value={loginData.email}
                onChange={handleLoginChange}
                required
              />
            </div>

            <div className="mb-3">
              <label htmlFor="loginPassword" className="form-label">
                Contraseña
              </label>
              <input
                type="password"
                className="form-control"
                id="loginPassword"
                name="password"
                placeholder="••••••••"
                value={loginData.password}
                onChange={handleLoginChange}
                required
              />
            </div>

            <div className="d-grid mb-3">
              <button type="submit" className="btn btn-primary">
                Entrar
              </button>
            </div>
          </form>

          <hr />

          <div className="text-center">
            <span className="small text-muted">¿No tienes cuenta? </span>
            <button
              type="button"
              className="btn btn-link p-0 small text-decoration-none fw-bold"
              onClick={() => setShowRegisterModal(true)}
            >
              Regístrate aquí
            </button>
          </div>
        </div>
      </div>

      {/* Modal de Registro */}
      {showRegisterModal && (
        <>
          <div
            className="modal fade show d-block"
            tabIndex="-1"
            role="dialog"
            aria-modal="true"
          >
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title fw-bold">Crear Cuenta</h5>
                  <button
                    type="button"
                    className="btn-close"
                    aria-label="Close"
                    onClick={() => setShowRegisterModal(false)}
                  ></button>
                </div>

                <form onSubmit={handleRegisterSubmit}>
                  <div className="modal-body">
                    <div className="mb-3">
                      <label htmlFor="regName" className="form-label">
                        Nombre completo
                      </label>
                      <input
                        type="text"
                        className="form-control"
                        id="regName"
                        name="name"
                        placeholder="Juan Pérez"
                        value={registerData.name}
                        onChange={handleRegisterChange}
                        required
                      />
                    </div>

                    <div className="mb-3">
                      <label htmlFor="regEmail" className="form-label">
                        Correo electrónico
                      </label>
                      <input
                        type="email"
                        className="form-control"
                        id="regEmail"
                        name="email"
                        placeholder="nombre@ejemplo.com"
                        value={registerData.email}
                        onChange={handleRegisterChange}
                        required
                      />
                    </div>

                    <div className="row">
                      <div className="col-md-6 mb-3">
                        <label htmlFor="regPassword" className="form-label">
                          Contraseña
                        </label>
                        <input
                          type="password"
                          className="form-control"
                          id="regPassword"
                          name="password"
                          placeholder="••••••••"
                          value={registerData.password}
                          onChange={handleRegisterChange}
                          required
                        />
                      </div>
                      <div className="col-md-6 mb-3">
                        <label htmlFor="regConfirmPassword" className="form-label">
                          Confirmar
                        </label>
                        <input
                          type="password"
                          className="form-control"
                          id="regConfirmPassword"
                          name="confirmPassword"
                          placeholder="••••••••"
                          value={registerData.confirmPassword}
                          onChange={handleRegisterChange}
                          required
                        />
                      </div>
                    </div>
                  </div>

                  <div className="modal-footer">
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => setShowRegisterModal(false)}
                    >
                      Cancelar
                    </button>
                    <button type="submit" className="btn btn-success">
                      Crear Cuenta
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>

          {/* Backdrop oscuro para el modal */}
          <div
            className="modal-backdrop fade show"
            onClick={() => setShowRegisterModal(false)}
          ></div>
        </>
      )}
    </div>
  );
}