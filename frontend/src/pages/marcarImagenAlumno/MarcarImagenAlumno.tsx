import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import ActivityResultScreen, { type ActivityResultConfig } from '../../components/ActivityResultScreen/ActivityResultScreen';
import AnswerViewModal from '../../components/AnswerViewModal/AnswerViewModal';
import espadaImg from '../../assets/props/espada.png';
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
  readonly permitirReintento?: boolean;
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
  readonly encontrarRespuestaAlumno?: boolean;
};

type ActividadAlumnoDTO = {
  readonly id: number;
  readonly nota?: number;
  readonly puntuacion?: number | null;
  readonly fechaFin?: string | null;
};

function isCompletedAttempt(fechaFin?: string | null): boolean {
  if (!fechaFin) return false;
  const parsed = new Date(fechaFin);
  if (Number.isNaN(parsed.getTime())) return false;
  return parsed.getFullYear() !== 1970;
}

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
  const [activityConfig, setActivityConfig] = useState<ActivityResultConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>('');
  const [submitted, setSubmitted] = useState(false);
  const [lastAttemptScore, setLastAttemptScore] = useState<number | null>(null);
  const [lastAttemptGrade, setLastAttemptGrade] = useState<number | null>(null);
  const [selectedPuntoId, setSelectedPuntoId] = useState<number | null>(null);
  const [respuestas, setRespuestas] = useState<Record<number, string>>({});
  const [imageDims, setImageDims] = useState<ImageDims | null>(null);
  const [showAnswerModal, setShowAnswerModal] = useState(false);
  const [answerModalMode, setAnswerModalMode] = useState<'student' | 'correct'>('student');

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const marcarImagenIdNum = useMemo(() => {
    if (!marcarImagenId) return Number.NaN;
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
    const onMouseDown = (ev: MouseEvent) => {
      const target = ev.target as HTMLElement | null;
      if (!target) return;
      if (target.closest('.mia-point')) return;
      if (target.closest('.mia-float-card')) return;
      setSelectedPuntoId(null);
    };

    document.addEventListener('mousedown', onMouseDown);
    return () => document.removeEventListener('mousedown', onMouseDown);
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

      try {
        const actRes = await apiFetch(`${apiBase}/api/marcar-imagenes/${marcarImagenIdNum}`);
        const actData = (await actRes.json()) as MarcarImagenDTO;
        setActividad(actData);

        // Load activity configuration
        setActivityConfig({
          showScore: actData.mostrarPuntuacion ?? true,
          allowRetry: actData.permitirReintento ?? false,
          showCorrectAnswer: actData.encontrarRespuestaMaestro ?? true,
          showStudentAnswer: actData.encontrarRespuestaAlumno ?? true,
        });

        const puntos = Array.isArray(actData.puntosImagen) ? actData.puntosImagen : [];
        if (puntos.length > 0) {
          setSelectedPuntoId(puntos[0].id);
        }

        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new TypeError('No se pudo identificar al alumno conectado. Inicia sesión de nuevo.');

        let hasExisting = false;
        try {
          const getAA = await apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${actData.id}`);
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            hasExisting = true;
            setActividadAlumnoId(aaData.id);
            if (isCompletedAttempt(aaData.fechaFin)) {
              completedRef.current = true;
              setSubmitted(true);
              setLastAttemptScore(aaData.puntuacion ?? 0);
              setLastAttemptGrade(aaData.nota ?? null);
            }
          } else {
            throw new TypeError('Respuesta inválida al obtener ActividadAlumno');
          }
        } catch {
          hasExisting = false;
        }

        if (!hasExisting) {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: actData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new TypeError('Respuesta inválida al crear ActividadAlumno');
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

  const selectedPunto = useMemo(() => {
    if (!selectedPuntoId) return null;
    return puntos.find((p) => p.id === selectedPuntoId) ?? null;
  }, [puntos, selectedPuntoId]);

  const allAnswered = useMemo(() => {
    if (puntos.length === 0) return false;
    return puntos.every((p) => (respuestas[p.id] ?? '').trim().length > 0);
  }, [puntos, respuestas]);

  const aciertosEstimados = useMemo(() => {
    if (!actividad) return { aciertos: 0, total: 0 };
    const total = puntos.length;
    if (total === 0) return { aciertos: 0, total: 0 };

    const norm = (v: string) => v.trim().toLowerCase();
    let aciertos = 0;
    for (const p of puntos) {
      const alumno = norm(respuestas[p.id] ?? '');
      const correcta = norm(p.respuesta ?? '');
      if (alumno && alumno === correcta) aciertos += 1;
    }
    return { aciertos, total };
  }, [actividad, puntos, respuestas]);

  const getPointStyle = (p: PuntoImagenDTO): React.CSSProperties => {
    if (!imageDims) return { left: 0, top: 0 };
    const x = (p.pixelX / imageDims.naturalWidth) * imageDims.displayWidth;
    const y = (p.pixelY / imageDims.naturalHeight) * imageDims.displayHeight;
    return { left: `${x}px`, top: `${y}px` };
  };

  const floatCardStyle = useMemo((): React.CSSProperties | null => {
    if (!selectedPunto || !imageDims) return null;

    const xRaw = (selectedPunto.pixelX / imageDims.naturalWidth) * imageDims.displayWidth;
    const yRaw = (selectedPunto.pixelY / imageDims.naturalHeight) * imageDims.displayHeight;

    const approxHalfCardWidth = 140;
    const x = Math.min(Math.max(xRaw, approxHalfCardWidth), Math.max(approxHalfCardWidth, imageDims.displayWidth - approxHalfCardWidth));
    const y = Math.min(Math.max(yRaw + 18, 16), Math.max(16, imageDims.displayHeight - 16));

    return {
      left: `${x}px`,
      top: `${y}px`,
    };
  }, [imageDims, selectedPunto]);

  const handleSubmit = async () => {
    setError('');

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
            puntoImagenId: p.id,        
            actividadAlumnoId: actividadAlumnoId 
          }),
        });
        const data = (await res.json()) as RespAlumnoPuntoImagenDTO;
        if (!data?.id) throw new TypeError('Respuesta inválida al guardar una respuesta');
        return data.id;
      })
    );

      const corregirRes = await apiFetch(`${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`, {
        method: 'PUT',
        body: JSON.stringify(respIds),
      });
      const aa = (await corregirRes.json()) as ActividadAlumnoDTO;
      const nota = typeof aa?.nota === 'number' ? aa.nota : 0;
      setLastAttemptScore(typeof aa?.puntuacion === 'number' ? aa.puntuacion : null);
      setLastAttemptGrade(typeof aa?.nota === 'number' ? aa.nota : null);
      const correcta = nota >= 10;

      if (correcta) completedRef.current = true;

      setSubmitted(true);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error enviando la respuesta';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleRetry = async () => {
    if (!actividad) return;

    try {
      const alumnoId = getCurrentUserIdFromJwt();
      if (!alumnoId) {
        throw new TypeError('No se pudo identificar al alumno.');
      }

      const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
        method: 'POST',
        body: JSON.stringify({ alumnoId, actividadId: actividad.id }),
      });
      const aaData = (await createAA.json()) as ActividadAlumnoDTO;
      if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
        setActividadAlumnoId(aaData.id);
        actividadAlumnoIdRef.current = aaData.id;
      }

      completedRef.current = false;
      abandonReportedRef.current = false;
      setRespuestas({});
      setSubmitted(false);
      setLastAttemptScore(null);
      setLastAttemptGrade(null);
      setError('');
      setShowAnswerModal(false);
      setAnswerModalMode('student');

      const puntosActuales = Array.isArray(actividad.puntosImagen) ? actividad.puntosImagen : [];
      if (puntosActuales.length > 0) {
        setSelectedPuntoId(puntosActuales[0].id);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo crear un nuevo intento');
    }
  };

  const handleViewStudentAnswers = () => {
    setAnswerModalMode('student');
    setShowAnswerModal(true);
  };

  const handleViewCorrectAnswers = () => {
    setAnswerModalMode('correct');
    setShowAnswerModal(true);
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
        {actividad && (
          <>
            <div className="mia-top">
              <button className="mia-exit-btn" type="button" onClick={() => navigate(-1)}>
                <img src={espadaImg} alt="" className="mia-exit-icon" />
                <span>Salir</span>
              </button>

              <div className="mia-title-banner">
                <h1 className="mia-title-text">{actividad.titulo}</h1>
              </div>
            </div>

            {actividad.descripcion && <p className="mia-description">{actividad.descripcion}</p>}
          </>
        )}

        {error && (
          <p className="ca-text" style={{ marginTop: 0 }}>
            {error}
          </p>
        )}

        {!actividad && !error && <p className="ca-text">No se encontró la actividad.</p>}

        {actividad && (
          <>
            <div className="mia-image-section">
              <p className="mia-instruction">¡Selecciona los puntos dentro de la foto para poder escribir las respuestas!</p>
              <div className="mia-image-frame" onMouseDown={() => setSelectedPuntoId(null)}>
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
                        onMouseDown={(ev) => {
                          ev.stopPropagation();
                        }}
                        onClick={(ev) => {
                          ev.stopPropagation();
                          setSelectedPuntoId(p.id);
                        }}
                        aria-label={`Punto ${idx + 1}`}
                      >
                      </button>
                    );
                  })}
                </div>

                {selectedPunto && floatCardStyle && (
                  <div
                    className="mia-float-card"
                    style={floatCardStyle}
                    onMouseDown={(ev) => ev.stopPropagation()}
                  >
                    <div className="mia-float-title">Respuesta</div>
                    <input
                      className="mia-float-input"
                      type="text"
                      value={respuestas[selectedPunto.id] ?? ''}
                      placeholder="Escribe tu respuesta"
                      onChange={(e) =>
                        setRespuestas((prev) => ({
                          ...prev,
                          [selectedPunto.id]: e.target.value,
                        }))
                      }
                      autoFocus
                    />
                  </div>
                )}
              </div>
            </div>

            <div className="mia-bottom">
              <div className="mia-bottom-inner">
                {!submitted && (
                  <button
                    className="ca-btn-guardar"
                    type="button"
                    disabled={submitting || !actividadAlumnoId || !allAnswered}
                    onClick={handleSubmit}
                  >
                    {submitting ? 'Enviando...' : 'Enviar respuesta'}
                  </button>
                )}
              </div>
            </div>

            {submitted && activityConfig ? (
              <ActivityResultScreen
                title="¡MARCAR IMAGEN COMPLETADA!"
                score={lastAttemptScore ?? Math.round((aciertosEstimados.aciertos / Math.max(aciertosEstimados.total, 1)) * actividad.puntuacion)}
                maxScore={actividad.puntuacion}
                grade={lastAttemptGrade ?? undefined}
                config={activityConfig}
                onContinue={() => navigate(-1)}
                onRetry={handleRetry}
                onViewStudentAnswer={handleViewStudentAnswers}
                onViewCorrectAnswer={handleViewCorrectAnswers}
                onCancel={() => navigate(-1)}
              />
            ) : submitted ? (
              <div className="mia-alert-overlay" role="dialog" aria-modal="true">
                <div className="mia-alert" onMouseDown={(ev) => ev.stopPropagation()}>
                  <div className="mia-alert-title">¡Actividad completada!</div>
                  <button className="ca-btn-guardar" type="button" onClick={() => navigate(-1)}>
                    Volver
                  </button>
                </div>
              </div>
            ) : null}
          </>
        )}
      </main>

      {showAnswerModal && (
        <AnswerViewModal
          title={answerModalMode === 'student' ? 'Mi respuesta' : 'Respuesta correcta'}
          answers={actividad ? actividad.puntosImagen.map((punto, idx) => ({
            question: `Punto ${idx + 1}`,
            studentAnswer: respuestas[punto.id] || '(No marcado)',
            correctAnswer: punto.respuesta,
            isCorrect: respuestas[punto.id]?.trim().toLowerCase() === punto.respuesta.trim().toLowerCase(),
          })) : []}
          onClose={() => setShowAnswerModal(false)}
          mode={answerModalMode}
        />
      )}
    </div>
  );
}
