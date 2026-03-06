import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import '../crearCurso/CrearCurso.css';

function getInitials(titulo: string): string {
  return titulo
    .split(" ")
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase() ?? "")
    .join("");
}

export default function EditarCurso() {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [imagen, setImagen] = useState('');
  const [error, setError] = useState('');
  const [imagenError, setImagenError] = useState(false); // Estado para controlar si la URL de imagen falla
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

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
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
          <p className="welcome-text">Cargando datos del curso...</p>
        </main>
      </div>
    );
  }

  return (
    <div className="crear-curso-page">
      <NavbarMisCursos />
      
      <main className="crear-curso-main">
        <button className="detalle-volver" onClick={() => navigate(`/cursos/${id}`)}>
          ← Volver
        </button>

        <h2 className="welcome-text">Editar detalles del curso</h2>

        <div className="crear-curso-card">
          <form onSubmit={handleSubmit} className="crear-curso-layout">
            
            {/* Columna Izquierda: Formulario */}
            <div className="form-column">
              <div className="input-group">
                <label htmlFor="titulo">Título</label>
                <input
                  id="titulo"
                  type="text"
                  value={titulo}
                  onChange={(e) => setTitulo(e.target.value)}
                  className="pixel-input"
                  disabled={loadingUpdate}
                />
              </div>

              <div className="input-group">
                <label htmlFor="descripcion">Descripción</label>
                <textarea
                  id="descripcion"
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                  className="pixel-textarea"
                  disabled={loadingUpdate}
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
                  placeholder="https://..."
                  disabled={loadingUpdate}
                />
              </div>
            </div>

            {/* Columna Derecha: Preview y Botón */}
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

              <button type="submit" className="pixel-btn-submit-main" disabled={loadingUpdate}>
                {loadingUpdate ? 'Guardando...' : 'Guardar cambios'}
              </button>
              
              {error && <p className="error-msg" style={{marginTop: '10px'}}>{error}</p>}
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}