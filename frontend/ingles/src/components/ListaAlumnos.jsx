import React from 'react'
export default function ListaAlumnos({ datos }) {
  return (
    <div>
      <h2>Alumnos inscritos</h2>
      <ul>
        {datos.map((a) => (
          <li key={a.id}>{a.nombre} — Nivel {a.nivel}</li>
        ))}
      </ul>
    </div>
  )
}