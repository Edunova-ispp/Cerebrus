import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import kingImg from '../../assets/props/king.png';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import ActivityResultScreen, { type ActivityResultConfig } from '../../components/ActivityResultScreen/ActivityResultScreen';
import AnswerViewModal from '../../components/AnswerViewModal/AnswerViewModal';
import './OrdenacionAlumno.css';

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
  readonly permitirReintento?: boolean;
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
  readonly encontrarRespuestaAlumno?: boolean;
};

type RespAlumnoOrdenacionCreateResponse = {
  readonly respAlumnoOrdenacion: {
    readonly id: number;
    readonly correcta: boolean;
  };
  readonly comentario: string;
};

type ActividadAlumnoDTO = {
  readonly id: number;
  readonly puntuacion?: number | null;
  readonly nota?: number;
  readonly fechaInicio?: string | null;
  readonly fechaFin?: string | null;
  readonly numAbandonos?: number;
};

type RespAlumnoOrdenacionDetalleDTO = {
  readonly id: number;
  readonly correcta: boolean;
  readonly valoresAlum: string[];
  readonly valoresCorrectos: string[];
};

function isCompletedAttempt(fechaFin?: string | null): boolean {
  if (!fechaFin) return false;
  const parsed = new Date(fechaFin);
  if (Number.isNaN(parsed.getTime())) return false;
  return parsed.getFullYear() !== 1970;
}

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

  // Aceptar rutas relativas (Vite/NGINX) si parecen imagen por extensión.
  // Ejemplos: /seed/ordenacion/html.svg, seed/ordenacion/html.svg
  const pathLike = trimmed.split('#')[0]?.split('?')[0]?.toLowerCase() ?? '';
  if (/\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(pathLike)) return true;

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

function normalizeForComparison(value?: string): string {
  return (value ?? '').trim();
}

export default function OrdenacionAlumno() {
  const { ordenacionId } = useParams<{ ordenacionId: string }>();
  const navigate = useNavigate();

  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const [ordenacion, setOrdenacion] = useState<OrdenacionDTO | null>(null);
  const [items, setItems] = useState<string[]>([]);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [lastAttemptScore, setLastAttemptScore] = useState<number | null>(null);
  const [lastAttemptGrade, setLastAttemptGrade] = useState<number | null>(null);
  const [activityConfig, setActivityConfig] = useState<ActivityResultConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>('');
  const [feedback, setFeedback] = useState<{ correcta: boolean; comentario?: string } | null>(null);
  const [showAnswerModal, setShowAnswerModal] = useState(false);
  const [answerModalMode, setAnswerModalMode] = useState<'student' | 'correct'>('student');
  const [submittedOrder, setSubmittedOrder] = useState<string[] | null>(null);
  const [correctOrder, setCorrectOrder] = useState<string[] | null>(null);
  const incorrectosRef = useRef(0);
  const pendingRespuestaIdRef = useRef<number | null>(null);
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
  
  const ordenacionIdNum = useMemo(() => {
    if (!ordenacionId) return Number.NaN;
    return Number.parseInt(ordenacionId, 10);
  }, [ordenacionId]);

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
        const ordRes = await apiFetch(`${apiBase}/api/ordenaciones/${ordenacionIdNum}`);
        const ordData = (await ordRes.json()) as OrdenacionDTO;
        setOrdenacion(ordData);
        setItems(Array.isArray(ordData.valores) ? [...ordData.valores] : []);
        
        // Load activity configuration
        setActivityConfig({
          showScore: ordData.mostrarPuntuacion ?? true,
          allowRetry: ordData.permitirReintento ?? false,
          showCorrectAnswer: ordData.encontrarRespuestaMaestro ?? true,
          showStudentAnswer: ordData.encontrarRespuestaAlumno ?? true,
        });

        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new TypeError('No se pudo identificar al alumno conectado. Inicia sesión de nuevo.');

        let hasExisting = false;
        try {
          const getAA = await apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${ordData.id}`);
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            hasExisting = true;
            setActividadAlumnoId(aaData.id);
            if (isCompletedAttempt(aaData.fechaFin)) {
              completedRef.current = true;
              setFeedback({ correcta: true });
              setLastAttemptScore(aaData.puntuacion ?? 0);
              setLastAttemptGrade(aaData.nota ?? null);
              try {
                const ultimaRes = await apiFetch(`${apiBase}/api/respuestas-alumno-ordenacion/actividad-alumno/${aaData.id}/ultima`);
                const ultimaData = (await ultimaRes.json()) as RespAlumnoOrdenacionDetalleDTO;
                setSubmittedOrder(Array.isArray(ultimaData.valoresAlum) ? [...ultimaData.valoresAlum] : null);
                setCorrectOrder(Array.isArray(ultimaData.valoresCorrectos) ? [...ultimaData.valoresCorrectos] : null);
              } catch {
                setSubmittedOrder(null);
                setCorrectOrder(null);
              }
            }
          }
        } catch {
          hasExisting = false;
        }

        if (!hasExisting) {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: ordData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new TypeError('Respuesta inválida al crear ActividadAlumno');
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
      const res = await apiFetch(`${apiBase}/api/respuestas-alumno-ordenacion`, {
        method: 'POST',
        body: JSON.stringify({
          actividadAlumno: { id: actividadAlumnoId },
          ordenacion: { id: ordenacion.id },
          valoresAlum: items,
        }),
      });
      if (!res.ok) throw new TypeError('Error al guardar la respuesta');

      const data = (await res.json()) as RespAlumnoOrdenacionCreateResponse;
      setSubmittedOrder([...items]);
      const respuestaId = data?.respAlumnoOrdenacion?.id;
      const correcta = Boolean(data?.respAlumnoOrdenacion?.correcta);
      const comentario = typeof data?.comentario === 'string' ? data.comentario : '';

      try {
        const ultimaRes = await apiFetch(`${apiBase}/api/respuestas-alumno-ordenacion/actividad-alumno/${actividadAlumnoId}/ultima`);
        const ultimaData = (await ultimaRes.json()) as RespAlumnoOrdenacionDetalleDTO;
        setSubmittedOrder(Array.isArray(ultimaData.valoresAlum) ? [...ultimaData.valoresAlum] : [...items]);
        setCorrectOrder(Array.isArray(ultimaData.valoresCorrectos) ? [...ultimaData.valoresCorrectos] : null);
      } catch {
        setCorrectOrder(null);
      }

      if (correcta) {
        if (respuestaId) pendingRespuestaIdRef.current = respuestaId;

        if (respuestaId) {
          const aaRes = await apiFetch(`${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`, {
            method: 'PUT',
            body: JSON.stringify([respuestaId]),
          });
          const aaData = (await aaRes.json()) as ActividadAlumnoDTO;
          setLastAttemptScore(aaData.puntuacion ?? 0);
          setLastAttemptGrade(aaData.nota ?? null);
        } else {
          setLastAttemptScore(0);
          setLastAttemptGrade(0);
        }

        completedRef.current = true;
        setFeedback({ correcta: true, comentario: undefined });
        return;
      }

      // Respuesta incorrecta: contar intento fallido
      const nuevosIncorrectos = incorrectosRef.current + 1;
      incorrectosRef.current = nuevosIncorrectos;
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

  const handleContinuar = async () => {
    navigate(-1);
  };

  const handleRetry = async () => {
    if (!ordenacion) return;

    try {
      const alumnoId = getCurrentUserIdFromJwt();
      if (!alumnoId) {
        throw new TypeError('No se pudo identificar al alumno.');
      }

      const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
        method: 'POST',
        body: JSON.stringify({ alumnoId, actividadId: ordenacion.id }),
      });
      const aaData = (await createAA.json()) as ActividadAlumnoDTO;
      if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
        setActividadAlumnoId(aaData.id);
        actividadAlumnoIdRef.current = aaData.id;
      }

      completedRef.current = false;
      abandonReportedRef.current = false;
      setItems([...ordenacion.valores]);
      setFeedback(null);
      setSubmittedOrder(null);
      setCorrectOrder(null);
      setLastAttemptScore(null);
      setLastAttemptGrade(null);
      setShowAnswerModal(false);
      setAnswerModalMode('student');
      setError('');
      incorrectosRef.current = 0;
      pendingRespuestaIdRef.current = null;
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo crear un nuevo intento');
    }
  };

  const handleViewStudentAnswers = () => {
    const loadAndOpen = async () => {
      if (!actividadAlumnoId) {
        setError('No se ha podido cargar la respuesta de este intento.');
        return;
      }

      try {
        const ultimaRes = await apiFetch(`${apiBase}/api/respuestas-alumno-ordenacion/actividad-alumno/${actividadAlumnoId}/ultima`);
        const ultimaData = (await ultimaRes.json()) as RespAlumnoOrdenacionDetalleDTO;
        setSubmittedOrder(Array.isArray(ultimaData.valoresAlum) ? [...ultimaData.valoresAlum] : null);
        setCorrectOrder(Array.isArray(ultimaData.valoresCorrectos) ? [...ultimaData.valoresCorrectos] : null);
      } catch {
        // Si no hay detalle disponible, mostramos lo que ya tenemos sin forzar comparaciones falsas.
      }

      setAnswerModalMode('student');
      setShowAnswerModal(true);
    };

    void loadAndOpen();
  };

  const handleViewCorrectAnswers = () => {
    const loadAndOpen = async () => {
      if (!actividadAlumnoId) {
        setError('No se ha podido cargar la respuesta correcta de este intento.');
        return;
      }

      try {
        const ultimaRes = await apiFetch(`${apiBase}/api/respuestas-alumno-ordenacion/actividad-alumno/${actividadAlumnoId}/ultima`);
        const ultimaData = (await ultimaRes.json()) as RespAlumnoOrdenacionDetalleDTO;
        setSubmittedOrder(Array.isArray(ultimaData.valoresAlum) ? [...ultimaData.valoresAlum] : null);
        setCorrectOrder(Array.isArray(ultimaData.valoresCorrectos) ? [...ultimaData.valoresCorrectos] : null);
      } catch {
        // Si falla la consulta, evitamos usar el orden mezclado cargado para jugar.
      }

      setAnswerModalMode('correct');
      setShowAnswerModal(true);
    };

    void loadAndOpen();
  };

  if (loading) {
    return (
      <div className="ordenacion-alumno-page">
        <NavbarMisCursos />
        <main className="ordenacion-alumno-main">
          <p className="ca-text">Cargando...</p>
        </main>
      </div>
    );
  }

  return (
    <div className="ordenacion-alumno-page">
      <NavbarMisCursos />

      <main className="ordenacion-alumno-main">
        {error && (
          <p className="ca-text" style={{ marginTop: 0 }}>
            {error}
          </p>
        )}

        {ordenacion && (
          
          <>
            <ActivityHeader
              title={ordenacion.titulo}
              subtitle={ordenacion.descripcion ?? undefined}
              guideType="ordenacion"
              guideRole="alumno"
            />

            {/* Layout: rey izquierda, items derecha */}
<div className="ord-content-row">

  {/* Columna izquierda: botón salir + rey + bocadillo */}
  <div className="ord-left-col">

    <div className="ord-king-row">
      <div className="ord-speech-bubble">
        <span>Esto es un caos</span>
        <span>Ordena las casillas</span>
        <span>Ordena el reino</span>
      </div>
      <img src={kingImg} alt="Rey" className="ord-king-img" />
    </div>
  </div>

  {/* Columna derecha: items + botón enviar */}
  <div>
    <div className="ord-items">
      {items.map((value, index) => (
        <div key={`${value}-${index}`} className="ord-item">
          <div className="ord-item-index">{index + 1}</div>

          <div className="ord-item-value">
            {isImageString(value) ? (
              <img src={value} alt={`Elemento ${index + 1}`} className="ord-item-img" />
            ) : (
              <div className="ord-item-text">{value}</div>
            )}
          </div>

          <div className="ord-item-actions">
            <button
              className="ord-arrow-btn"
              type="button"
              disabled={index === 0 || Boolean(feedback?.correcta)}
              onClick={() => setItems((prev) => moveItem(prev, index, index - 1))}
            >
              ↑
            </button>
            <button
              className="ord-arrow-btn"
              type="button"
              disabled={index === items.length - 1 || Boolean(feedback?.correcta)}
              onClick={() => setItems((prev) => moveItem(prev, index, index + 1))}
            >
              ↓
            </button>
          </div>
        </div>
      ))}
    </div>

    <div className="ord-bottom">
      <div className="ord-bottom-inner">
        {feedback && (
          <div className="ord-feedback">
            <div>{feedback.correcta ? 'Tu respuesta es correcta.' : 'Tu respuesta es incorrecta.'}</div>
            {ordenacion.respVisible && feedback.comentario && <div>{feedback.comentario}</div>}
          </div>
        )}
        <button
          className="ca-btn-guardar"
          type="button"
          disabled={submitting || items.length === 0 || !actividadAlumnoId}
          onClick={handleSubmit}
        >
          {submitting ? 'Enviando...' : 'Enviar'}
        </button>
      </div>
    </div>
  </div>

</div>
            
          </>
        )}

        {!ordenacion && !error && <p className="ca-text">No se encontró la ordenación.</p>}

        {feedback?.correcta && activityConfig ? (
          <ActivityResultScreen
            title="¡ORDENACIÓN COMPLETADA!"
            score={lastAttemptScore ?? (ordenacion?.puntuacion || 0)}
            maxScore={ordenacion?.puntuacion || 100}
            grade={lastAttemptGrade ?? undefined}
            config={activityConfig}
            onContinue={handleContinuar}
            onRetry={handleRetry}
            onViewStudentAnswer={handleViewStudentAnswers}
            onViewCorrectAnswer={handleViewCorrectAnswers}
            onCancel={() => navigate(-1)}
          />
        ) : feedback?.correcta ? (
          <CompletionPopup title="¡ORDENACIÓN COMPLETADA!" onContinue={handleContinuar} />
        ) : null}

        {showAnswerModal && (
          <AnswerViewModal
            title={answerModalMode === 'student' ? 'Mi respuesta' : 'Respuesta correcta'}
            answers={ordenacion ? (() => {
              const studentValues = submittedOrder ?? items;
              const correctValues = correctOrder ?? [];

              if (answerModalMode === 'student') {
                return studentValues.map((studentValue, idx) => {
                  const hasReference = idx < correctValues.length;
                  const studentNorm = normalizeForComparison(studentValue);
                  const correctNorm = normalizeForComparison(correctValues[idx]);

                  return {
                    question: `Posición ${idx + 1}`,
                    studentAnswer: studentValue,
                    correctAnswer: hasReference ? (correctValues[idx] ?? '') : '(Sin referencia)',
                    isCorrect: hasReference ? studentNorm === correctNorm : undefined,
                  };
                });
              }

              return correctValues.map((correctValue, idx) => ({
                question: `Posición ${idx + 1}`,
                studentAnswer: studentValues[idx] ?? '(No respondida)',
                correctAnswer: correctValue,
                isCorrect: normalizeForComparison(studentValues[idx]) === normalizeForComparison(correctValue),
              }));
            })() : []}
            onClose={() => setShowAnswerModal(false)}
            mode={answerModalMode}
          />
        )}
      </main>
    </div>
  );
}