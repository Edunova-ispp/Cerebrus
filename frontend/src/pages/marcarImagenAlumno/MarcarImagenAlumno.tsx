import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './MarcarImagenAlumno.css';

type PuntoImagenDTO = {
  readonly id: number;
  readonly respuesta: string;
  readonly pixelX: number;
  readonly pixelY: number;
};

type MarcarImagenDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly imagenActividad: string | null;
  readonly respVisible: boolean;
  readonly comentariosRespVisible: string | null;
  readonly temaId: number | null;
  readonly imagenAMarcar: string;
  readonly puntosImagen: PuntoImagenDTO[];
};

type ActividadAlumnoDTO = {
  readonly id: number;
  readonly nota?: number;
};

type RespAlumnoPuntoImagenDTO = {
  readonly id: number;
};

function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo();
  if (!info) return null;
  const raw =
    (info as Record<string, unknown>)?.id ??
    (info as Record<string, unknown>)?.userId ??
    (info as Record<string, unknown>)?.sub;
  const userId = typeof raw === 'string' ? Number(raw) : raw;
  return typeof userId === 'number' && Number.isFinite(userId) ? userId : null;
}

type ImageDims = {
  naturalWidth: number;
  naturalHeight: number;
  displayWidth: number;
  displayHeight: number;
};

export default function MarcarImagenAlumno() {
  const { marcarImagenId } = useParams<{ marcarImagenId: string }>();
  const navigate = useNavigate();

  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const imageRef = useRef<HTMLImageElement | null>(null);

  const [actividad, setActividad] = useState<MarcarImagenDTO | null>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>('');
  const [feedback, setFeedback] = useState<{ correcta: boolean; comentario?: string } | null>(null);
  const [selectedPuntoId, setSelectedPuntoId] = useState<number | null>(null);
  const [respuestas, setRespuestas] = useState<Record<number, string>>({});
  const [imageDims, setImageDims] = useState<ImageDims | null>(null);

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const marcarImagenIdNum = useMemo(() => {
    if (!marcarImagenId) return NaN;
    return Number.parseInt(marcarImagenId, 10);
  }, [marcarImagenId]);

  useEffect(() => {
    actividadAlumnoIdRef.current = actividadAlumnoId;
  }, [actividadAlumnoId]);

  useEffect(() => {
    return () => {
      const id = actividadAlumnoIdRef.current;
      if (!id) return;
      if (completedRef.current) return;
      if (abandonReportedRef.current) return;
      abandonReportedRef.current = true;
      apiFetch(`${apiBase}/api/actividades-alumno/${id}/abandon`, { method: 'POST' }).catch(() => {});
    };
  }, []);

  const updateImageDims = () => {
    const img = imageRef.current;
    if (!img) return;
    if (!img.naturalWidth || !img.naturalHeight) return;
    setImageDims({
      naturalWidth: img.naturalWidth,
      naturalHeight: img.naturalHeight,
      displayWidth: img.clientWidth,
      displayHeight: img.clientHeight,
    });
  };

  useEffect(() => {
    const onResize = () => updateImageDims();
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  useEffect(() => {
    const run = async () => {
      if (initInFlightRef.current) return;
      initInFlightRef.current = true;

      if (!marcarImagenId || Number.isNaN(marcarImagenIdNum)) {
        setError('Falta el id de la actividad en la URL');
        setLoading(false);
        initInFlightRef.current = false;
        return;
      }

      setLoading(true);
      setError('');
      setFeedback(null);

      try {
        const actRes = await apiFetch(`${apiBase}/api/marcar-imagenes/${marcarImagenIdNum}`);
        const actData = (await actRes.json()) as MarcarImagenDTO;
        setActividad(actData);

        const puntos = Array.isArray(actData.puntosImagen) ? actData.puntosImagen : [];
        if (puntos.length > 0) {
          setSelectedPuntoId(puntos[0].id);
        }

        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new Error('No se pudo identificar al alumno conectado. Inicia sesión de nuevo.');

        const ensureRes = await apiFetch(`${apiBase}/api/actividades-alumno/ensure/${actData.id}`);
        const ensureValue = (await ensureRes.json()) as unknown;
        const exists = ensureValue === 1 || ensureValue === '1' || ensureValue === true;

        if (exists) {
          const getAA = await apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${actData.id}`);
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new Error('Respuesta inválida al obtener ActividadAlumno');
          }
        } else {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: actData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new Error('Respuesta inválida al crear ActividadAlumno');
          }
        }
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'Error cargando la actividad';
        setError(msg);
      } finally {
        setLoading(false);
        initInFlightRef.current = false;
      }
    };

    run();
  }, [marcarImagenId, marcarImagenIdNum]);

  const puntos = actividad?.puntosImagen ?? [];

  const allAnswered = useMemo(() => {
    if (puntos.length === 0) return false;
    return puntos.every((p) => (respuestas[p.id] ?? '').trim().length > 0);
  }, [puntos, respuestas]);

  const getPointStyle = (p: PuntoImagenDTO): React.CSSProperties => {
    if (!imageDims) return { left: 0, top: 0 };
    const x = (p.pixelX / imageDims.naturalWidth) * imageDims.displayWidth;
    const y = (p.pixelY / imageDims.naturalHeight) * imageDims.displayHeight;
    return { left: `${x}px`, top: `${y}px` };
  };

  const handleSubmit = async () => {
    setError('');
    setFeedback(null);

    if (!actividad) {
      setError('No se ha cargado la actividad');
      return;
    }
    if (!actividadAlumnoId) {
      setError('No se ha podido inicializar la actividad del alumno');
      return;
    }
    if (puntos.length === 0) {
      setError('Esta actividad no tiene puntos configurados');
      return;
    }
    if (!allAnswered) {
      setError('Responde todos los puntos antes de enviar');
      return;
    }

    setSubmitting(true);
    try {
      const respIds = await Promise.all(
        puntos.map(async (p) => {
          const res = await apiFetch(`${apiBase}/api/respuestas-alumno-punto-imagen`, {
            method: 'POST',
            body: JSON.stringify({
              respuesta: (respuestas[p.id] ?? '').trim(),
              pixelX: p.pixelX,
              pixelY: p.pixelY,
              marcarImagenId: actividad.id,
              actividadAlumnoId,
            }),
          });
          const data = (await res.json()) as RespAlumnoPuntoImagenDTO;
          if (!data?.id) throw new Error('Respuesta inválida al guardar una respuesta');
          return data.id;
        })
      );

      const corregirRes = await apiFetch(`${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`, {
        method: 'PUT',
        body: JSON.stringify(respIds),
      });
      const aa = (await corregirRes.json()) as ActividadAlumnoDTO;
      const nota = typeof aa?.nota === 'number' ? aa.nota : 0;
      const correcta = nota >= 10;

      if (correcta) {
        completedRef.current = true;
        window.alert('Tu respuesta es correcta.');
        navigate(-1);
        return;
      }

      setFeedback({
        correcta: false,
        comentario: actividad.respVisible ? actividad.comentariosRespVisible ?? undefined : undefined,
      });
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error enviando la respuesta';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="marcar-imagen-alumno-page">
        <NavbarMisCursos />
        <main className="marcar-imagen-alumno-main">
          <p className="ca-text">Cargando...</p>
        </main>
      </div>
    );
  }

  return (
    <div className="marcar-imagen-alumno-page">
      <NavbarMisCursos />

      <main className="marcar-imagen-alumno-main">
        <div className="mia-top">
          <button className="mia-exit" type="button" onClick={() => navigate(-1)}>
            Salir
          </button>
          {actividad && (
            <div className="mia-title">
              <h1 className="mia-h1">{actividad.titulo}</h1>
              {actividad.descripcion && <p className="mia-desc">{actividad.descripcion}</p>}
            </div>
          )}
        </div>

        {error && (
          <p className="ca-text" style={{ marginTop: 0 }}>
            {error}
          </p>
        )}

        {!actividad && !error && <p className="ca-text">No se encontró la actividad.</p>}

        {actividad && (
          <>
            <div className="mia-image-section">
              <div className="mia-image-frame">
                <img
                  ref={imageRef}
                  src={actividad.imagenAMarcar}
                  alt="Imagen"
                  className="mia-image"
                  onLoad={updateImageDims}
                />

                <div className="mia-overlay" aria-hidden>
                  {puntos.map((p, idx) => {
                    const hasAnswer = (respuestas[p.id] ?? '').trim().length > 0;
                    const isSelected = selectedPuntoId === p.id;
                    const statusClass = isSelected
                      ? 'mia-point--selected'
                      : hasAnswer
                        ? 'mia-point--answered'
                        : 'mia-point--unanswered';

                    return (
                      <button
                        key={p.id}
                        type="button"
                        className={`mia-point ${statusClass}`}
                        style={getPointStyle(p)}
                        onClick={() => setSelectedPuntoId(p.id)}
                        title={`Punto ${idx + 1}`}
                      >
                        {idx + 1}
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>

            <div className="mia-cards-section">
              <h2 className="mia-h2">Respuestas</h2>
              <div className="mia-cards">
                {puntos.map((p, idx) => {
                  const hasAnswer = (respuestas[p.id] ?? '').trim().length > 0;
                  const isSelected = selectedPuntoId === p.id;
                  const cardClass = isSelected
                    ? 'mia-card--selected'
                    : hasAnswer
                      ? 'mia-card--answered'
                      : 'mia-card--unanswered';

                  return (
                    <div
                      key={p.id}
                      className={`mia-card ${cardClass}`}
                      role="button"
                      tabIndex={0}
                      onClick={() => setSelectedPuntoId(p.id)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') setSelectedPuntoId(p.id);
                      }}
                    >
                      <div className="mia-card-title">Punto {idx + 1}</div>
                      <input
                        className="mia-input"
                        type="text"
                        value={respuestas[p.id] ?? ''}
                        placeholder="Escribe tu respuesta"
                        onFocus={() => setSelectedPuntoId(p.id)}
                        onChange={(e) =>
                          setRespuestas((prev) => ({
                            ...prev,
                            [p.id]: e.target.value,
                          }))
                        }
                      />
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="mia-bottom">
              <div className="mia-bottom-inner">
                {feedback && (
                  <div className="mia-feedback">
                    <div>{feedback.correcta ? 'Tu respuesta es correcta.' : 'Tu respuesta es incorrecta.'}</div>
                    {actividad.respVisible && feedback.comentario && <div>{feedback.comentario}</div>}
                  </div>
                )}

                <button
                  className="ca-btn-guardar"
                  type="button"
                  disabled={submitting || !actividadAlumnoId || !allAnswered}
                  onClick={handleSubmit}
                >
                  {submitting ? 'Enviando...' : 'Enviar respuesta'}
                </button>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
