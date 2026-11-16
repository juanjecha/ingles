import React, {useEffect, useState} from 'react'
import api from '../api/axiosConfig'

const LEVELS = ['A1', 'A2', 'B1', 'B2', 'C1']

const nextLevel = (level) => {
  const i = LEVELS.indexOf(level)
  return i >= 0 && i < LEVELS.length -1 ? LEVELS[i +1] :null
}

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

  const newStudentLevel = async (student) => {
    const current = student.level ?? 'A1'
    const next = nextLevel(current)
    if (!next)return
    if (!window.confirm(`Promote ${student.name} from ${current} to ${next}?`)) return
  

    try {
        const res = await api.post(`students/${student.id}/promote`)
        setStudents((prev) => prev.map((s) =>
          s.id === student.id ? { ...s, level: res.data.level } : s
        ))
    } catch (err) {
      alert(err.response?.data?.messages?.error || 'The level could not be updated')
    }
  }
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
                <th>Promotion</th>
              </tr>
            </thead>

            <tbody>
              {students.map((s) => {
                const level = s.level ?? 'A1'
                const next = nextLevel(level)
                return (
                  <tr key={s.id}>
                  <td>{s.enrollment}</td>
                  <td>{s.name}</td>
                  <td>{s.email}</td>
                  <td>{s.course}</td>
                  <td>{level}</td>

                  <td>
                    {next ? (
                      <button className='btn btn-sm btn-outline-success' onClick={() => newStudentLevel}>
                        <i className='bi bi-arrow-up-circle me-1'></i> Promote to {next}
                      </button>
                    ) : (
                      <span className='text-muted'> Highest Level</span>
                    )}
                  </td>
                </tr>
                )
              })
              }
            </tbody>

          </table>
        </>
      )}
    </div>
  )
}