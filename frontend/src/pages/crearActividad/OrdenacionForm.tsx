import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';

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

  const handleSubmit = async (e: React.FormEvent) => {
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
    <form
      onSubmit={handleSubmit}
      className="ca-ordenacion-form"
      style={{ width: '100%', maxWidth: '100%', boxSizing: 'border-box' }}
    >
      {error && (
        <p className="ca-text" style={{ marginTop: 0 }}>
          {error}
        </p>
      )}

      <div className="ca-contenedor-blanco" style={{ gap: 24, maxWidth: '100%' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: '1 1 320px', minWidth: 0 }}>
          <div>
            <label className="ca-text" htmlFor="titulo">
              Título
            </label>
            <input
              type="text"
              id="titulo"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              style={{ width: '100%' }}
            />
          </div>

          <div>
            <label className="ca-text" htmlFor="descripcion">
              Descripción
            </label>
            <textarea
              id="descripcion"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              rows={3}
              style={{ width: '100%' }}
            />
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: '1 1 320px', minWidth: 0 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <label className="ca-text" htmlFor="puntuacion" style={{ whiteSpace: 'nowrap' }}>
                Puntuación
              </label>
              <input
                type="number"
                id="puntuacion"
                value={puntuacion}
                onChange={(e) => setPuntuacion(e.target.value)}
                style={{ width: 90 }}
              />
            </div>
          </div>

          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <label className="ca-text" htmlFor="posicion" style={{ whiteSpace: 'nowrap' }}>
                Posición
              </label>
              <input
                type="number"
                id="posicion"
                value={posicion}
                onChange={(e) => setPosicion(e.target.value)}
                style={{ width: 90 }}
              />
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="respVisible"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            <label className="ca-text" htmlFor="respVisible">
              Correcciones visibles
            </label>
          </div>

          {respVisible && (
            <div>
              <label className="ca-text" htmlFor="comentariosRespVisible">
                Comentarios
              </label>
              <input
                type="text"
                id="comentariosRespVisible"
                value={comentariosRespVisible}
                onChange={(e) => setComentariosRespVisible(e.target.value)}
                style={{ width: '100%' }}
              />
            </div>
          )}

          {/*<button className="ca-text" type="button" disabled>
            Generar con IA
          </button>*/}
        </div>
      </div>

      <div
        className="ca-contenedor-blanco"
        style={{ gap: 16, marginTop: 16, flexDirection: 'column', alignItems: 'stretch' }}
      >
        <p className="ca-ordenacion-help" style={{ marginTop: 0, marginBottom: 0 }}>
          Actividad de ordenación. El alumno debe organizar los valores siguiendo un criterio determinado. Introduzca los valores en el orden correcto y Cerebrus reorganizará los valores aleatoriamente para sus alumnos.
        </p>

        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <button
            className="ca-text"
            type="button"
            disabled={ordenItemsKind === 'words'}
            onClick={() => setOrdenItemsKind('words')}
          >
            Palabras
          </button>

          <button
            className="ca-text"
            type="button"
            disabled={ordenItemsKind === 'images'}
            onClick={() => setOrdenItemsKind('images')}
          >
            Imágenes
          </button>
        </div>

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, width: '100%' }}>
          {ordenItems.map((v, i) => (
            <div key={i} style={{ border: '1px solid black', padding: 8 }}>
              {ordenItemsKind === 'images' && v.trim() && (
                <img
                  src={v.trim()}
                  alt={`Elemento ${i + 1}`}
                  style={{ width: 56, height: 56, objectFit: 'cover', display: 'block', marginBottom: 8 }}
                />
              )}
              <input
                type={ordenItemsKind === 'images' ? 'url' : 'text'}
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
            className="ca-text"
            type="button"
            onClick={() => {
              setOrdenItems([...ordenItems, '']);
            }}
          >
            +
          </button>
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 16 }}>
        <button className="ca-btn-guardar" type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
