import React, {useEffect, useState} from 'react'
import api from '../api/axiosConfig'

export default function ListaAlumnos({ datos }) {
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('students').then((res) => setStudents(res.data))
    .catch((err) => {
      console.error(err)
      setError("Students could not be showed up")
    }).finally(() => setLoading(false))

  }, [])

  if (loading) return <p>Loading...</p>
  if (error) return <p>{error}</p>

  return (
    <div>
      <h2><i className='bi bi-people me-2'></i>Registered students</h2>

      {students.length === 0 ? (
        <p>There are not students yet</p>
      ) : (
        <>
          <p className='text-muted'> Total: {students.length}</p>
          <table className='table table-striped table-hover align-middle'>
            <thead>
              <tr>
                <th>Enrollment</th>
                <th>Name</th>
                <th>Email</th>
                <th>Course</th>
                <th>Level</th>
              </tr>
            </thead>

            <tbody>
              {students.map((s) => (
                <tr key={s.id}>
                  <td>{s.enrollment}</td>
                  <td>{s.name}</td>
                  <td>{s.email}</td>
                  <td>{s.course}</td>
                  <td>{s.level ?? <span className='text-muted'>Not given</span>}</td>
                </tr>
              ))}
            </tbody>

          </table>
        </>
      )}
    </div>
  )
}