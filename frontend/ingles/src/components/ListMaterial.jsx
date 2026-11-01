import React, { useEffect, useState } from 'react'
import api from '../api/axiosConfig'

//get images from public/uploads
const FILES_URL = api.defaults.baseURL.replace(/\/api\/?$/, '') + '/uploads/'

export default function ListMaterial({ datos }) {
  const [materials, setMaterials] = useState([])
  const [loading, setLoading] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
      api.get('materials').then((res) => setMaterials(res.data))
      .catch((err) => {
        console.error(err)
        setError('Error charging resources')
      })
      .finally(() => setLoading(false))
    }, []
  )

  if (loading) return <p>Charging...</p>
  if (error) return <p>{error}</p>

  return (
    <div>
      <h2>Available resources</h2>

      {materials.length === 0 ? (
        <p>No resources yet</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Image</th>
              <th>Word</th>
              <th>Category</th>
              <th>Level</th>
            </tr>
          </thead>

          <tbody>
            {materials.map((m) => (
            <tr key={m.id}>
              <td>
                <img src={FILES_URL + m.img} alt={m.name} style={{height: 60}}/>
              </td>
              <td>{m.name}</td>
              <td>{m.category}</td>
              <td>{m.level}</td>
            </tr>))}
          </tbody>
        </table>
      )}
    </div>
  )
}