import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { getCurrentUserInfo } from '../../types/curso';
import { apiFetch } from '../../utils/api';
import ActivityResultScreen, { type ActivityResultConfig } from '../../components/ActivityResultScreen/ActivityResultScreen';
import AnswerViewModal from '../../components/AnswerViewModal/AnswerViewModal';

import varitaImg from '../../assets/props/varita.png';
import libroImg from '../../assets/props/libro.png';
import pergaminoImg from '../../assets/props/pergamino.png';
import cristal1Img from '../../assets/props/cristal1.png';
import cristal2Img from '../../assets/props/cristal2.png';
import espadaImg from '../../assets/props/espada.png';
import ActivityGuideButton from '../../components/ActivityGuideButton/ActivityGuideButton';

import './ClasificacionAlumno.css';

type RespuestaDTO = {
  readonly id: number;
  readonly respuesta: string;
  readonly imagen: string | null;
};

type PreguntaDTO = {
  readonly id: number;
  readonly pregunta: string;
  readonly imagen: string | null;
  readonly respuestas: RespuestaDTO[];
};

type ClasificacionDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly imagen: string | null;
  readonly respVisible: boolean;
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly temaId: number | null;
  readonly preguntas: PreguntaDTO[];
  readonly permitirReintento?: boolean;
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
  readonly encontrarRespuestaAlumno?: boolean;
};

type GeneralResponseDTO = {
  id?: number;
  correcta?: boolean;
  comentario?: string;
};

type ActividadAlumnoDTO = { 
  readonly id: number;
  readonly puntuacion?: number | null;
  readonly nota?: number;
  readonly fechaFin?: string | null;
};

type RespAlumnoGeneralResumenDTO = {
  readonly preguntaId: number | null;
  readonly respuesta: string;
  readonly correcta?: boolean | null;
  readonly respuestaCorrecta?: string | null;
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

function isCompletedAttempt(fechaFin?: string | null): boolean {
  if (!fechaFin) return false;
  const parsed = new Date(fechaFin);
  if (Number.isNaN(parsed.getTime())) return false;
  return parsed.getFullYear() !== 1970;
}

function shuffleArray<T>(array: T[]): T[] {
  const copy = [...array];
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

export default function ClasificacionAlumno() {
  const { clasificacionId } = useParams<{ clasificacionId: string }>();
  const navigate = useNavigate();

  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const [clasificacion, setClasificacion] = useState<ClasificacionDTO | null>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [lastAttemptScore, setLastAttemptScore] = useState<number | null>(null);
  const [lastAttemptGrade, setLastAttemptGrade] = useState<number | null>(null);
  const [activityConfig, setActivityConfig] = useState<ActivityResultConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>('');
  const [feedback, setFeedback] = useState<{ correcta: boolean; comentario?: string } | null>(null);
  const [submitted, setSubmitted] = useState(false);

  const [respuestasAsociadas, setRespuestasAsociadas] = useState<Map<number, number[]>>(new Map());
  const [respuestasDesordenadas, setRespuestasDesordenadas] = useState<RespuestaDTO[]>([]);
  const [submittedAnswersByQuestion, setSubmittedAnswersByQuestion] = useState<Map<number, string[]>>(new Map());
  const [correctAnswersByQuestion, setCorrectAnswersByQuestion] = useState<Map<number, string>>(new Map());
  const [showAnswerModal, setShowAnswerModal] = useState(false);
  const [answerModalMode, setAnswerModalMode] = useState<'student' | 'correct'>('student');

  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

  const clasificacionIdNum = useMemo(() => {
    if (!clasificacionId) return Number.NaN;
    return Number.parseInt(clasificacionId, 10);
  }, [clasificacionId]);

  useEffect(() => {
    actividadAlumnoIdRef.current = actividadAlumnoId;
  }, [actividadAlumnoId]);

  useEffect(() => {
    return () => {
      const id = actividadAlumnoIdRef.current;
      if (!id || completedRef.current || abandonReportedRef.current) return;
      abandonReportedRef.current = true;
      apiFetch(`${apiBase}/api/actividades-alumno/${id}/abandon`, { method: 'POST' }).catch(() => {});
    };
  }, [apiBase]);

  useEffect(() => {
    const run = async () => {
      if (initInFlightRef.current) return;
      initInFlightRef.current = true;

      setLoading(true);
      setError('');

      try {
        const clsRes = await apiFetch(`${apiBase}/api/generales/clasificacion/${clasificacionIdNum}`);
        const clsData = (await clsRes.json()) as ClasificacionDTO;

        const allItems: RespuestaDTO[] = [];
        const cleanPreguntas: PreguntaDTO[] = [];
        const seenPregIds = new Set();
        const seenRespIds = new Set();

        clsData.preguntas.forEach((p) => {
          if (!seenPregIds.has(p.id)) {
            seenPregIds.add(p.id);
            const pRespuestas: RespuestaDTO[] = [];
            p.respuestas.forEach(r => {
              if (!seenRespIds.has(r.id)) {
                seenRespIds.add(r.id);
                pRespuestas.push(r);
                allItems.push(r);
              }
            });
            cleanPreguntas.push({ ...p, respuestas: pRespuestas });
          }
        });

        setClasificacion({ ...clsData, preguntas: cleanPreguntas });
        setRespuestasDesordenadas(shuffleArray(allItems));

        const emptyMap = new Map<number, number[]>();
        cleanPreguntas.forEach((p) => emptyMap.set(p.id, []));
        setRespuestasAsociadas(emptyMap);

        // Load activity configuration
        setActivityConfig({
          showScore: clsData.mostrarPuntuacion ?? true,
          allowRetry: clsData.permitirReintento ?? false,
          showCorrectAnswer: clsData.encontrarRespuestaMaestro ?? true,
          showStudentAnswer: clsData.encontrarRespuestaAlumno ?? true,
        });

        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new Error('No se pudo identificar al alumno.');

        let hasExisting = false;
        try {
          const getAA = await apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${clsData.id}`);
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            hasExisting = true;
            setActividadAlumnoId(aaData.id);
            if (isCompletedAttempt(aaData.fechaFin)) {
              completedRef.current = true;
              setSubmitted(true);
              setLastAttemptScore(aaData.puntuacion ?? 0);
              setLastAttemptGrade(aaData.nota ?? null);
              try {
                const histRes = await apiFetch(`${apiBase}/api/respuestas-alumno-general/actividad-alumno/${aaData.id}`);
                const histData = (await histRes.json()) as RespAlumnoGeneralResumenDTO[];
                const studentMap = new Map<number, string[]>();
                const correctMap = new Map<number, string>();
                for (const item of histData) {
                  if (typeof item.preguntaId !== 'number') continue;
                  const current = studentMap.get(item.preguntaId) ?? [];
                  current.push(item.respuesta);
                  studentMap.set(item.preguntaId, current);
                  if (item.respuestaCorrecta && !correctMap.has(item.preguntaId)) {
                    correctMap.set(item.preguntaId, item.respuestaCorrecta);
                  }
                }
                setSubmittedAnswersByQuestion(studentMap);
                setCorrectAnswersByQuestion(correctMap);
              } catch {
                setSubmittedAnswersByQuestion(new Map());
                setCorrectAnswersByQuestion(new Map());
              }
            }
          }
        } catch {
          hasExisting = false;
        }

        if (!hasExisting) {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: clsData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          setActividadAlumnoId(aaData.id);
        }

      } catch (e: unknown) {
        setError(e instanceof Error ? e.message : 'Error cargando la clasificación');
      } finally {
        setLoading(false);
        initInFlightRef.current = false;
      }
    };
    run();
  }, [clasificacionIdNum, apiBase]);

  const isRespuestaAsociada = (respuestaId: number): boolean => {
    for (const [, respuestaIds] of respuestasAsociadas) {
      if (respuestaIds.includes(respuestaId)) return true;
    }
    return false;
  };

  const handleRetry = async () => {
    if (!clasificacion) return;

    setError('');
    try {
      const alumnoId = getCurrentUserIdFromJwt();
      if (!alumnoId) throw new Error('No se pudo identificar al alumno.');

      const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
        method: 'POST',
        body: JSON.stringify({ alumnoId, actividadId: clasificacion.id }),
      });
      const aaData = (await createAA.json()) as ActividadAlumnoDTO;
      if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
        setActividadAlumnoId(aaData.id);
      }

      completedRef.current = false;
      abandonReportedRef.current = false;

      setRespuestasAsociadas(new Map(clasificacion.preguntas.map(p => [p.id, []])));
      setRespuestasDesordenadas(shuffleArray(
        clasificacion.preguntas.flatMap((p) => p.respuestas)
      ));
      setSubmittedAnswersByQuestion(new Map());
      setCorrectAnswersByQuestion(new Map());
      setLastAttemptScore(null);
      setLastAttemptGrade(null);
      setFeedback(null);
      setSubmitted(false);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'No se pudo crear el nuevo intento');
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

  const handleDragStart = (e: React.DragEvent<HTMLDivElement>, respuestaId: number) => {
    if (submitted) {
      e.preventDefault();
      return;
    }
    e.dataTransfer.setData('text/plain', respuestaId.toString());
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    if (submitted) return;
    e.preventDefault(); 
    e.dataTransfer.dropEffect = 'move';
  };

  const handleDropToCategory = (e: React.DragEvent<HTMLDivElement>, preguntaId: number) => {
    if (submitted) return;
    e.preventDefault();
    const respuestaIdStr = e.dataTransfer.getData('text/plain');
    if (!respuestaIdStr) return;
    
    const respuestaId = Number.parseInt(respuestaIdStr, 10);

    setRespuestasAsociadas((prev) => {
      const newMap = new Map(prev);
      for (const [key, val] of newMap.entries()) {
        newMap.set(key, val.filter(id => id !== respuestaId));
      }
      const current = newMap.get(preguntaId) || [];
      newMap.set(preguntaId, [...current, respuestaId]);
      return newMap;
    });
  };

  const handleDropToShelf = (e: React.DragEvent<HTMLDivElement>) => {
    if (submitted) return;
    e.preventDefault();
    const respuestaIdStr = e.dataTransfer.getData('text/plain');
    if (!respuestaIdStr) return;
    
    const respuestaId = Number.parseInt(respuestaIdStr, 10);

    setRespuestasAsociadas((prev) => {
      const newMap = new Map(prev);
      for (const [key, val] of newMap.entries()) {
        newMap.set(key, val.filter(id => id !== respuestaId));
      }
      return newMap;
    });
  };

  const handleSubmit = async () => {
    setError('');
    setFeedback(null);

    if (!clasificacion || !actividadAlumnoId) return;

    setSubmitting(true);
    try {
      const respuestasGeneradasIds: number[] = [];
      let todoCorrecto = true;
      const comentariosArr: string[] = []; 

      const promesas: Promise<GeneralResponseDTO>[] = [];
      respuestasAsociadas.forEach((respuestaIds, preguntaId) => {
        respuestaIds.forEach((respuestaId) => {
          const p = apiFetch(`${apiBase}/api/respuestas-alumno-general`, {
            method: 'POST',
            body: JSON.stringify({ actividadAlumnoId, preguntaId, respuestaId }),
          }).then(res => res.json() as Promise<GeneralResponseDTO>);
          promesas.push(p);
        });
      });

      const resultados = await Promise.all(promesas);
      
      resultados.forEach(r => {
        if (r.id) respuestasGeneradasIds.push(r.id);
        if (r.comentario && !comentariosArr.includes(r.comentario)) {
            comentariosArr.push(r.comentario);
        }
      });

      if (respuestasGeneradasIds.length > 0) {
        const resCorreccion = await apiFetch(`${apiBase}/api/actividades-alumno/corregir-automaticamente-general-clasificacion/${actividadAlumnoId}`, {
          method: 'PUT',
          body: JSON.stringify(respuestasGeneradasIds),
        });

        if (resCorreccion.ok) {
          const aaActualizada = (await resCorreccion.json()) as ActividadAlumnoDTO; 
          if (typeof aaActualizada.puntuacion === 'number') {
            setLastAttemptScore(aaActualizada.puntuacion);
          }
          setLastAttemptGrade(typeof aaActualizada.nota === 'number' ? aaActualizada.nota : null);
          
          if (typeof aaActualizada.puntuacion === 'number' && typeof clasificacion.puntuacion === 'number') {
            if (aaActualizada.puntuacion < clasificacion.puntuacion) {
              todoCorrecto = false;
            }
          }
        }

        try {
          const histRes = await apiFetch(`${apiBase}/api/respuestas-alumno-general/actividad-alumno/${actividadAlumnoId}`);
          const histData = (await histRes.json()) as RespAlumnoGeneralResumenDTO[];
          const studentMap = new Map<number, string[]>();
          const correctMap = new Map<number, string>();
          for (const item of histData) {
            if (typeof item.preguntaId !== 'number') continue;
            const current = studentMap.get(item.preguntaId) ?? [];
            current.push(item.respuesta);
            studentMap.set(item.preguntaId, current);
            if (item.respuestaCorrecta && !correctMap.has(item.preguntaId)) {
              correctMap.set(item.preguntaId, item.respuestaCorrecta);
            }
          }
          setSubmittedAnswersByQuestion(studentMap);
          setCorrectAnswersByQuestion(correctMap);
        } catch {
          // Keep local fallbacks when history fetch fails
        }
      }

      completedRef.current = true;
      setSubmitted(true);

      if (clasificacion.respVisible) {
        setFeedback({
          correcta: todoCorrecto,
          comentario: todoCorrecto
            ? "¡Excelente! Has conjurado todos los hechizos correctamente." 
            : (comentariosArr.length > 0
                ? comentariosArr.join(" | ")
                : "Vaya, parece que la energía mágica es inestable. Algunas runas no coinciden.")
        });
      } else {
        setFeedback({
          correcta: todoCorrecto,
          comentario: "Fórmulas selladas en el Grimorio correctamente." 
        });
      }

    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error enviando la respuesta');
    } finally {
      setSubmitting(false);
    }
  };

  const totalItems = respuestasDesordenadas.length;
  const assignedItems = Array.from(respuestasAsociadas.values()).reduce((acc, curr) => acc + curr.length, 0);
  const progressPct = totalItems === 0 ? 100 : (assignedItems / totalItems) * 100;
  const magicReached = assignedItems === totalItems && totalItems > 0;
  const allAnswered = assignedItems === totalItems;

  if (loading) {
    return (
      <div className="clasificacion-alumno-page">
        <NavbarMisCursos />
        <main className="clasificacion-alumno-main"><p className="ca-text">Abriendo el Grimorio...</p></main>
      </div>
    );
  }

  return (
    <div className="clasificacion-alumno-page">
      <NavbarMisCursos />
      <main className="clasificacion-alumno-main">
        {error && <p className="ca-text" style={{ marginTop: 0, color: '#c0392b' }}>{error}</p>}

        {clasificacion && (
          <>
            <div className="clf-header">
              <button className="clf-exit-btn" type="button" onClick={() => navigate(-1)}>
                <img src={espadaImg} alt="" className="clf-exit-icon" /> Huir
              </button>
              <div className="clf-title-banner">
                <h1 className="clf-title">{clasificacion.titulo}</h1>
              </div>
              <ActivityGuideButton activityType="clasificacion" role="alumno" />
            </div>
            <div className="ta-battle-bar" style={{ marginBottom: '16px' }}>
              <img src={varitaImg} alt="Varita" className="ta-magic-start" />
              <div className="ta-progress-track">
                <div className="ta-progress-fill" style={{ width: `${progressPct}%` }} />
                {!submitted && progressPct > 0 && <div className="ta-spell-head" style={{ left: `calc(${progressPct}% - 10px)` }} />}
              </div>
              <img src={libroImg} alt="Libro de Hechizos" className={`ta-magic-end ${magicReached ? ' ta-book--glowing' : ''}`} />
            </div>
            {submitted && feedback && (
              <div className={`ta-score-banner ${(!clasificacion.respVisible) ? 'ta-score-banner--neutral' : feedback.correcta ? 'ta-score-banner--perfect' : 'ta-score-banner--wrong'}`}>
                {feedback.comentario}
              </div>
            )}
            <div className="clf-container">
              <div className="clf-preguntas">
                <h2 className="clf-section-title">Páginas del Grimorio</h2>
                <div className="clf-preguntas-list">
                  {clasificacion.preguntas.map((pregunta) => (
                    <div 
                      key={`cat-${pregunta.id}`} 
                      className="clf-pergamino-box"
                      onDragOver={handleDragOver}
                      onDrop={(e) => handleDropToCategory(e, pregunta.id)}
                      style={{ backgroundImage: `url(${pergaminoImg})` }}
                    >
                      <h3 className="clf-pergamino-title">{pregunta.pregunta}</h3>
                      
                      <div className="clf-respuestas-asociadas">
                        {(respuestasAsociadas.get(pregunta.id) || []).map((respuestaId) => {
                          const r = respuestasDesordenadas.find(item => item.id === respuestaId);
                          if (!r) return null;
                          const cristalSrc = r.id % 2 === 0 ? cristal1Img : cristal2Img;
                          
                          return (
                            <div 
                              key={`asoc-${pregunta.id}-${respuestaId}`} 
                              className="clf-runa-wrapper small"
                              draggable={!submitted}
                              onDragStart={(e) => handleDragStart(e, respuestaId)}
                            >
                              <img src={cristalSrc} alt="Cristal" className="clf-runa-img" />
                              <span className="clf-runa-text">{r.respuesta}</span> 
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="clf-respuestas">
                <h2 className="clf-section-title">Mesa de Alquimia</h2>
                <div 
                  className="clf-respuestas-shelf"
                  onDragOver={handleDragOver}
                  onDrop={handleDropToShelf}
                >
                  {respuestasDesordenadas.filter(r => !isRespuestaAsociada(r.id)).map((respuesta) => {
                    const cristalSrc = respuesta.id % 2 === 0 ? cristal1Img : cristal2Img;
                    return (
                      <div className="clf-respuesta-item" key={`disp-${respuesta.id}`}>
                        <div 
                          className="clf-runa-wrapper"
                          draggable={!submitted}
                          onDragStart={(e) => handleDragStart(e, respuesta.id)}
                        >
                          <img src={cristalSrc} alt="Cristal" className="clf-runa-img" />
                          <span className="clf-runa-text">{respuesta.respuesta}</span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>

            <div className="clf-bottom">
              {!submitted ? (
                <button type="button" className="ca-btn-guardar" onClick={handleSubmit} disabled={submitting || !allAnswered}>
                  {submitting ? 'Sellando...' : '¡Sellar Hechizos!'}
                </button>
              ) : (
                <button type="button" className="ca-btn-guardar" onClick={() => navigate(-1)}>Cerrar Grimorio</button>
              )}
            </div>

            {submitted && activityConfig ? (
              <ActivityResultScreen
                title="¡CLASIFICACIÓN COMPLETADA!"
                score={lastAttemptScore ?? 0}
                maxScore={clasificacion.puntuacion}
                grade={lastAttemptGrade ?? undefined}
                config={activityConfig}
                onContinue={() => navigate(-1)}
                onRetry={handleRetry}
                onViewStudentAnswer={handleViewStudentAnswers}
                onViewCorrectAnswer={handleViewCorrectAnswers}
                onCancel={() => navigate(-1)}
              />
            ) : submitted ? (
              <div style={{ position: 'fixed', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                <div style={{ textAlign: 'center', padding: '2rem', backgroundColor: 'white', borderRadius: '8px' }}>
                  <h2>¡Actividad completada!</h2>
                  <button onClick={() => navigate(-1)} style={{ marginTop: '1rem', padding: '0.5rem 1rem' }}>Continuar</button>
                </div>
              </div>
            ) : null}
          </>
        )}
      </main>

      {showAnswerModal && (
        <AnswerViewModal
          title={answerModalMode === 'student' ? 'Mi respuesta' : 'Respuesta correcta'}
          answers={clasificacion ? clasificacion.preguntas.map((pregunta) => {
            const respuestasIds = respuestasAsociadas.get(pregunta.id) || [];
            const respuestasTexto = pregunta.respuestas
              .filter(r => respuestasIds.includes(r.id))
              .map(r => r.respuesta)
              .join(', ');
            return {
              question: pregunta.pregunta,
              studentAnswer: submittedAnswersByQuestion.get(pregunta.id)?.join(', ') || respuestasTexto || '(No respondida)',
              correctAnswer: correctAnswersByQuestion.get(pregunta.id) || pregunta.respuestas.map(r => r.respuesta).join(', ') || '(No disponible)',
              isCorrect: feedback?.correcta,
            };
          }) : []}
          onClose={() => setShowAnswerModal(false)}
          mode={answerModalMode}
        />
      )}
    </div>
  );
}