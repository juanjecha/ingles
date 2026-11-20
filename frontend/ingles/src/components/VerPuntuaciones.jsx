import React, { useEffect, useState } from 'react'
import api from '../api/axiosConfig'

export default function VerPuntuaciones({ datos }) {
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [open, setOpen] = useState(null)

  useEffect(() => {
    api.get('grades').then((res) => setRows(res.data.data))
    .catch((err) => {
      setError('Grades could not be loaded')
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <p>Loading...</p>
  if (error) return <p>{error}</p>

  return (
    <div>
      <h2><i className='bi bi-bar-chart me-2'></i>Scores</h2>

      {rows.length === 0 ? (
        <p>There are not students yet</p>
      ) : (
        <table className='table table-striped table-hover align-middle'>
          <thead>
            <tr>
              <th>Enrollment</th>
              <th>Name</th>
              <th>Level</th>
              <th>Tests</th>
              <th>Average</th>
              <th>Suggested level</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            {rows.map((s) => (
              <React.Fragment key={s.id}>
                <tr>
                  <td>{s.enrollment}</td>
                  <td>{s.name}</td>
                  <td>{s.level}</td>
                  <td>{s.completed}</td>
                  <td>{s.completed} / {s.total}</td>
                  <td>{s.enabled && s.average !== null ? `${s.average}%` : '—'}</td>
                  <td>
                    {s.enabled ? (
                      s.can_promote
                    ? <span className='badge bg-success'>{s.suggested_level} · Ready to promote</span>
                    : <span className='badge bg-secondary'>{s.suggested_level} · Highest level</span>
                  ) : (
                    <span className='text-muted'>Pending</span>
                  )}
                  </td>

                  <td>
                    {s.enabled && (
                      <button
                        className='btn btn-sm btn-outline-secondary'
                        onClick={() => setOpen === s.id ? null : s.id}
                        >
                          {open === s.id ? 'Hide' : 'Details'}
                        </button>
                    )}
                  </td>
                </tr>

                {open === s.id && (
                  <tr>
                    <td colSpan={7}>
                      <table className='table table-sm mb-0'>
                        <thead>
                          <tr><th>Category</th><th>Score</th></tr>
                        </thead>

                        <tbody>
                          {s.categories.map((c) => (
                            <tr key={c.category_id}>
                              <td style={{ textTransform: 'capitalize' }}>{c.name}</td>
                              <td>{c.score !== null ? `${c.score}%` : '-'}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </td>
                  </tr>
                )}
              </React.Fragment>
            ))}
          </tbody>  
        </table>
      )}
      <p className='text-muted small'>
        The grades table is enabled when the student passes every test of the categories assigned to them.
        Promotion is done manually from the Students view.
      </p>
    </div>
  )
}