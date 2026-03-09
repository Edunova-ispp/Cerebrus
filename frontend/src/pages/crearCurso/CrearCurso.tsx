import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './CrearCurso.css';

function getInitials(titulo: string): string {
  return titulo
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase() ?? "")
    .join("");
}

export default function CrearCurso() {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [imagen, setImagen] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [imagenError, setImagenError] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
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
        <button className="detalle-volver" onClick={() => navigate('/miscursos')}>
          ←
        </button>
        <div className="crear-curso-header">
           <h2 className="welcome-text">Bienvenido al creador de cursos</h2>
        </div>

        <div className="crear-curso-card">
          <form onSubmit={handleSubmit} className="crear-curso-layout">
  {/* Columna Izquierda */}
  <div className="form-column">
    <div className="input-group">
      <label htmlFor="titulo">Título</label>
      <input
        id="titulo"
        type="text"
        value={titulo}
        onChange={(e) => setTitulo(e.target.value)}
        className="pixel-input"
        placeholder="Nombre del curso..."
      />
    </div>

    <div className="input-group">
      <label htmlFor="descripcion">Descripción</label>
      <textarea
        id="descripcion"
        value={descripcion}
        onChange={(e) => setDescripcion(e.target.value)}
        className="pixel-textarea"
        placeholder="¿De qué trata el curso?"
      />
    </div>

    <div className="input-group">
      <label htmlFor="imagen">URL Imagen</label>
      <input
        id="imagen"
        type="text"
        value={imagen}
        onChange={(e) => { setImagen(e.target.value); setImagenError(false); }}
        className="pixel-input"
        placeholder="https://... <opcional>"
      />
    </div>
  </div>

  {/* Columna Derecha */}
  <div className="actions-column">
    <div className="image-preview-group">
      <label>Vista previa</label>
      <div className="pixel-drop-zone">
        {imagen && !imagenError ? (
          <img 
            src={imagen} 
            alt="Preview" 
            className="pixel-preview-img"
            onError={() => setImagenError(true)} 
          />
        ) : (
          <div className="pixel-fallback-box">
            <span className="pixel-fallback-initials">
              {titulo ? getInitials(titulo) : "?"}
            </span>
          </div>
        )}
      </div>
    </div>

    <button type="submit" className="pixel-btn-submit-main" disabled={loading}>
      {loading ? 'Creando...' : 'Crear curso'}
    </button>
  </div>
</form>
          {error && <p className="error-msg">{error}</p>}
        </div>
      </main>
    </div>
  );
}