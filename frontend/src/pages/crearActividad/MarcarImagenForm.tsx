import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import './MarcarImagenForm.css';

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
  readonly readOnly?: boolean;
}

const MAX_CARACTERES_TITULO = 60;
const MAX_CARACTERES_DESCRIPCION = 1000;
const MAX_PUNTUACION = 10000;
const MAX_CARACTERES_COMENTARIOS = 250;
const MAX_PUNTOS = 55;
const MAX_CARACTERES_RESPUESTA_PUNTO = 60;

type Point = {
  id?: number;
  respuesta: string;
  pixelX: number;
  pixelY: number;
};

export function MarcarImagenForm({ mode = 'create', marcarImagenId, initialValues, temaIdProp, cursoIdProp, onDone, readOnly }: Props) {
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

  // Tracks which activity ID was last initialized to prevent re-initializing on every render
  const initializedActivityIdRef = useRef<number | null>(null);

  useEffect(() => {
    if (!initialValues) return;

    // Only reinitialize if the activity ID changed, not on every render
    if (initializedActivityIdRef.current === marcarImagenId) return;
    
    initializedActivityIdRef.current = marcarImagenId ?? null;

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
  }, [marcarImagenId, mode]);

  const temaIdNum = useMemo(() => {
    if (!temaId) return null;
    const parsed = Number.parseInt(temaId, 10);
    return Number.isNaN(parsed) ? null : parsed;
  }, [temaId]);

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';
    if (titulo.trim().length > MAX_CARACTERES_TITULO) return `El título no puede exceder ${MAX_CARACTERES_TITULO} caracteres.`;

    if (descripcion.trim().length > MAX_CARACTERES_DESCRIPCION) return `La descripción no puede exceder ${MAX_CARACTERES_DESCRIPCION} caracteres.`;

    if (respVisible && comentariosRespVisible.trim().length === 0) return 'Escribe un comentario de corrección para mostrar.';
    if (comentariosRespVisible.trim().length > MAX_CARACTERES_COMENTARIOS) return `Los comentarios no pueden exceder ${MAX_CARACTERES_COMENTARIOS} caracteres.`;

    if (!puntuacion.trim()) return 'La puntuación es requerida';
    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(puntuacionNum)) return 'La puntuación debe ser un número válido';
    if (puntuacionNum <= 0) return 'La puntuación debe ser un número mayor a 0';
    if (puntuacionNum > MAX_PUNTUACION) return `La puntuación no puede exceder ${MAX_PUNTUACION}`;

    if (!temaIdNum) return 'Falta el id del tema en la URL';
    if (!cursoId) return 'Falta el id del curso en la URL';

    if (!imagenAMarcar.trim()) return 'La URL de la imagen a marcar es requerida';

    if (puntos.length === 0) return 'Añade al menos un punto haciendo clic en la imagen';
    if (puntos.length > MAX_PUNTOS) return `La imagen no puede tener más de ${MAX_PUNTOS} puntos`;
    if (puntos.some((p) => !p.respuesta.trim())) return 'Todos los puntos deben tener respuesta';
    if (puntos.some((p) => p.respuesta.trim().length > MAX_CARACTERES_RESPUESTA_PUNTO)) return `La respuesta de un punto no puede tener más de ${MAX_CARACTERES_RESPUESTA_PUNTO} caracteres`;

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
    if (readOnly) return;

    const el = imgRef.current;
    if (!el) return;
    if (puntos.length >= MAX_PUNTOS) return;

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
      <div className="ca-contenedor-blanco" style={{ gap: 24, maxWidth: '100%' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, flex: '1 1 320px', minWidth: 0 }}>
          <div>
            <label className="of-label" htmlFor="mi-titulo">
              Título *
            </label>
            <input
              className="mi-input"
              readOnly={readOnly}
              type="text"
              id="mi-titulo"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              placeholder="Ej: Señala los elementos correctos"
              required
            />
          </div>

          <div>
            <label className="of-label" htmlFor="mi-descripcion">
              Descripción
            </label>
            <textarea
              className="mi-textarea"
              readOnly={readOnly}
              id="mi-descripcion"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              rows={3}
              placeholder="Instrucciones para el alumno"
            />
          </div>

          <div>
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12, flex: '1 1 320px', minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div>
              <label className="of-label" htmlFor="mi-puntuacion" style={{ whiteSpace: 'nowrap' }}>
                Puntuación *
              </label>
              <input
                className="mi-input"
                readOnly={readOnly}
                type="number"
                id="mi-puntuacion"
                value={puntuacion}
                onChange={(e) => setPuntuacion(e.target.value)}
                style={{ width: 90 }}
                min="1"
                required
              />
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              disabled={readOnly}
              type="checkbox"
              id="mi-resp-visible"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            <label className="ca-text" htmlFor="mi-resp-visible">
              Mostrar comentarios de corrección
            </label>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              disabled={readOnly}
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
              disabled={readOnly}
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
              disabled={readOnly}
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
              disabled={readOnly}
              type="checkbox"
              id="mi-mostrar-respuesta-alumno"
              checked={encontrarRespuestaAlumno}
              onChange={(e) => setEncontrarRespuestaAlumno(e.target.checked)}
            />
            <label className="ca-text" htmlFor="mi-mostrar-respuesta-alumno">
              Mostrar respuesta del alumno
            </label>
          </div>

          {respVisible && (
            <div>
              <label className="of-label" htmlFor="mi-comentarios">
                Comentarios
              </label>
              <input
                className="mi-input"
                readOnly={readOnly}
                type="text"
                id="mi-comentarios"
                value={comentariosRespVisible}
                onChange={(e) => setComentariosRespVisible(e.target.value)}
              />
            </div>
          )}

          <div>
            <label className="of-label" htmlFor="mi-imagen-a-marcar">
              Imagen a marcar (URL)
            </label>
            <input
              className="mi-input"
              readOnly={readOnly}
              type="url"
              id="mi-imagen-a-marcar"
              value={imagenAMarcar}
              onChange={(e) => setImagenAMarcar(e.target.value)}
              placeholder="https://..."
            />
          </div>
        </div>
      </div>

      <div
        className="ca-contenedor-blanco"
        style={{ gap: 16, marginTop: 16, marginBottom: 24, flexDirection: 'column', alignItems: 'stretch' }}
      >
        <h3 className="cf-section-title">
          Puntos
          <span>{puntos.length} / {MAX_PUNTOS} máx.</span>
        </h3>
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
                <img
                  ref={imgRef}
                  src={imagenAMarcar.trim()}
                  alt="Vista previa"
                  onLoad={handleImageLoad}
                  onClick={handleImageClick}
                  style={{
                    width: '100%',
                    maxHeight: 350,
                    objectFit: 'contain',
                    display: 'block',
                    cursor: 'crosshair',
                    userSelect: 'none',
                  }}
                />

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

              <div className="ca-form-footer">
                <div className="mi-footer-stack">
                  {!readOnly && (
                    <div className="mi-footer-actions">
                      <button className="ca-btn-guardar" type="submit" disabled={loading}>
                        {loading ? 'Guardando...' : 'Guardar'}
                      </button>
                    </div>
                  )}
                  {error && (
                    <p className="ca-text tf-error" style={{ color: '#c0392b' }}>
                      {error}
                    </p>
                  )}
                </div>
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
                      }}
                      onClick={() => setSelectedPointIndex(i)}
                    >
                      <button
                        disabled={readOnly}
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
                          className="mi-input"
                          readOnly={readOnly}
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
          <div className="ca-form-footer">
            <div className="mi-footer-stack">
              <div className="mi-footer-actions">
                <button className="ca-btn-guardar" type="submit" disabled={loading}>
                  {loading ? 'Guardando...' : 'Guardar'}
                </button>
              </div>
              {error && (
                <p className="ca-text tf-error" style={{ color: '#c0392b' }}>
                  {error}
                </p>
              )}
            </div>
          </div>
        )}
      </div>
    </form>
  );
}
