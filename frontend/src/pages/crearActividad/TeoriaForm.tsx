import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';

interface Props {
  mode?: 'create' | 'edit';
  actividadId?: number;
  initialTitulo?: string;
  initialDescripcion?: string;
}

export function TeoriaForm({ mode = 'create', actividadId, initialTitulo = '', initialDescripcion = '' }: Props) {
  const [titulo, setTitulo] = useState(initialTitulo);
  const [descripcion, setDescripcion] = useState(initialDescripcion);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { id: cursoId, temaId } = useParams<{ id: string; temaId: string }>();

  useEffect(() => {
    setTitulo(initialTitulo);
    setDescripcion(initialDescripcion);
  }, [initialTitulo, initialDescripcion]);

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      if (mode === 'edit') {
        if (!actividadId) throw new Error('ID de actividad no encontrado');
        
        await apiFetch(`${apiBase}/api/actividades/teoria/${actividadId}`, {
          method: 'PUT',
          body: JSON.stringify({
            titulo: titulo.trim(),
            descripcion: descripcion.trim(),
          }),
        });
      } else {
        const temaIdNum = temaId ? parseInt(temaId, 10) : null;
        if (!temaIdNum) throw new Error('ID de tema no encontrado');
        if (!cursoId) throw new Error('ID de curso no encontrado');

        await apiFetch(`${apiBase}/api/actividades/teoria`, {
          method: 'POST',
          body: JSON.stringify({
            titulo: titulo.trim(),
            descripcion: descripcion.trim(),
            imagen: '',
            temaId: temaIdNum,
          }),
        });
      }

      navigate(-1);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error guardando la teoría';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="ca-ordenacion-form">
      {error && (
        <p className="ca-text" style={{ color: '#ff4444', marginBottom: '15px' }}>
          {error}
        </p>
      )}

      <div
        className="ca-contenedor-blanco"
        style={{ flexDirection: 'column', alignItems: 'stretch', gap: '16px' }}
      >
        <div>
          <label className="ca-text" htmlFor="titulo">
            Título de la Lección
          </label>
          <input
            type="text"
            id="titulo"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            style={{ width: '100%' }}
            placeholder="Ej: Introducción a la materia"
          />
        </div>

        <div>
          <label className="ca-text" htmlFor="descripcion">
            Contenido Teórico
          </label>
          <textarea
            id="descripcion"
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            rows={10}
            style={{ width: '100%', resize: 'none' }}
            placeholder="Escribe aquí el contenido..."
          />
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20px' }}>
        <button className="ca-btn-guardar" type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar Teoría'}
        </button>
      </div>
    </form>
  );
}