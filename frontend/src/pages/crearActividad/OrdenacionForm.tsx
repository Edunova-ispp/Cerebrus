import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import GenerarIAModal from '../../components/GenerarIAModal/GenerarIAModal';
import './OrdenacionForm.css';

export type OrdenacionFormMode = 'create' | 'edit';

export interface OrdenacionFormInitialValues {
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly imagen: string | null;
  readonly respVisible: boolean;
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly valores: string[];
}

interface Props {
  readonly mode?: OrdenacionFormMode;
  readonly ordenacionId?: number;
  readonly initialValues?: OrdenacionFormInitialValues;
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly onDone?: () => void;
}

function makeLocalKey(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

export function OrdenacionForm({ mode = 'create', ordenacionId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [ordenItems, setOrdenItems] = useState<string[]>(['']);
  const [ordenItemKeys, setOrdenItemKeys] = useState<string[]>([makeLocalKey()]);
  const [ordenItemsKind, setOrdenItemsKind] = useState<'words' | 'images'>('words');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [iaModalOpen, setIaModalOpen] = useState(false);

  const posicionOriginal = useMemo(() => {
    if (mode !== 'edit') return null;
    const raw = initialValues?.posicion;
    return typeof raw === 'number' && Number.isFinite(raw) ? raw : null;
  }, [initialValues?.posicion, mode]);

  const navigate = useNavigate();
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const temaId = temaIdProp ?? params.temaId;

  useEffect(() => {
    if (!initialValues) return;

    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion ?? ''));
    setRespVisible(Boolean(initialValues.respVisible));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');
    const initialItems = initialValues.valores?.length ? [...initialValues.valores] : [''];
    setOrdenItems(initialItems);
    setOrdenItemKeys(initialItems.map(() => makeLocalKey()));
  }, [initialValues]);

  const valores = useMemo(() => {
    return ordenItems.map((v) => v.trim()).filter(Boolean);
  }, [ordenItems]);

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    if (!titulo.trim()) {
      setError('El título de la actividad de ordenación es requerido');
      return;
    }

    if (!puntuacion.trim()) {
      setError('La puntuación de la actividad de ordenación es requerida');
      return;
    }

    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(puntuacionNum)) {
      setError('La puntuación debe ser un número válido');
      return;
    }
    if (puntuacionNum <= 0) {
      setError('La puntuación debe ser un número mayor a 0');
      return;
    }

    if (!temaId) {
      setError('Falta el id del tema en la URL');
      return;
    }

    const temaIdNum = Number.parseInt(temaId, 10);
    if (Number.isNaN(temaIdNum)) {
      setError('El id del tema debe ser un número válido');
      return;
    }

    if (!cursoId) {
      setError('Falta el id del curso en la URL');
      return;
    }

    if (mode === 'edit' && posicionOriginal === null) {
      setError('No se pudo conservar la posición original para la edición');
      return;
    }

    if (valores.length === 0) {
      setError('Debes añadir al menos un valor');
      return;
    }

    setLoading(true);
    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      const url = mode === 'edit' ? `${apiBase}/api/ordenaciones/update/${ordenacionId}` : `${apiBase}/api/ordenaciones`;
      const method = mode === 'edit' ? 'PUT' : 'POST';

      if (mode === 'edit' && !ordenacionId) {
        setError('Falta el id de la ordenación a editar');
        return;
      }

      await apiFetch(url, {
        method,
        body: JSON.stringify({
          titulo: titulo.trim(),
          descripcion: descripcion.trim() || '',
          puntuacion: puntuacionNum,
          imagen: '',
          tema: { id: temaIdNum },
          respVisible,
          comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || null) : null,
          ...(mode === 'edit' ? { posicion: posicionOriginal } : {}),
          valores,
        }),
      });

      if (onDone) onDone(); else navigate(`/cursos/${cursoId}`);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error creando la ordenación';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleIAResult = (data: Record<string, unknown>) => {
    if (data.titulo) setTitulo(data.titulo as string);
    if (data.descripcion) setDescripcion(data.descripcion as string);

    const valores = data.valores as { texto: string; orden: number }[] | undefined;
    if (valores && Array.isArray(valores)) {
      const sorted = [...valores].sort((a, b) => a.orden - b.orden);
      setOrdenItems(sorted.map((v) => v.texto));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="of-form">
      {error && <p className="of-error">{error}</p>}

      <GenerarIAModal
        tipoActividad="ORDEN"
        open={iaModalOpen}
        onClose={() => setIaModalOpen(false)}
        onResult={handleIAResult}
      />

      {/* ── Metadata ── */
      <div className="of-meta-section">
        <div className="of-col">
          <div>
            <label className="of-label" htmlFor="of-titulo">Título *</label>
            <input
              type="text"
              id="of-titulo"
              className="of-input"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              placeholder="Título de la actividad"
              required
            />
          </div>
          <div>
            <label className="of-label" htmlFor="of-descripcion">Descripción</label>
            <textarea
              id="of-descripcion"
              className="of-textarea"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              rows={3}
              placeholder="Descripción opcional"
            />
          </div>
        </div>

        <div className="of-col">
          <div className="of-row">
            <label className="of-label" htmlFor="of-puntuacion">Puntuación *</label>
            <input
              type="number"
              id="of-puntuacion"
              className="of-input of-input-sm"
              value={puntuacion}
              onChange={(e) => setPuntuacion(e.target.value)}
              min="1"
              required
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            Correcciones visibles
          {respVisible && (
            <div>
              <label className="of-label" htmlFor="of-comentarios">Comentarios</label>
              <input
                type="text"
                id="of-comentarios"
                className="of-input"
                value={comentariosRespVisible}
                onChange={(e) => setComentariosRespVisible(e.target.value)}
              />
            </div>
          )}
        </div>
      </div>
      </div>

    }
      <div className="of-items-section">
        <p className="of-help">
          Actividad de ordenación. El alumno debe organizar los valores siguiendo un criterio
          determinado. Introduzca los valores en el orden correcto y Cerebrus reorganizará los
          valores aleatoriamente para sus alumnos.
        </p>

        <div className="of-kind-btns">
          <button
            className="of-kind-btn"
            type="button"
            disabled={ordenItemsKind === 'words'}
            onClick={() => setOrdenItemsKind('words')}
          >
            Palabras
          </button>
          <button
            className="of-kind-btn"
            type="button"
            disabled={ordenItemsKind === 'images'}
            onClick={() => setOrdenItemsKind('images')}
          >
            Imágenes
          </button>
        </div>

        <div className="of-items-grid">
          {ordenItems.map((v, i) => (
            <div key={ordenItemKeys[i] ?? `fallback-${i}`} className="of-item">
              {ordenItemsKind === 'images' && v.trim() && (
                <img
                  src={v.trim()}
                  alt={`Elemento ${i + 1}`}
                  className="of-item-img"
                />
              )}
              <input
                type={ordenItemsKind === 'images' ? 'url' : 'text'}
                className="of-input"
                placeholder={ordenItemsKind === 'images' ? `URL imagen ${i + 1}` : `Elemento ${i + 1}`}
                value={v}
                onChange={(e) => {
                  const copia = [...ordenItems];
                  copia[i] = e.target.value;
                  setOrdenItems(copia);
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Backspace' && v === '' && ordenItems.length > 1) {
                    setOrdenItems(ordenItems.filter((_, idx) => idx !== i));
                    setOrdenItemKeys(ordenItemKeys.filter((_, idx) => idx !== i));
                  }
                }}
              />
            </div>
          ))}
          <button
            className="of-btn-add"
            type="button"
            onClick={() => {
              setOrdenItems([...ordenItems, '']);
              setOrdenItemKeys([...ordenItemKeys, makeLocalKey()]);
            }}
          >
            +
          </button>
        </div>
      </div>

      <div className="ca-form-footer">
        <button type="button" className="iam-trigger-btn" onClick={() => setIaModalOpen(true)}>
          Generar con IA
        </button>
        <button className="ca-btn-guardar" type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
