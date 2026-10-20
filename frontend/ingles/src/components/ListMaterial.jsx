import React from 'react'
export default function ListMaterial({ datos }) {
  return (
    <div>
      <h2>Lista de material</h2>
      <ul>
        {datos.map((m, i) => (
          <li key={i}><strong>{m.titulo}</strong> ({m.tipo}) — {m.descripcion}</li>
        ))}
      </ul>
    </div>
  )
}