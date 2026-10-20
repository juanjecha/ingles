import React from 'react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function SubirMaterial({ agregar }) {
  const [word, setword] = useState('')
  const [category, setcategory] = useState('')
  const [image, setimage] = useState(null)
  const navigate = useNavigate()

  const chooseImg = (e) => {
    const file = e.target.files[0]
    if (!file) return

    const reader = new FileReader()
    reader.onloadend = () => setimage(reader.result)
    reader.readAsDataURL(file)
  }

  const enviar = (e) => {
    e.preventDefault()
    agregar({ word, category, image })
    alert('Content uploaded succesfully!')
    navigate('/materiales')
  }

  return (
    <form onSubmit={enviar}>
      <h2>New content</h2>

      <label htmlFor="word">Word</label>
      <input
        id="word"
        value={word}
        onChange={(e) => setword(e.target.value)}
        required
      />

      <label htmlFor="category">Category</label>
      <input
        id="category"
        value={category}
        onChange={(e) => setcategory(e.target.value)}
        required
      />

      <label htmlFor="image">Image</label>
      <input
        id="image"
        type="file"
        accept="image/*"
        onChange={chooseImg}
        required
      />

      {image && (
        <img src={image} alt="Vista previa" style={{ maxWidth: 200 }} />
      )}

      <button type="submit">Upload</button>
    </form>
  )
}