import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './EditarTema.css';

export default function EditarTema() {
  // Sacamos id (del curso) y temaId (del tema) de la URL
  const { id: cursoId, temaId } = useParams<{ id: string; temaId: string }>();
  const [titulo, setTitulo] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [editando, setEditando] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    const cargarDatos = async () => {
      try {
        setLoading(true);
        // Intentamos traer el tema. 
        // Si tu API no soporta GET /api/temas/:id, 
        // podrías traer todos los temas del curso y buscar el que coincida.
        const response = await apiFetch(`${apiBase}/api/temas/${temaId}`);
        
        if (!response.ok) throw new Error("No se pudo obtener el tema");
        
        const data = await response.json();
        // Rellenamos el campo con el título que viene de la BD
        setTitulo(data.titulo || ""); 
      } catch (err) {
        console.error(err);
        setError('Error al cargar la información del tema');
      } finally {
        setLoading(false);
      }
    };

    cargarDatos();
  }, [temaId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const userInfo = getCurrentUserInfo();
    const maestroId = userInfo?.id;

    if (!titulo.trim()) {
      setError('El título es obligatorio');
      return;
    }

    setEditando(true);
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    try {
      await apiFetch(`${apiBase}/api/temas/${temaId}?maestroId=${maestroId}`, {
        method: 'PUT',
        body: JSON.stringify({ nuevoTitulo: titulo.trim() }),
      });
      navigate(`/cursos/${cursoId}/temas`); // Volvemos a la lista
    } catch (err) {
      setError('Error al guardar los cambios');
    } finally {
      setEditando(false);
    }
  };

  return (
    <div className="crear-tema-page">
      <NavbarMisCursos />
      <main className="crear-tema-main">
        <button className="detalle-volver" onClick={() => navigate(-1)}>
          ←
        </button>

        <h2 className="welcome-text">Bienvenido al editor de temas</h2>

        <div className="crear-tema-card">
          {loading ? (
            <p className="loading-text">Cargando información del tema...</p>
          ) : (
            <form onSubmit={handleSubmit} className="crear-tema-layout-simple">
              <div className="input-group">
                <label htmlFor="titulo">Título del tema</label>
                <input
                  id="titulo"
                  type="text"
                  value={titulo}
                  onChange={(e) => setTitulo(e.target.value)}
                  className="pixel-input"
                  placeholder="Ej: Introducción Avanzada..."
                  disabled={editando}
                />
              </div>

              {error && <p className="error-msg">{error}</p>}

              <div className="crear-tema-actions-row">
                <button type="submit" className="pixel-btn-submit-main" disabled={editando}>
                  {editando ? 'Guardando...' : 'Guardar Cambios'}
                </button>
              </div>
            </form>
          )}
        </div>
      </main>
    </div>
  );
}