import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';

type OrdenacionDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly imagen: string | null;
  readonly respVisible: boolean;
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly temaId: number | null;
  readonly valores: string[];
};

type RespAlumnoOrdenacionCreateResponse = {
  readonly respAlumnoOrdenacion: {
    readonly id: number;
    readonly correcta: boolean;
  };
  readonly comentario: string;
};

type ActividadAlumnoDTO = { readonly id: number };

function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo();
  if (!info) return null;
  const raw = (info as Record<string, unknown>)?.id ?? (info as Record<string, unknown>)?.userId ?? (info as Record<string, unknown>)?.sub;
  const userId = typeof raw === 'string' ? Number(raw) : raw;
  return typeof userId === 'number' && Number.isFinite(userId) ? userId : null;
}

function isImageString(value: string): boolean {
  const trimmed = value.trim();
  if (!trimmed) return false;
  if (/^data:image\//i.test(trimmed)) return true;

  try {
    const url = new URL(trimmed);
    const path = url.pathname.toLowerCase();
    return /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(path);
  } catch {
    return false;
  }
}

function moveItem<T>(items: readonly T[], fromIndex: number, toIndex: number): T[] {
  if (fromIndex === toIndex) return [...items];
  if (fromIndex < 0 || fromIndex >= items.length) return [...items];
  if (toIndex < 0 || toIndex >= items.length) return [...items];

  const next = [...items];
  const [moved] = next.splice(fromIndex, 1);
  next.splice(toIndex, 0, moved);
  return next;
}

export default function OrdenacionAlumno() {
  const { ordenacionId } = useParams<{ ordenacionId: string }>();

  const initInFlightRef = useRef(false);

  const [ordenacion, setOrdenacion] = useState<OrdenacionDTO | null>(null);
  const [items, setItems] = useState<string[]>([]);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>('');
  const [feedback, setFeedback] = useState<{ correcta: boolean; comentario?: string } | null>(null);

  const ordenacionIdNum = useMemo(() => {
    if (!ordenacionId) return NaN;
    return Number.parseInt(ordenacionId, 10);
  }, [ordenacionId]);

  useEffect(() => {
    const run = async () => {
      if (initInFlightRef.current) return;
      initInFlightRef.current = true;

      if (!ordenacionId || Number.isNaN(ordenacionIdNum)) {
        setError('Falta el id de la ordenación en la URL');
        setLoading(false);
        initInFlightRef.current = false;
        return;
      }

      setLoading(true);
      setError('');
      setFeedback(null);

      try {
        const ordRes = await apiFetch(`/api/ordenaciones/${ordenacionIdNum}`);
        const ordData = (await ordRes.json()) as OrdenacionDTO;
        setOrdenacion(ordData);
        setItems(Array.isArray(ordData.valores) ? [...ordData.valores] : []);

        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) {
          throw new Error('No se pudo identificar al alumno conectado. Inicia sesión de nuevo.');
        }

        // 1) ensure devuelve 0/1 (sin errores si no existe)
        const ensureRes = await apiFetch(`/api/actividades-alumno/ensure/${ordData.id}`);
        const ensureValue = (await ensureRes.json()) as unknown;
        const exists = ensureValue === 1 || ensureValue === '1' || ensureValue === true;

        if (!exists) {
          // 2) Si no existe, lo creamos
          const createAA = await apiFetch(`/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: ordData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new Error('Respuesta inválida al crear ActividadAlumno');
          }
        } else {
          // 3) Si existe, recuperamos el DTO para obtener el id
          const getAA = await apiFetch(`/api/actividades-alumno/alumno/${alumnoId}/actividad/${ordData.id}`);
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new Error('Respuesta inválida al obtener ActividadAlumno');
          }
        }
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'Error cargando la ordenación';
        setError(msg);
      } finally {
        setLoading(false);
        initInFlightRef.current = false;
      }
    };

    run();
  }, [ordenacionId, ordenacionIdNum]);

  const handleSubmit = async () => {
    setError('');
    setFeedback(null);

    if (!ordenacion || !ordenacion.id) {
      setError('No se ha cargado la actividad de ordenación');
      return;
    }

    if (!actividadAlumnoId) {
      setError('No se ha podido inicializar la actividad del alumno');
      return;
    }

    setSubmitting(true);
    try {
      const res = await apiFetch('/api/respuestas-alumno-ordenacion', {
        method: 'POST',
        body: JSON.stringify({
          actividadAlumno: { id: actividadAlumnoId },
          ordenacion: { id: ordenacion.id },
          valoresAlum: items,
        }),
      });

      const data = (await res.json()) as RespAlumnoOrdenacionCreateResponse;
      const correcta = Boolean(data?.respAlumnoOrdenacion?.correcta);
      const comentario = typeof data?.comentario === 'string' ? data.comentario : '';

      setFeedback({
        correcta,
        comentario: ordenacion.respVisible ? comentario : undefined,
      });
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error enviando la respuesta';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p className="ca-text">Cargando...</p>;
  }

  return (
    <div style={{ width: '100%', maxWidth: 900, margin: '0 auto' }}>
      {error && (
        <p className="ca-text" style={{ marginTop: 0 }}>
          {error}
        </p>
      )}

      {ordenacion && (
        <div className="ca-contenedor-blanco" style={{ gap: 16 }}>
          <h2 style={{ margin: 0 }}>{ordenacion.titulo}</h2>
          {ordenacion.descripcion && <p className="ca-text" style={{ margin: 0 }}>{ordenacion.descripcion}</p>}

          <p className="ca-text" style={{ margin: 0 }}>
            Reordena los elementos y envía tu respuesta.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {items.map((value, index) => (
              <div
                key={`${value}-${index}`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'flex-start',
                  gap: 12,
                  border: '1px solid black',
                  padding: 8,
                }}
              >
                <div style={{ width: 24, textAlign: 'right' }}>{index + 1}.</div>

                <div
                  style={{
                    flex: 1,
                    minWidth: 0,
                    textAlign: 'left',
                    display: 'flex',
                    justifyContent: 'flex-start',
                    alignItems: 'center',
                  }}
                >
                  {isImageString(value) ? (
                    <img
                      src={value}
                      alt={`Elemento ${index + 1}`}
                      style={{ width: 72, height: 72, objectFit: 'cover', display: 'block' }}
                    />
                  ) : (
                    <div className="ca-text" style={{ width: '100%' }}>
                      {value}
                    </div>
                  )}
                </div>

                <div style={{ display: 'flex', gap: 8 }}>
                  <button
                    className="ca-text"
                    type="button"
                    disabled={index === 0}
                    onClick={() => setItems((prev) => moveItem(prev, index, index - 1))}
                  >
                    ↑
                  </button>
                  <button
                    className="ca-text"
                    type="button"
                    disabled={index === items.length - 1}
                    onClick={() => setItems((prev) => moveItem(prev, index, index + 1))}
                  >
                    ↓
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div style={{ display: 'flex', justifyContent: 'center', gap: 12 }}>
            <button
              className="ca-btn-guardar"
              type="button"
              disabled={submitting || items.length === 0 || !actividadAlumnoId}
              onClick={handleSubmit}
            >
              {submitting ? 'Enviando...' : 'Enviar respuesta'}
            </button>
          </div>

          {feedback && (
            <div>
              <p className="ca-text" style={{ marginTop: 0 }}>
                {feedback.correcta ? 'Tu respuesta es correcta.' : 'Tu respuesta es incorrecta.'}
              </p>
              {ordenacion.respVisible && feedback.comentario && (
                <p className="ca-text" style={{ marginTop: 0 }}>
                  {feedback.comentario}
                </p>
              )}
            </div>
          )}
        </div>
      )}

      {!ordenacion && !error && <p className="ca-text">No se encontró la ordenación.</p>}
    </div>
  );
}
