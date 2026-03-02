import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './EditarTema.css';

export default function EditarTema() {
  const { temaId } = useParams<{ temaId: string }>();
  const [titulo, setTitulo] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
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
      await apiFetch(`/api/temas/${temaId}?maestroId=${maestroId}`, {
        method: 'PUT',
        body: JSON.stringify({
          nuevoTitulo: titulo.trim()
        }),
      });
      navigate(-1);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al editar el tema');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="crear-tema-page">
      <NavbarMisCursos />

      <main className="crear-tema-main">
        <div className="crear-tema-container">
          <div className="crear-tema-banner">
            Bienvenido al editor de temas
          </div>

          <div className="crear-tema-box">
            <h1 className="crear-tema-title">Editar Tema</h1>

            {error && <div className="crear-tema-error">{error}</div>}

            <form onSubmit={handleSubmit} className="crear-tema-form">
              <div className="pixel-input-wrapper">
                <label style={{color: '#000'}} htmlFor="titulo">Título del tema:</label>
                <input
                  id="titulo"
                  type="text"
                  value={titulo}
                  onChange={(e) => setTitulo(e.target.value)}
                  placeholder="Ingresa el título del tema"
                  disabled={loading}
                  autoFocus
                />
              </div>

              <div className="crear-tema-actions">
                <button
                  type="button"
                  className="pixel-btn-cancelar"
                  onClick={() => navigate(-1)}
                  disabled={loading}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="pixel-btn-submit"
                  disabled={loading}
                >
                  {loading ? 'Editando...' : 'Editar Tema'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
}
