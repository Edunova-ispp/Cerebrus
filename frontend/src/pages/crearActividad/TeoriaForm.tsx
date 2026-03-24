import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import GenerarIAModal from '../../components/GenerarIAModal/GenerarIAModal';
import './OrdenacionForm.css';

export type TeoriaFormMode = 'create' | 'edit';

export interface TeoriaFormInitialValues {
  readonly titulo: string;
  readonly descripcion: string;
  readonly imagen: string;
  readonly posicion: number;
}

interface Props {
  readonly mode?: 'create' | 'edit';
  readonly actividadId?: number;
  readonly initialValues?: TeoriaFormInitialValues;
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly onDone?: () => void;
}

export function TeoriaForm({ mode = 'create', actividadId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [imagen, setImagen] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [iaModalOpen, setIaModalOpen] = useState(false);


  const navigate = useNavigate();
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const temaId = temaIdProp ?? params.temaId;

  useEffect(() => {
    console.log("Intial values", initialValues);
    if (!initialValues) return;
    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setImagen(initialValues.imagen ?? '');
  }, [initialValues]);

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';

    if (!temaId) return 'Falta el id del tema en la URL';
    if (Number.isNaN(Number.parseInt(temaId, 10))) return 'El id del tema no es válido';
    if (!cursoId) return 'Falta el id del curso en la URL';
    if (!descripcion.trim()) return 'La descripción es requerida en una actividad de teoría';

    return null;
  };

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    const validationError = validate();

    if (validationError) {
      setError(validationError);
      return;
    }
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
            imagen: imagen.trim(),
          }),
        });
      } else {
        const temaIdNum = temaId ? Number.parseInt(temaId, 10) : null;
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

      if (onDone) onDone(); else navigate(-1);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error guardando la teoría';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleIAResult = (data: Record<string, unknown>) => {
    if (data.titulo) setTitulo(data.titulo as string);
    if (data.descripcion) setDescripcion(data.descripcion as string);
  };

  return (
    <form onSubmit={handleSubmit} className="of-form">
      {error && <p className="of-error">{error}</p>}

      <GenerarIAModal
        tipoActividad="TEORIA"
        open={iaModalOpen}
        onClose={() => setIaModalOpen(false)}
        onResult={handleIAResult}
      />

      <div className="of-meta-section" style={{ flexDirection: 'column' }}>
        <div>
          <label className="of-label" htmlFor="teoria-titulo">Título de la Lección</label>
          <input
            type="text"
            id="teoria-titulo"
            className="of-input"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Ej: Introducción a la materia"
          />
        </div>
        <div>
          <label className="of-label" htmlFor="teoria-descripcion">Contenido Teórico</label>
          <textarea
            id="teoria-descripcion"
            className="of-textarea"
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            rows={10}
            placeholder="Escribe aquí el contenido..."
          />
        </div>
        <div>
            <label className="cf-label" htmlFor="cf-imagen">URL de imagen (opcional)</label>
            <input
              type="url"
              id="cf-imagen"
              className="cf-input"
              value={imagen}
              onChange={(e) => setImagen(e.target.value)}
              placeholder="https://..."
            />
            {imagen.trim() && (
              <img
                src={imagen.trim()}
                alt="Preview"
                className="cf-img-preview"
                onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
                onLoad={(e) => { (e.target as HTMLImageElement).style.display = 'block'; }}
              />
            )}
          </div>
      </div>

      <div className="ca-form-footer">        <button type="button" className="iam-trigger-btn" onClick={() => setIaModalOpen(true)}>
          Generar con IA
        </button>        <button className="ca-btn-guardar" type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}