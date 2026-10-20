import React from 'react'
export default function VerPuntuaciones({ datos }) {
  return (
    <div>
      <h2>Puntuaciones</h2>
      <table>
        <thead><tr><th>Alumno</th><th>Examen</th><th>Nota</th></tr></thead>
        <tbody>
          {datos.map((p, i) => (
            <tr key={i}><td>{p.alumno}</td><td>{p.examen}</td><td>{p.nota}</td></tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}