import React from 'react'
import {Link} from 'react-router-dom'

export default function MainPage() {
  return (
    <div>
      <h1>Bienvenido a la clase de Inglés</h1>
      <p>Elige una opción:</p>
      <div className="botones">
        <Link to="/subir">Subir material</Link>
        <Link to="/puntuaciones">Ver puntuaciones</Link>
        <Link to="/alumnos">Lista de alumnos</Link>
        <Link to="/materiales">Lista de material</Link>
      </div>
    </div>
  )
}