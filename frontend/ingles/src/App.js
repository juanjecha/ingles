import {useState}  from 'react'
import {Routes, Route, NavLink} from 'react-router-dom'
import MainPage from './components/MainPage';
import SubirMaterial from './components/SubirMaterial';
import VerPuntuaciones from './components/VerPuntuaciones';
import ListaAlumnos from './components/ListaAlumnos';
import ListMaterial from './components/ListMaterial';

export default function App() {
  const [materiales, setMateriales] = useState([
    { titulo: 'Verb To Be', tipo: 'PDF', descripcion: 'Ejercicios básicos' },
  ])
  const alumnos = [
    { id: 1, nombre: 'Ana López', nivel: 'A1' },
    { id: 2, nombre: 'Carlos Pérez', nivel: 'A2' },
  ]
  const puntuaciones = [
    { alumno: 'Ana López', examen: 'Unidad 1', nota: 90 },
    { alumno: 'Carlos Pérez', examen: 'Unidad 1', nota: 78 },
  ]

  return (
    <>
      <nav className="menu">
        <NavLink to="/">Inicio</NavLink>
        <NavLink to="/subir">Subir material</NavLink>
        <NavLink to="/puntuaciones">Ver puntuaciones</NavLink>
        <NavLink to="/alumnos">Lista de alumnos</NavLink>
        <NavLink to="/materiales">Lista de material</NavLink>
      </nav>

      <main>
        <Routes>
          <Route path="/" element={<MainPage />} />
          <Route path="/subir" element={<SubirMaterial agregar={(m) => setMateriales([...materiales, m])} />} />
          <Route path="/puntuaciones" element={<VerPuntuaciones datos={puntuaciones} />} />
          <Route path="/alumnos" element={<ListaAlumnos datos={alumnos} />} />
          <Route path="/materiales" element={<ListMaterial datos={materiales} />} />
        </Routes>
      </main>
    </>
  )
}