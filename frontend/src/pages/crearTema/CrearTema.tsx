import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './CrearTema.css';

interface CrearTemaProps {
  readonly cursoIdProp?: string;
  readonly embedded?: boolean;
  readonly onDone?: () => void;
}

export default function CrearTema({ cursoIdProp, embedded, onDone }: CrearTemaProps = {}) {
  const params = useParams<{ id: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const [titulo, setTitulo] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    if (!titulo.trim()) {
      setError('El título del tema es requerido');
      return;
    }

    const userInfo = getCurrentUserInfo();
    const maestroId = userInfo?.id;
    if (!maestroId) {
      setError('No se pudo obtener el usuario. Inicia sesión de nuevo.');
      return;
    }

    setLoading(true);
    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      await apiFetch(`${apiBase}/api/temas?maestroId=${maestroId}`, {
        method: 'POST',
        body: JSON.stringify({
          titulo: titulo.trim(),
          cursoId: Number(cursoId),
        }),
      });
      if (onDone) { onDone(); } else { navigate(`/cursos/${cursoId}/temas`); }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al crear el tema');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={embedded ? 'crear-tema-embedded' : 'crear-tema-page'}>
      {!embedded && <NavbarMisCursos />}

      <main className="crear-tema-main">
        {!embedded && (
          <button className="detalle-volver" onClick={() => navigate(-1)}>
            ← 
          </button>
        )}

        <h2 className="welcome-text">Crear tema</h2>

        <div className="crear-tema-card">
          <form onSubmit={handleSubmit} className="crear-tema-layout-simple">
            <div className="input-group">
              <label htmlFor="titulo">Título del tema</label>
              <input
                id="titulo"
                type="text"
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                className="pixel-input"
                placeholder="Ej: Introducción a la programación..."
                disabled={loading}
                autoFocus
              />
            </div>

            {error && <p className="error-msg">{error}</p>}

            <div className="crear-tema-actions-row">
              <button
                type="submit"
                className="pixel-btn-submit-main"
                disabled={loading}
              >
                {loading ? 'Creando...' : 'Crear Tema'}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}