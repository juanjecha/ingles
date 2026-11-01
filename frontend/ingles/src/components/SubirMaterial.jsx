import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axiosConfig'

export default function SubirMaterial({ agregar }) {
  const [word, setword] = useState('')
  const [category, setCategory] = useState('')
  //const [level, setLevel] = useState(1)
  const [file, setFile] = useState(null)
  const [preview, setPreview] = useState(null)
  const [categories, setCategories] = useState([])
  const [sending, setSending] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    api.get('categories').then((res) => setCategories(res.data))
    .catch(() => alert('Categories could not be showed up'))
  }, [])

  const chooseImg = (e) => {
    const file = e.target.files[0]
    if (!file) return
    setFile(file)
    setPreview(URL.createObjectURL(file))
  }

  const send = async (e) => {
    e.preventDefault()

    const data = new FormData()
    data.append('word', word)
    data.append('category', category)
    //data.append('level', level)
    data.append('image', file)

    setSending(true)
    try {
      await api.post('newMaterial', data)
      alert('Resource uploaded succesfully')
      navigate('/materiales')
    } catch (err) {
      const errors = err.response?.data?.messages
      alert(errors ? Object.values(errors).join('\n') : 'Resource could not be uploaded')
    } finally {
      setSending(false)
    }
  }

  return (
    <form onSubmit={send}>
      <h2>New content</h2>

      <label htmlFor="word">Word</label>
      <input
        id="word"
        value={word}
        onChange={(e) => setword(e.target.value)}
        required
      />

      <label htmlFor="category">Category</label>
      <select id="category"
      value={category}
      onChange={(e) => setCategory(e.target.value)}
      required>
        <option value="">Selecciona una categoría</option>
        {categories.map((c) => (
          <option key={c.id} value={c.id}>{c.name} ({c.difficulty})</option>
        ))}
      </select>

      <label htmlFor="image">Image</label>
      <input
        id="image"
        type="file"
        accept="image/png, image/jpeg, image/webp"
        onChange={chooseImg}
        required
      />

      {preview && <img src={preview} alt='Preview' style={{maxWidth: 200}} />}

      <button type="submit" disabled={sending}> {sending ? 'Uploading...' : 'Upload'} </button>
    </form>
  )
}