import { useEffect } from 'react';
import { clearSession, getTokenExp } from './api/session';
import {Routes, Route, NavLink, useLocation, useNavigate} from 'react-router-dom';
import Login from './components/login';
import MainPage from './components/MainPage';
import SubirMaterial from './components/SubirMaterial';
import VerPuntuaciones from './components/VerPuntuaciones';
import ListaAlumnos from './components/ListaAlumnos';
import ListMaterial from './components/ListMaterial';

function getUser() {
  try {
    return JSON.parse(localStorage.getItem('user')) || {};
  } catch {
    return {};
  }
}

export default function App() {
  const puntuaciones = [
    { alumno: 'Ana López', examen: 'Unidad 1', nota: 90 },
    { alumno: 'Carlos Pérez', examen: 'Unidad 1', nota: 78 },
  ]
  const location = useLocation();
  const navigate = useNavigate();
  const esLogin = location.pathname === '/';
  const user = getUser;

  useEffect(() => {
    if (esLogin) return;

    const closeSession = (exp) => {
      clearSession(exp);
      navigate('/', {replace: true });
    };

    const token = localStorage.getItem('jwt_token');
    const exp = getTokenExp(token);

    //without token, go to login
    if (!exp || exp <= Date.now()) {
      closeSession(!!token);
      return;
    }

    //if it stays in the same view, logout once token expires
    const id = setTimeout(() => closeSession(true), exp - Date.now());
    return () => clearTimeout(id);
  }, [location.pathname, esLogin, navigate]
    
);

  const handleLogout = () => {
    clearSession();
    navigate('/', { replace: true});
  }

  return (
    <>

    {!esLogin && (
      <nav className="navbar navbar-expand navbar-dark px-3 flex-wrap" style={{background: '#1e3a5f'}}>
        <div className='navbar-nav me-auto flex-wrap'>

          <NavLink className="nav-link" to="/main">
            <i className='bi bi-house-door me-1'></i> Main
          </NavLink>

          <NavLink className="nav-link" to="/upload">
            <i className='bi bi-upload me-1'></i> New content
          </NavLink>

          <NavLink className="nav-link" to="/scores">
            <i className='bi bi-bar-chart me-1'></i> Scores
          </NavLink>

          <NavLink className="nav-link" to="/students">
            <i className='bi bi-people me-1'></i> Students
          </NavLink>

          <NavLink className="nav-link" to="/resources">
            <i className='bi bi-collection me-1'></i> Resources
          </NavLink>
        </div>

        <div className="d-flex align-items-center gap-3">
          {user.email && (
            <span className="navbar-text text-white-50 small">
              <i className="bi bi-person-circle me-1"></i>{user.email}
            </span>
          )}
          <button type="button" className="btn btn-outline-light btn-sm" onClick={handleLogout}>
            <i className="bi bi-box-arrow-right me-1"></i>Log out
          </button>
        </div>

      </nav>
    )}

      <main>
        <Routes>
          <Route path="/" element={<Login />}/>
          <Route path="/main" element={<MainPage />} />
          <Route path="/upload" element={<SubirMaterial />} />
          <Route path="/scores" element={<VerPuntuaciones datos={puntuaciones} />} />
          <Route path="/students" element={<ListaAlumnos />} />
          <Route path="/resources" element={<ListMaterial/>} />
        </Routes>
      </main>
    </>
  )
}