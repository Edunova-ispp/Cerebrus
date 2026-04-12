import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import ActivityResultScreen, { type ActivityResultConfig } from '../../components/ActivityResultScreen/ActivityResultScreen';
import AnswerViewModal from '../../components/AnswerViewModal/AnswerViewModal';
import './TestAlumno.css';
import dragonImg from '../../assets/props/dragon.png';
import caballeroImg from '../../assets/props/caballero.png';


// ── Types ─────────────────────────────────────────────────────────────────

type RespuestaDTO = {
  readonly id: number;
  readonly respuesta: string;
  readonly correcta?: boolean;
};

type PreguntaDTO = {
  readonly id: number;
  readonly pregunta: string;
  readonly imagen: string | null;
  readonly respuestas: RespuestaDTO[];
  readonly numRespuestasCorrectas?: number | null;
};

type GeneralTestDTO = {
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

type RespAlumnoGeneralCreateResponse = {
  readonly id: number;
  readonly correcta: boolean;
  readonly comentario: string;
};

type QuestionResult = {
  readonly correcta: boolean;
  readonly comentario: string;
  readonly selectedIds: number[];
  readonly selectedCorrectById: Map<number, boolean>;
};

type ActividadAlumnoDTO = {
  readonly id: number;
  readonly puntuacion?: number | null;
  readonly fechaFin?: string | null;
};

type RespAlumnoGeneralResumenDTO = {
  readonly preguntaId: number | null;
  readonly respuesta: string;
  readonly correcta?: boolean | null;
  readonly respuestaCorrecta?: string | null;
};

// ── Helpers ───────────────────────────────────────────────────────────────

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

// ── Component ─────────────────────────────────────────────────────────────

export default function TestAlumno() {
  const { testId } = useParams<{ testId: string }>();
  const navigate = useNavigate();

  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const [test, setTest] = useState<GeneralTestDTO | null>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [lastAttemptScore, setLastAttemptScore] = useState<number | null>(null);
  const [activityConfig, setActivityConfig] = useState<ActivityResultConfig | null>(null);
  // preguntaId -> set of selected respuesta IDs
  const [selections, setSelections] = useState<Map<number, Set<number>>>(new Map());
  const [results, setResults] = useState<Map<number, QuestionResult>>(new Map());
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [showAnswerModal, setShowAnswerModal] = useState(false);
  const [answerModalMode, setAnswerModalMode] = useState<'student' | 'correct'>('student');
  const [submittedAnswersByQuestion, setSubmittedAnswersByQuestion] = useState<Map<number, string[]>>(new Map());
  const [submittedCorrectByQuestion, setSubmittedCorrectByQuestion] = useState<Map<number, string>>(new Map());
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
  // Pagination: which question is currently shown
  const [currentIndex, setCurrentIndex] = useState(0);

  const testIdNum = useMemo(() => {
    if (!testId) return Number.NaN;
    return Number.parseInt(testId, 10);
  }, [testId]);

  // Keep ref in sync for cleanup
  useEffect(() => {
    actividadAlumnoIdRef.current = actividadAlumnoId;
  }, [actividadAlumnoId]);

  // Abandon on unmount if not submitted
  useEffect(() => {
    return () => {
      const id = actividadAlumnoIdRef.current;
      if (!id || completedRef.current || abandonReportedRef.current) return;
      abandonReportedRef.current = true;
      apiFetch(`${apiBase}/api/actividades-alumno/${id}/abandon`, { method: 'POST' }).catch(() => {});
    };
  }, [apiBase]);

  // Initial load
  useEffect(() => {
    const run = async () => {
      if (initInFlightRef.current) return;
      initInFlightRef.current = true;

      if (!testId || Number.isNaN(testIdNum)) {
        setError('Falta el id del test en la URL');
        setLoading(false);
        initInFlightRef.current = false;
        return;
      }

      setLoading(true);
      setError('');

      try {
        // 1. Load test (respuestas already shuffled by backend)
        const testRes = await apiFetch(`${apiBase}/api/generales/test/${testIdNum}`);
        const testData = (await testRes.json()) as GeneralTestDTO;
        setTest(testData);
        
        // Load activity configuration
        setActivityConfig({
          showScore: testData.mostrarPuntuacion ?? true,
          allowRetry: testData.permitirReintento ?? false,
          showCorrectAnswer: testData.encontrarRespuestaMaestro ?? true,
          showStudentAnswer: testData.encontrarRespuestaAlumno ?? true,
        });

        // 2. Resolve ActividadAlumno
        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) {
          throw new TypeError('No se pudo identificar al alumno. Inicia sesión de nuevo.');
        }

        let hasExisting = false;
        try {
          const getAA = await apiFetch(
            `${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${testData.id}`,
          );
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            hasExisting = true;
            setActividadAlumnoId(aaData.id);
            if (isCompletedAttempt(aaData.fechaFin)) {
              completedRef.current = true;
              setSubmitted(true);
              setLastAttemptScore(aaData.puntuacion ?? 0);
              const histRes = await apiFetch(`${apiBase}/api/respuestas-alumno-general/actividad-alumno/${aaData.id}`);
              const histData = (await histRes.json()) as RespAlumnoGeneralResumenDTO[];
              const map = new Map<number, string[]>();
              const correctMap = new Map<number, string>();
              for (const item of histData) {
                if (typeof item.preguntaId !== 'number') continue;
                const current = map.get(item.preguntaId) ?? [];
                current.push(item.respuesta);
                map.set(item.preguntaId, current);
                if (item.respuestaCorrecta && !correctMap.has(item.preguntaId)) {
                  correctMap.set(item.preguntaId, item.respuestaCorrecta);
                }
              }
              setSubmittedAnswersByQuestion(map);
              setSubmittedCorrectByQuestion(correctMap);
            }
          }
        } catch {
          hasExisting = false;
        }

        if (!hasExisting) {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: testData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new TypeError('Respuesta inválida al crear ActividadAlumno');
          }
        }
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'Error cargando el test';
        setError(msg);
      } finally {
        setLoading(false);
        initInFlightRef.current = false;
      }
    };

    run();
  }, [testId, testIdNum, apiBase]);

  // ── Derived state ─────────────────────────────────────────────────────────

  const allAnswered = useMemo(() => {
    if (!test) return false;
    return test.preguntas.every((p) => {
      const selected = selections.get(p.id);
      return selected != null && selected.size > 0;
    });
  }, [test, selections]);

  const totalPreguntas = test?.preguntas.length ?? 0;
  const isLastQuestion = currentIndex === totalPreguntas - 1;
  const isFirstQuestion = currentIndex === 0;
  // Progress: 0% on question 1, 100% on last question
  const progressPct = totalPreguntas <= 1 ? 100 : (currentIndex / (totalPreguntas - 1)) * 100;
  const dragonReached = currentIndex === totalPreguntas - 1 && totalPreguntas > 1;

  // ── Handlers ──────────────────────────────────────────────────────────────

  const handleSelect = (preguntaId: number, respuestaId: number) => {
    if (submitted) return;
    setSelections((prev) => {
      const next = new Map(prev);
      const current = new Set(next.get(preguntaId) ?? []);
      if (current.has(respuestaId)) {
        current.delete(respuestaId);
      } else {
        current.add(respuestaId);
      }
      next.set(preguntaId, current);
      return next;
    });
  };

  const handleNext = () => {
    if (currentIndex < totalPreguntas - 1) setCurrentIndex((i) => i + 1);
  };

  const handlePrev = () => {
    if (currentIndex > 0) setCurrentIndex((i) => i - 1);
  };

  const handleSubmit = async () => {
    if (!test || !actividadAlumnoId || !allAnswered) return;
    setError('');
    setSubmitting(true);
    setLastAttemptScore(null);

    try {
      const alumnoId = getCurrentUserIdFromJwt();
      if (!alumnoId) {
        throw new TypeError('No se pudo identificar al alumno. Inicia sesión de nuevo.');
      }

      // Reasegura el intento activo justo antes de enviar para evitar usar
      // un intento antiguo/terminado en escenarios de reintento.
      const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
        method: 'POST',
        body: JSON.stringify({ alumnoId, actividadId: test.id }),
      });
      const ensuredAA = (await createAA.json()) as ActividadAlumnoDTO;
      if (typeof ensuredAA?.id !== 'number' || !Number.isFinite(ensuredAA.id)) {
        throw new TypeError('No se pudo preparar el intento de la actividad.');
      }
      const actividadAlumnoIdActual = ensuredAA.id;
      setActividadAlumnoId(actividadAlumnoIdActual);

      const respuestasIds: number[] = [];
      const resultEntries = await Promise.all(
        test.preguntas.map(async (p) => {
          const selectedIds = Array.from(selections.get(p.id) ?? []);

          const optionResponses = await Promise.all(
            selectedIds.map(async (selectedId) => {
              const res = await apiFetch(`${apiBase}/api/respuestas-alumno-general`, {
                method: 'POST',
                body: JSON.stringify({
                  actividadAlumnoId: actividadAlumnoIdActual,
                  preguntaId: p.id,
                  respuestaId: selectedId,
                }),
              });
              const data = (await res.json()) as RespAlumnoGeneralCreateResponse;
              return { selectedId, data };
            }),
          );

          const selectedCorrectById = new Map<number, boolean>();
          let comentario = '';
          let selectedCorrectCount = 0;

          for (const { selectedId, data } of optionResponses) {
            if (data.id) respuestasIds.push(data.id);
            const optionCorrect = Boolean(data?.correcta);
            selectedCorrectById.set(selectedId, optionCorrect);
            if (optionCorrect) selectedCorrectCount++;
            if (!comentario && data?.comentario) comentario = data.comentario;
          }

          const numCorrectasEsperadas = Math.max(1, Number(p.numRespuestasCorrectas ?? 1));
          const correctaPregunta =
            selectedIds.length > 0 &&
            selectedCorrectCount === selectedIds.length &&
            selectedIds.length === numCorrectasEsperadas;

          return [
            p.id,
            {
              correcta: correctaPregunta,
              comentario,
              selectedIds,
              selectedCorrectById,
            },
          ] as [number, QuestionResult];
        }),
      );

      if (respuestasIds.length > 0) {
        const corregirRes = await apiFetch(`${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoIdActual}`, {
          method: 'PUT',
          body: JSON.stringify(respuestasIds),
        });
        const corregido = (await corregirRes.json()) as ActividadAlumnoDTO;
        if (typeof corregido?.puntuacion === 'number') {
          setLastAttemptScore(corregido.puntuacion);
        }

        const histRes = await apiFetch(`${apiBase}/api/respuestas-alumno-general/actividad-alumno/${actividadAlumnoIdActual}`);
        const histData = (await histRes.json()) as RespAlumnoGeneralResumenDTO[];
        const map = new Map<number, string[]>();
        const correctMap = new Map<number, string>();
        for (const item of histData) {
          if (typeof item.preguntaId !== 'number') continue;
          const current = map.get(item.preguntaId) ?? [];
          current.push(item.respuesta);
          map.set(item.preguntaId, current);
          if (item.respuestaCorrecta && !correctMap.has(item.preguntaId)) {
            correctMap.set(item.preguntaId, item.respuestaCorrecta);
          }
        }
        setSubmittedAnswersByQuestion(map);
        setSubmittedCorrectByQuestion(correctMap);
      }

      

      completedRef.current = true;
      const resultsMap = new Map<number, QuestionResult>(resultEntries);
      setResults(resultsMap);
      setSubmitted(true);
      // Reset to first question so user can review from the beginning
      setCurrentIndex(0);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error enviando las respuestas';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleRetry = () => {
    // Reset all state to allow retrying
    setSelections(new Map());
    setResults(new Map());
    setSubmitted(false);
    setLastAttemptScore(null);
    setSubmittedAnswersByQuestion(new Map());
    setSubmittedCorrectByQuestion(new Map());
    setCurrentIndex(0);
    setError('');
  };

  const handleViewStudentAnswers = () => {
    setAnswerModalMode('student');
    setShowAnswerModal(true);
  };

  const handleViewCorrectAnswers = () => {
    setAnswerModalMode('correct');
    setShowAnswerModal(true);
  };

  // ── Score summary ─────────────────────────────────────────────────────────

  const score = useMemo(() => {
    if (!submitted || results.size === 0) return null;
    const total = results.size;
    const correct = [...results.values()].filter((r) => r.correcta).length;
    return { correct, total };
  }, [submitted, results]);

  const scorePoints = useMemo(() => {
    if (typeof lastAttemptScore === 'number') return lastAttemptScore;
    if (!score || !test || score.total === 0) return 0;
    return Math.round((score.correct / score.total) * test.puntuacion);
  }, [lastAttemptScore, score, test]);

  // ── Render ────────────────────────────────────────────────────────────────

  if (loading) {
    return (
      <div className="test-alumno-page">
        <NavbarMisCursos />
        <main className="test-alumno-main">
          <p className="ca-text">Cargando...</p>
        </main>
      </div>
    );
  }

  const currentPregunta = test?.preguntas[currentIndex];

  let lastQuestionButton: React.ReactNode;
  if (submitted) {
    lastQuestionButton = (
      <button
        type="button"
        className="ca-btn-guardar"
        onClick={() => navigate(-1)}
      >
        Volver al curso
      </button>
    );
  } else {
    lastQuestionButton = (
      <button
        type="button"
        className="ta-nav-btn ta-nav-btn--submit"
        onClick={handleSubmit}
        disabled={submitting || !allAnswered || !actividadAlumnoId}
        title={
          allAnswered
            ? ''
            : 'Responde todas las preguntas antes de enviar'
        }
      >
        {submitting ? 'Enviando...' : '¡Enviar respuestas!'}
      </button>
    );
  }

  let answeredBadgeText: string | null = null;
  if (currentPregunta && (selections.get(currentPregunta.id)?.size ?? 0) > 0) {
    if (submitted) {
      answeredBadgeText = results.get(currentPregunta.id)?.correcta ? '✓ Correcta' : '✗ Incorrecta';
    } else {
      answeredBadgeText = '✓ Respondida';
    }
  }

  return (
    <div className="test-alumno-page">
      <NavbarMisCursos />

      <main className="test-alumno-main">
        {error && (
          <p className="ca-text" style={{ marginTop: 0, color: '#c0392b' }}>
            {error}
          </p>
        )}

        {test && currentPregunta && (
          <>
            {/* ── Header ── */}
            <ActivityHeader title={test.titulo} guideType="test" guideRole="alumno" />

            {/* ── Score banner after submit ── */}
            {submitted && score && (
              <div
                className={`ta-score-banner ${
                  score.correct === score.total ? 'ta-score-banner--perfect' : ''
                }`}
              >
                {score.correct === score.total
                  ? `¡Perfecto! ${score.correct} / ${score.total} correctas 🏆`
                  : `${score.correct} / ${score.total} correctas`}
              </div>
            )}

            {/* ── Battle progress bar ── */}
            <div className="ta-battle-bar">
              <img 
    src={caballeroImg} 
    alt="Caballero" 
    className="ta-knight-img" 
    title="Caballero" 
  />
              <div className="ta-progress-track">
                <div
                  className="ta-progress-fill"
                  style={{ width: `${progressPct}%` }}
                />
                {!submitted && progressPct > 0 && (
                  <div
                    className="ta-spell-head"
                    style={{ left: `calc(${progressPct}% - 10px)` }}
                  />
                )}
              </div>
              <img 
    src={dragonImg} 
    alt="Dragón" 
    className={`ta-dragon-img ${dragonReached ? ' ta-dragon--shaking' : ''}`}
  />
            </div>

            {/* ── Question counter ── */}
            <div className="ta-question-counter">
              <span className="ta-question-counter-text">
                Pregunta {currentIndex + 1} de {totalPreguntas}
              </span>
              {(selections.get(currentPregunta.id)?.size ?? 0) > 0 && (
                <span className="ta-answered-badge">
                  {answeredBadgeText}
                </span>
              )}
            </div>

            {/* ── Current question card ── */}
            <div className="ta-question-card">
              <p className="ta-question-text">{currentPregunta.pregunta}</p>

              {currentPregunta.imagen && (
                <img
                  src={currentPregunta.imagen}
                  alt={`Pregunta ${currentIndex + 1}`}
                  className="ta-question-img"
                />
              )}

              <div className="ta-options">
                {currentPregunta.respuestas.map((r, oi) => {
                  const selectedIds = selections.get(currentPregunta.id) ?? new Set<number>();
                  const isSelected = selectedIds.has(r.id);
                  const result = results.get(currentPregunta.id);

                  let optClass = 'ta-option';
                  if (submitted && result) {
                    if (isSelected) {
                      optClass += result.correcta ? ' ta-option--correct' : ' ta-option--wrong';
                    }
                  } else if (isSelected) {
                    optClass += ' ta-option--selected';
                  }

                  return (
                    <button
                      key={r.id}
                      type="button"
                      className={optClass}
                      onClick={() => handleSelect(currentPregunta.id, r.id)}
                      disabled={submitted}
                    >
                      <span className="ta-option-letter">{String.fromCharCode(65 + oi)}.</span>
                      <span className="ta-option-text">{r.respuesta}</span>
                      {submitted && isSelected && (
                        <span className="ta-option-icon">{result?.selectedCorrectById.get(r.id) ? '✓' : '✗'}</span>
                      )}
                    </button>
                  );
                })}
              </div>

              {submitted &&
                results.get(currentPregunta.id) &&
                test.respVisible &&
                results.get(currentPregunta.id)!.comentario && (
                  <div className="ta-feedback-comment">
                    💬 {results.get(currentPregunta.id)!.comentario}
                  </div>
                )}
            </div>

            {/* ── Navigation buttons ── */}
            <div className="ta-nav-buttons">
              <button
                type="button"
                className="ta-nav-btn ta-nav-btn--prev"
                onClick={handlePrev}
                disabled={isFirstQuestion}
              >
                ← Anterior
              </button>

              {isLastQuestion ? (
                lastQuestionButton
              ) : (
                <button
                  type="button"
                  className="ca-btn-guardar"
                  onClick={handleNext}
                >
                  Siguiente →
                </button>
              )}
            </div>

            {/* Hint when on last question but not all answered */}
            {!submitted && !allAnswered && isLastQuestion && (
              <><p className="test-hint">
                Responde todas las preguntas para enviar.</p><p className="test-hint">
                  Usa ← Anterior para revisar.
                </p></>
            )}
          </>
        )}

        {!test && !error && <p className="ca-text">No se encontró el test.</p>}

        {submitted && activityConfig ? (
          <ActivityResultScreen
            title="¡TEST COMPLETADO!"
            score={scorePoints}
            maxScore={test?.puntuacion || 100}
            config={activityConfig}
            onContinue={() => navigate(-1)}
            onRetry={handleRetry}
            onViewStudentAnswer={handleViewStudentAnswers}
            onViewCorrectAnswer={handleViewCorrectAnswers}
            onCancel={() => navigate(-1)}
          />
        ) : submitted ? (
          <CompletionPopup title="¡TEST COMPLETADO!" onContinue={() => navigate(-1)} />
        ) : null}

        {showAnswerModal && (
          <AnswerViewModal
            title={answerModalMode === 'student' ? 'Mi respuesta' : 'Respuesta correcta'}
            answers={test ? test.preguntas.map((pregunta) => {
              const result = results.get(pregunta.id);
              const selectedIds = selections.get(pregunta.id) || new Set();
              const selectedAnswersText = pregunta.respuestas
                .filter(r => selectedIds.has(r.id))
                .map(r => r.respuesta)
                .join(', ');
              return {
                question: pregunta.pregunta,
                studentAnswer:
                  (submittedAnswersByQuestion.get(pregunta.id)?.join(', ') || selectedAnswersText) ||
                  '(No respondida)',
                correctAnswer:
                  submittedCorrectByQuestion.get(pregunta.id) ||
                  pregunta.respuestas.filter((r) => r.correcta).map((r) => r.respuesta).join(', ') ||
                  '(No disponible)',
                isCorrect: result?.correcta,
              };
            }) : []}
            onClose={() => setShowAnswerModal(false)}
            mode={answerModalMode}
          />
        )}
      </main>
    </div>
  );
}
