import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
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
}

export function OrdenacionForm({ mode = 'create', ordenacionId, initialValues }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [posicion, setPosicion] = useState('');
  const [ordenItems, setOrdenItems] = useState<string[]>(['']);
  const [ordenItemsKind, setOrdenItemsKind] = useState<'words' | 'images'>('words');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { id: cursoId, temaId } = useParams<{ id: string; temaId: string }>();

  useEffect(() => {
    if (!initialValues) return;

    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion ?? ''));
    setRespVisible(Boolean(initialValues.respVisible));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');
    setPosicion(String(initialValues.posicion ?? ''));
    setOrdenItems(initialValues.valores?.length ? [...initialValues.valores] : ['']);
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

    if (!posicion.trim()) {
      setError('La posición es requerida');
      return;
    }

    const posicionNum = Number.parseInt(posicion.trim(), 10);
    if (Number.isNaN(posicionNum)) {
      setError('La posición debe ser un número válido');
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
          posicion: posicionNum,
          valores,
        }),
      });

      navigate(`/cursos/${cursoId}/temas`);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error creando la ordenación';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="of-form">
      {error && <p className="of-error">{error}</p>}

      {/* ── Metadata ── */}
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
            />
          </div>
          <div className="of-row">
            <label className="of-label" htmlFor="of-posicion">Posición</label>
            <input
              type="number"
              id="of-posicion"
              className="of-input of-input-sm"
              value={posicion}
              onChange={(e) => setPosicion(e.target.value)}
            />
          </div>
          <label className="of-check-label">
            <input
              type="checkbox"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            Correcciones visibles
          </label>
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

      {/* ── Items section ── */}
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
            <div key={i} className="of-item">
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
                  }
                }}
              />
            </div>
          ))}
          <button
            className="of-btn-add"
            type="button"
            onClick={() => setOrdenItems([...ordenItems, ''])}
          >
            +
          </button>
        </div>
      </div>

      <div className="ca-form-footer">
        <button className="ca-btn-guardar" type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
