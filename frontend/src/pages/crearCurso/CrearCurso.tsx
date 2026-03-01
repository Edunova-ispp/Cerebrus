import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './CrearCurso.css';

export default function CrearCurso() {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [imagen, setImagen] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [imagenError, setImagenError] = useState(false);
  const navigate = useNavigate();
  

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!titulo.trim()) {
      setError('El título del curso es requerido');
      return;
    }

    setLoading(true);

    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      const response = await fetch(`${apiBase}/api/cursos/curso`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({
          titulo: titulo.trim(),
          descripcion: descripcion.trim() || '',
          imagen: imagen.trim() || '',
        }),
      });

      if (response.ok) {
        alert('¡Curso creado exitosamente!');
        navigate('/miscursos');
      } else {
        const data = await response.json();
        setError(data.message || 'Error al crear el curso');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al crear el curso');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="crear-curso-page">
      <NavbarMisCursos />
      
      <main className="crear-curso-main">
        <div className="crear-curso-container">
          <div className="crear-curso-welcome-banner">
            Bienvenido al creador de cursos
              </div>
          <div className="crear-curso-box">
            <h1 className="crear-curso-title">Crear Nuevo Curso</h1>
            
            {error && <div className="crear-curso-error">{error}</div>}

            <form onSubmit={handleSubmit} className="crear-curso-form">
              <div className="pixel-input-wrapper">
                <label htmlFor="titulo">Título del curso:</label>
                <input
                  id="titulo"
                  type="text"
                  value={titulo}
                  onChange={(e) => setTitulo(e.target.value)}
                  placeholder="Ingresa el título del curso"
                  disabled={loading}
                />
              </div>

              <div className="pixel-input-wrapper">
                <label htmlFor="descripcion">Descripción:</label>
                <textarea
                  id="descripcion"
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                  placeholder="Descripción del curso (opcional)"
                  disabled={loading}
                  className="crear-curso-textarea"
                />
              </div>

              <div className="pixel-input-wrapper">
                <label htmlFor="imagen">URL Imagen:</label>
                <input
                  id="imagen"
                  type="text"
                  value={imagen}
                  onChange={(e) => { setImagen(e.target.value); setImagenError(false); }}                  placeholder="URL de la imagen (opcional)"
                  disabled={loading}
                />
              </div>

               {imagen && (
  <div className="crear-curso-preview">
    {imagenError ? (
      <div className="crear-curso-error" style={{ fontFamily: "'Pixelify Sans', sans-serif" }}>
          URL de imagen inválida o no accesible
      </div>    ) : (
      <img
        src={imagen}
        alt="Vista previa del curso"
        style={{ maxWidth: '100%', maxHeight: '200px', objectFit: 'contain', display: 'block', margin: '0 auto' }}
        onError={() => setImagenError(true)}
      />
    )}
  </div>
)}

              <button
                type="submit"
                className="pixel-btn-submit"
                disabled={loading}
              >
                {loading ? 'Creando...' : 'Crear Curso'}
              </button>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
}
