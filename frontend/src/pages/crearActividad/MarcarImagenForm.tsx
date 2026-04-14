import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';

export type MarcarImagenFormMode = 'create' | 'edit';

export interface MarcarImagenFormInitialPoint {
  readonly id?: number;
  readonly respuesta: string;
  readonly pixelX: number;
  readonly pixelY: number;
}

export interface MarcarImagenFormInitialValues {
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly respVisible: boolean;
  readonly permitirReintento?: boolean;
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
  readonly encontrarRespuestaAlumno?: boolean;
  readonly comentariosRespVisible: string | null;
  readonly temaId?: number;
  readonly imagenAMarcar: string;
  readonly puntosImagen: readonly MarcarImagenFormInitialPoint[];
}

interface Props {
  readonly mode?: MarcarImagenFormMode;
  readonly marcarImagenId?: number;
  readonly initialValues?: MarcarImagenFormInitialValues;
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly onDone?: () => void;
}

type Point = {
  id?: number;
  respuesta: string;
  pixelX: number;
  pixelY: number;
};

export function MarcarImagenForm({ mode = 'create', marcarImagenId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [permitirReintento, setPermitirReintento] = useState(false);
  const [mostrarPuntuacion, setMostrarPuntuacion] = useState(false);
  const [encontrarRespuestaMaestro, setEncontrarRespuestaMaestro] = useState(false);
  const [encontrarRespuestaAlumno, setEncontrarRespuestaAlumno] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [imagenAMarcar, setImagenAMarcar] = useState('');
  const [puntos, setPuntos] = useState<Point[]>([]);
  const [selectedPointIndex, setSelectedPointIndex] = useState<number | null>(null);
  const [imageNaturalSize, setImageNaturalSize] = useState<{ w: number; h: number } | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const imgRef = useRef<HTMLImageElement | null>(null);

  const navigate = useNavigate();
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const temaId = temaIdProp ?? params.temaId ?? (initialValues?.temaId != null ? String(initialValues.temaId) : undefined);

  useEffect(() => {
    if (!initialValues) return;
    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion ?? ''));
    setRespVisible(Boolean(initialValues.respVisible));
    setPermitirReintento(Boolean(initialValues.permitirReintento));
    setMostrarPuntuacion(Boolean(initialValues.mostrarPuntuacion));
    setEncontrarRespuestaMaestro(Boolean(initialValues.encontrarRespuestaMaestro));
    setEncontrarRespuestaAlumno(Boolean(initialValues.encontrarRespuestaAlumno));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');
    setImagenAMarcar(initialValues.imagenAMarcar ?? '');

    const nextPoints: Point[] = initialValues.puntosImagen?.length
      ? initialValues.puntosImagen.map((p) => ({
          id: p.id,
          respuesta: p.respuesta ?? '',
          pixelX: p.pixelX ?? 0,
          pixelY: p.pixelY ?? 0,
        }))
      : [];

    setPuntos(nextPoints);
    setSelectedPointIndex(nextPoints.length ? 0 : null);
  }, [initialValues]);

  const temaIdNum = useMemo(() => {
    if (!temaId) return null;
    const parsed = Number.parseInt(temaId, 10);
    return Number.isNaN(parsed) ? null : parsed;
  }, [temaId]);

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';

    if (!puntuacion.trim()) return 'La puntuación es requerida';
    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(puntuacionNum)) return 'La puntuación debe ser un número válido';
    if (puntuacionNum <= 0) return 'La puntuación debe ser un número mayor a 0';

    if (!temaIdNum) return 'Falta el id del tema en la URL';
    if (!cursoId) return 'Falta el id del curso en la URL';

    if (!imagenAMarcar.trim()) return 'La URL de la imagen a marcar es requerida';

    if (puntos.length === 0) return 'Añade al menos un punto haciendo clic en la imagen';
    if (puntos.some((p) => !p.respuesta.trim())) return 'Todos los puntos deben tener respuesta';

    if (mode === 'edit' && !marcarImagenId) return 'Falta el id de la actividad a editar';

    // Nota: de momento no forzamos puntos; sirve como base.
    return null;
  };

  const handleImageLoad = () => {
    const el = imgRef.current;
    if (!el || !el.naturalWidth || !el.naturalHeight) return;
    setImageNaturalSize({ w: el.naturalWidth, h: el.naturalHeight });
  };

  const handleImageClick = (e: React.MouseEvent) => {
    const el = imgRef.current;
    if (!el) return;

    const rect = el.getBoundingClientRect();
    if (!rect.width || !rect.height) return;

    const naturalW = el.naturalWidth || imageNaturalSize?.w;
    const naturalH = el.naturalHeight || imageNaturalSize?.h;
    if (!naturalW || !naturalH) return;

    const x = Math.round(((e.clientX - rect.left) / rect.width) * naturalW);
    const y = Math.round(((e.clientY - rect.top) / rect.height) * naturalH);

    setPuntos((prev) => {
      const next = [...prev, { respuesta: '', pixelX: x, pixelY: y }];
      setSelectedPointIndex(next.length - 1);
      return next;
    });
  };

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);

    setLoading(true);
    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
      const url =
        mode === 'edit'
          ? `${apiBase}/api/marcar-imagenes/${marcarImagenId}`
          : `${apiBase}/api/marcar-imagenes`;
      const method = mode === 'edit' ? 'PUT' : 'POST';

      await apiFetch(url, {
        method,
        body: JSON.stringify({
          titulo: titulo.trim(),
          descripcion: descripcion.trim() || '',
          puntuacion: puntuacionNum,
          respVisible,
          permitirReintento,
          mostrarPuntuacion,
          encontrarRespuestaMaestro,
          encontrarRespuestaAlumno,
          comentariosRespVisible: respVisible ? comentariosRespVisible.trim() || null : null,
          temaId: temaIdNum,
          imagenAMarcar: imagenAMarcar.trim(),
          puntosImagen: puntos.map((p) => ({
            ...(mode === 'edit' && p.id ? { id: p.id } : {}),
            respuesta: p.respuesta.trim(),
            pixelX: p.pixelX,
            pixelY: p.pixelY,
          })),
        }),
      });

      if (onDone) onDone(); else navigate(`/cursos/${cursoId}`);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Error guardando la actividad';
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
        <p className="ca-text" style={{ marginTop: 0, color: '#c0392b !important' }}>
          {error}
        </p>
      )}

      <div className="ca-contenedor-blanco" style={{ gap: 24, maxWidth: '100%' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: '1 1 320px', minWidth: 0 }}>
          <div>
            <label className="ca-text" htmlFor="mi-titulo">
              Título
            </label>
            <input
              type="text"
              id="mi-titulo"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              style={{ width: '100%' }}
              placeholder="Ej: Señala los elementos correctos"
              required
            />
          </div>

          <div>
            <label className="ca-text" htmlFor="mi-descripcion">
              Descripción
            </label>
            <textarea
              id="mi-descripcion"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              rows={3}
              style={{ width: '100%', resize: 'vertical' }}
              placeholder="Instrucciones para el alumno"
            />
          </div>

          <div>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: '1 1 320px', minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <label className="ca-text" htmlFor="mi-puntuacion" style={{ whiteSpace: 'nowrap' }}>
              Puntuación
            </label>
            <input
              type="number"
              id="mi-puntuacion"
              value={puntuacion}
              onChange={(e) => setPuntuacion(e.target.value)}
              style={{ width: 90 }}
              min="1"
              required
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="mi-resp-visible"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            <label className="ca-text" htmlFor="mi-resp-visible">
              Correcciones visibles
            </label>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="mi-permitir-reintento"
              checked={permitirReintento}
              onChange={(e) => setPermitirReintento(e.target.checked)}
            />
            <label className="ca-text" htmlFor="mi-permitir-reintento">
              Permitir reintentos
            </label>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="mi-mostrar-puntuacion"
              checked={mostrarPuntuacion}
              onChange={(e) => setMostrarPuntuacion(e.target.checked)}
            />
            <label className="ca-text" htmlFor="mi-mostrar-puntuacion">
              Mostrar puntuación
            </label>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="mi-mostrar-respuesta-correcta"
              checked={encontrarRespuestaMaestro}
              onChange={(e) => setEncontrarRespuestaMaestro(e.target.checked)}
            />
            <label className="ca-text" htmlFor="mi-mostrar-respuesta-correcta">
              Mostrar respuesta correcta
            </label>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="mi-mostrar-respuesta-alumno"
              checked={encontrarRespuestaAlumno}
              onChange={(e) => setEncontrarRespuestaAlumno(e.target.checked)}
            />
            <label className="ca-text" htmlFor="mi-mostrar-respuesta-alumno">
              Mostrar mi respuesta
            </label>
          </div>

          {respVisible && (
            <div>
              <label className="ca-text" htmlFor="mi-comentarios">
                Comentarios
              </label>
              <input
                type="text"
                id="mi-comentarios"
                value={comentariosRespVisible}
                onChange={(e) => setComentariosRespVisible(e.target.value)}
                style={{ width: '100%' }}
              />
            </div>
          )}

          <div>
            <label className="ca-text" htmlFor="mi-imagen-a-marcar">
              Imagen a marcar (URL)
            </label>
            <input
              type="url"
              id="mi-imagen-a-marcar"
              value={imagenAMarcar}
              onChange={(e) => setImagenAMarcar(e.target.value)}
              style={{ width: '100%' }}
              placeholder="https://..."
            />
          </div>
        </div>
      </div>

      <div
        className="ca-contenedor-blanco"
        style={{ gap: 16, marginTop: 16, marginBottom: 24, flexDirection: 'column', alignItems: 'stretch' }}
      >
        <p className="ca-ordenacion-help" style={{ marginTop: 0, marginBottom: 0 }}>
          Haz clic en la imagen para añadir puntos.
        </p>

        {imagenAMarcar.trim() && (
          <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
            <div style={{ flex: '1 1 300px', maxWidth: 500, display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div
                style={{
                  border: '2px solid #000',
                  borderRadius: 8,
                  overflow: 'hidden',
                  background: '#fff',
                  position: 'relative',
                }}
              >
                <button
                  type="button"
                  onClick={handleImageClick}
                  aria-label="Imagen para añadir puntos"
                  style={{
                    all: 'unset',
                    display: 'block',
                    width: '100%',
                    cursor: 'crosshair',
                  }}
                >
                  <img
                    ref={imgRef}
                    src={imagenAMarcar.trim()}
                    alt="Vista previa"
                    onLoad={handleImageLoad}
                    style={{
                      width: '100%',
                      maxHeight: 350,
                      objectFit: 'contain',
                      display: 'block',
                      userSelect: 'none',
                    }}
                  />
                </button>

                {imageNaturalSize &&
                  puntos.map((p, idx) => {
                    const left = `${(p.pixelX / imageNaturalSize.w) * 100}%`;
                    const top = `${(p.pixelY / imageNaturalSize.h) * 100}%`;
                    const isSelected = selectedPointIndex === idx;

                    return (
                      <button
                        key={`${p.pixelX}-${p.pixelY}-${idx}`}
                        type="button"
                        onClick={(ev) => {
                          ev.stopPropagation();
                          setSelectedPointIndex(idx);
                        }}
                        aria-label={`Punto ${idx + 1}`}
                        style={{
                          position: 'absolute',
                          left,
                          top,
                          transform: 'translate(-50%, -50%)',
                          width: 14,
                          height: 14,
                          borderRadius: 9999,
                          border: '2px solid #000',
                          background: isSelected ? '#FFF394' : '#D10057',
                          boxShadow: '2px 2px 0px #000',
                          padding: 0,
                          cursor: 'pointer',
                        }}
                      />
                    );
                  })}
              </div>

              <div style={{ display: 'flex', justifyContent: 'center' }}>
                <button className="ca-btn-guardar" type="submit" disabled={loading}>
                  {loading ? 'Guardando...' : 'Guardar'}
                </button>
              </div>
            </div>

            {puntos.length > 0 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: '1 1 200px', maxWidth: 300, maxHeight: 400, overflowY: 'auto' }}>
                {puntos.map((p, i) => {
                  const selected = selectedPointIndex === i;
                  return (
                    <div
                      key={`${p.pixelX}-${p.pixelY}-${i}`}
                      style={{
                        border: selected ? '3px solid black' : '1px solid black',
                        borderRadius: 8,
                        padding: 6,
                        background: selected ? '#fff' : 'transparent',
                        position: 'relative',
                        cursor: 'pointer',
                      }}
                      onClick={() => setSelectedPointIndex(i)}
                    >
                      <button
                        type="button"
                        aria-label={`Eliminar punto ${i + 1}`}
                        onClick={(ev) => {
                          ev.stopPropagation();
                          setPuntos((prev) => {
                            const next = prev.filter((_, idx) => idx !== i);
                            const nextSelected = next.length === 0 ? null : Math.min(i, next.length - 1);
                            setSelectedPointIndex(nextSelected);
                            return next;
                          });
                        }}
                        style={{
                          position: 'absolute',
                          top: 4,
                          right: 4,
                          width: 22,
                          height: 22,
                          display: 'grid',
                          placeItems: 'center',
                          border: '2px solid #000',
                          borderRadius: 6,
                          background: '#fff',
                          padding: 0,
                          cursor: 'pointer',
                          lineHeight: 1,
                          boxShadow: '2px 2px 0px #000',
                        }}
                      >
                        🗑
                      </button>

                      <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <label className="ca-text" style={{ marginBottom: 0, fontSize: '0.85rem' }}>
                          Punto {i + 1}
                        </label>

                        <input
                          type="text"
                          placeholder="Respuesta"
                          value={p.respuesta}
                          onChange={(e) => {
                            const next = [...puntos];
                            next[i] = { ...next[i], respuesta: e.target.value };
                            setPuntos(next);
                          }}
                        />

                        <p className="ca-text" style={{ margin: 0, fontSize: '0.75rem', color: '#666' }}>
                          X: {p.pixelX}px, Y: {p.pixelY}px
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {puntos.length === 0 && imagenAMarcar.trim() && (
          <p className="ca-text" style={{ marginTop: 0, marginBottom: 0 }}>
            Sin puntos todavía. Añádelos haciendo clic en la imagen.
          </p>
        )}

        {!imagenAMarcar.trim() && (
          <div style={{ display: 'flex', justifyContent: 'center', marginTop: 8 }}>
            <button className="ca-btn-guardar" type="submit" disabled={loading}>
              {loading ? 'Guardando...' : 'Guardar'}
            </button>
          </div>
        )}
      </div>
    </form>
  );
}
