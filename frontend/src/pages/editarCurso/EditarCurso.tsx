import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import '../crearCurso/CrearCurso.css';

export default function EditarCurso() {
    const [titulo, setTitulo] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [imagen, setImagen] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);
    const [loadingUpdate, setLoadingUpdate] = useState(false);

    const { id } = useParams();
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

  // Cargar los datos del curso al iniciar
  useEffect(() => {
    const fetchCurso = async () => {
      setLoading(true);
      try {
        const response = await fetch(`${apiBase}/api/cursos/${id}/detalles`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
          const data = await response.json();
          // data es List<String>: [titulo, descripcion, imagen]
          setTitulo(data[0] || '');
          setDescripcion(data[1] || '');
          setImagen(data[2] || '');
          setError('');
        } else {
          if (response.status === 404) {
            setError('El curso no existe.');
          } else if (response.status === 403) {
            setError('No tienes permiso para ver este curso.');
          } else {
            setError('Error al cargar el curso.');
          }
        }
      } catch (err) {
        setError('Error de conexión: ' + (err instanceof Error ? err.message : 'Error desconocido'));
      } finally {
        setLoading(false);
      }
    };

    if (id && token && apiBase) {
      fetchCurso();
    }
  }, [id, apiBase, token]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    if (!titulo.trim()) {
      setError('El título del curso es requerido');
      return;
    }

    setLoadingUpdate(true);

    try {
      const response = await fetch(`${apiBase}/api/cursos/${id}`, {
        method: 'PATCH',
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
        alert('¡Curso actualizado exitosamente!');
        navigate('/miscursos');
      } else {
        if (response.status === 404) {
          setError('El curso no existe.');
        } else if (response.status === 403) {
          setError('No tienes permiso para editar este curso.');
        } else {
          setError('Error al actualizar el curso.');
        }
      }
    } catch (err) {
      setError('Error de conexión: ' + (err instanceof Error ? err.message : 'Error desconocido'));
    } finally {
      setLoadingUpdate(false);
    }
  };

  if (loading) {
    return (
      <div className="crear-curso-page">
        <NavbarMisCursos />
        <main className="crear-curso-main">
          <div className="crear-curso-container">
            <div className="crear-curso-box">
              <p style={{ fontFamily: "'Pixelify Sans', sans-serif", fontSize: '1.2rem', color: '#0F1338' }}>
                Cargando curso...
              </p>
            </div>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="crear-curso-page">
      <NavbarMisCursos />
      
      <main className="crear-curso-main">
        <div className="crear-curso-container">
          <div className="crear-curso-box">
            <h1 className="crear-curso-title">Editar Curso</h1>
            
            {error && <div className="crear-curso-error">{error}</div>}

            <form onSubmit={handleSubmit} className="crear-curso-form">
              <div className="pixel-input-wrapper">
                <label htmlFor="titulo">Título del curso:</label>
                <input
                  id="titulo"
                  type="text"
                  value={titulo}
                  onChange={(e) => setTitulo(e.target.value)}
                  placeholder="Título del curso"
                  disabled={loadingUpdate}
                />
              </div>

              <div className="pixel-input-wrapper">
                <label htmlFor="descripcion">Descripción:</label>
                <textarea
                  id="descripcion"
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                  placeholder="Descripción del curso (opcional)"
                  disabled={loadingUpdate}
                  className="crear-curso-textarea"
                />
              </div>

              <div className="pixel-input-wrapper">
                <label htmlFor="imagen">URL Imagen:</label>
                <input
                  id="imagen"
                  type="text"
                  value={imagen}
                  onChange={(e) => setImagen(e.target.value)}
                  placeholder="URL de la imagen (opcional)"
                />
              </div>

              {imagen && (
                <div className="crear-curso-preview">
                  <img src={imagen} alt="Vista previa del curso" />
                </div>
              )}

              <button
                type="submit"
                className="pixel-btn-submit"
              >
                Guardar Cambios
              </button>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
}