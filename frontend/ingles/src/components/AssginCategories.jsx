import React, { useEffect, useState } from 'react'
import api from '../api/axiosConfig'

export default function AssignCategoriesModal({ student, onClose }) {
  const [level, setLevel] = useState('')
  const [categories, setCategories] = useState([])
  const [selected, setSelected] = useState(new Set())
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.get(`students/${student.id}/categories`)
      .then((res) => {
        setLevel(res.data.level)
        setCategories(res.data.data)
        setSelected(new Set(res.data.data.filter((c) => c.assigned).map((c) => c.id)))
      })
      .catch((err) => {
        console.error(err)
        alert('The categories could not be loaded')
        onClose()
      })
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [student.id])

  const toggle = (id) => {
    setSelected((prev) => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const save = async () => {
    setSaving(true)
    try {
      await api.post(`students/${student.id}/categories`, {
        category_ids: Array.from(selected)
      })
      onClose()
    } catch (err) {
      console.error(err)
      const messages = err.response?.data?.messages
      alert(messages ? Object.values(messages).join('\n') : 'The categories could not be saved')
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <div className="modal-backdrop show"></div>
      <div className="modal d-block" tabIndex="-1" role="dialog">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">
                Categories for {student.name}
                {level && <span className="badge bg-secondary ms-2">{level}</span>}
              </h5>
              <button type="button" className="btn-close" aria-label="Close" onClick={onClose}></button>
            </div>

            <div className="modal-body">
              {loading ? (
                <p>Loading...</p>
              ) : categories.length === 0 ? (
                <p className="text-muted mb-0">
                  There are no published categories for level {level} yet.
                </p>
              ) : (
                <>
                  <p className="text-muted small">
                    Checked categories are enabled for the student. The rest stay locked in the app.
                  </p>
                  <ul className="list-group">
                    {categories.map((c) => (
                      <li key={c.id} className="list-group-item">
                        <div className="form-check">
                          <input
                            className="form-check-input"
                            type="checkbox"
                            id={`cat-${c.id}`}
                            checked={selected.has(c.id)}
                            onChange={() => toggle(c.id)}
                          />
                          <label className="form-check-label" htmlFor={`cat-${c.id}`}>
                            {c.name}
                            {c.passed && (
                              <span className="badge bg-success ms-2">
                                Completed{c.score !== null ? ` · ${c.score}%` : ''}
                              </span>
                            )}
                          </label>
                        </div>
                      </li>
                    ))}
                  </ul>
                </>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
              <button className="btn btn-primary" onClick={save} disabled={saving || loading}>
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}