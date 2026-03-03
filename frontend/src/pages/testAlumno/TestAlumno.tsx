import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './TestAlumno.css';

// ── Types ─────────────────────────────────────────────────────────────────

type RespuestaDTO = {
  readonly id: number;
  readonly respuesta: string;
};

type PreguntaDTO = {
  readonly id: number;
  readonly pregunta: string;
  readonly imagen: string | null;
  readonly respuestas: RespuestaDTO[];
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
};

type RespAlumnoGeneralCreateResponse = {
  readonly correcta: boolean;
  readonly comentario: string;
};

type QuestionResult = {
  readonly correcta: boolean;
  readonly comentario: string;
  readonly selectedText: string;
};

type ActividadAlumnoDTO = { readonly id: number };

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
  // preguntaId → selected respuesta ID (not text, to avoid duplicate-text false-matches)
  const [selections, setSelections] = useState<Map<number, number>>(new Map());
  const [results, setResults] = useState<Map<number, QuestionResult>>(new Map());
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const testIdNum = useMemo(() => {
    if (!testId) return NaN;
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
      apiFetch(`/api/actividades-alumno/${id}/abandon`, { method: 'POST' }).catch(() => {});
    };
  }, []);

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
        const testRes = await apiFetch(`/api/generales/test/${testIdNum}`);
        const testData = (await testRes.json()) as GeneralTestDTO;
        setTest(testData);

        // 2. Resolve ActividadAlumno
        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) {
          throw new Error('No se pudo identificar al alumno. Inicia sesión de nuevo.');
        }

        const ensureRes = await apiFetch(`/api/actividades-alumno/ensure/${testData.id}`);
        const ensureValue = (await ensureRes.json()) as unknown;
        const exists = ensureValue === 1 || ensureValue === '1' || ensureValue === true;

        if (!exists) {
          const createAA = await apiFetch('/api/actividades-alumno', {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: testData.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new Error('Respuesta inválida al crear ActividadAlumno');
          }
        } else {
          const getAA = await apiFetch(
            `/api/actividades-alumno/alumno/${alumnoId}/actividad/${testData.id}`,
          );
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new Error('Respuesta inválida al obtener ActividadAlumno');
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
  }, [testId, testIdNum]);

  // ── Derived state ─────────────────────────────────────────────────────────

  const allAnswered = useMemo(() => {
    if (!test) return false;
    return test.preguntas.every((p) => selections.has(p.id));
  }, [test, selections]);

  // ── Handlers ──────────────────────────────────────────────────────────────

  const handleSelect = (preguntaId: number, respuestaId: number) => {
    if (submitted) return;
    setSelections((prev) => new Map(prev).set(preguntaId, respuestaId));
  };

  const handleSubmit = async () => {
    if (!test || !actividadAlumnoId || !allAnswered) return;
    setError('');
    setSubmitting(true);

    try {
      const resultEntries = await Promise.all(
        test.preguntas.map(async (p) => {
          const selectedId = selections.get(p.id)!;
          const selectedText = p.respuestas.find((r) => r.id === selectedId)?.respuesta ?? '';
          const res = await apiFetch('/api/respuestas-alumno-general', {
            method: 'POST',
            body: JSON.stringify({
              actividadAlumnoId,
              preguntaId: p.id,
              respuestaId: selectedId,
            }),
          });
          const data = (await res.json()) as RespAlumnoGeneralCreateResponse;
          const result: QuestionResult = {
            correcta: Boolean(data?.correcta),
            comentario: typeof data?.comentario === 'string' ? data.comentario : '',
            selectedText,
          };
          return [p.id, result] as [number, QuestionResult];
        }),
      );

      completedRef.current = true;
      const resultsMap = new Map<number, QuestionResult>(resultEntries);
      setResults(resultsMap);
      setSubmitted(true);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error enviando las respuestas';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  // ── Score summary ─────────────────────────────────────────────────────────

  const score = useMemo(() => {
    if (!submitted || results.size === 0) return null;
    const total = results.size;
    const correct = [...results.values()].filter((r) => r.correcta).length;
    return { correct, total };
  }, [submitted, results]);

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

  return (
    <div className="test-alumno-page">
      <NavbarMisCursos />

      <main className="test-alumno-main">
        {error && (
          <p className="ca-text" style={{ marginTop: 0, color: '#c0392b' }}>
            {error}
          </p>
        )}

        {test && (
          <>
            {/* ── Header ── */}
            <div className="ta-top">
              <button className="ta-exit-btn" type="button" onClick={() => navigate(-1)}>
                Salir del curso
              </button>
              <div className="ta-title-banner">
                <h1 className="ta-title">{test.titulo}</h1>
              </div>
            </div>

            {test.descripcion && <p className="ta-description">{test.descripcion}</p>}

            {test.imagen && (
              <div className="ta-activity-img-wrap">
                <img src={test.imagen} alt={test.titulo} className="ta-activity-img" />
              </div>
            )}

            {/* ── Score banner after submit ── */}
            {submitted && score && (
              <div className={`ta-score-banner ${score.correct === score.total ? 'ta-score-banner--perfect' : ''}`}>
                {score.correct === score.total
                  ? `¡Perfecto! ${score.correct} / ${score.total} correctas`
                  : `${score.correct} / ${score.total} correctas`}
              </div>
            )}

            {/* ── Questions ── */}
            <div className="ta-questions">
              {test.preguntas.map((pregunta, qi) => {
                const result = results.get(pregunta.id);
                const selectedId = selections.get(pregunta.id);

                return (
                  <div key={pregunta.id} className="ta-question-card">
                    <div className="ta-question-num">Pregunta {qi + 1}</div>
                    <p className="ta-question-text">{pregunta.pregunta}</p>

                    {pregunta.imagen && (
                      <img
                        src={pregunta.imagen}
                        alt={`Pregunta ${qi + 1}`}
                        className="ta-question-img"
                      />
                    )}

                    <div className="ta-options">
                      {pregunta.respuestas.map((r, oi) => {
                        const isSelected = selectedId === r.id;
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
                            onClick={() => handleSelect(pregunta.id, r.id)}
                            disabled={submitted}
                          >
                            <span className="ta-option-letter">{String.fromCharCode(65 + oi)}.</span>
                            <span className="ta-option-text">{r.respuesta}</span>
                            {submitted && isSelected && (
                              <span className="ta-option-icon">
                                {result?.correcta ? '✓' : '✗'}
                              </span>
                            )}
                          </button>
                        );
                      })}
                    </div>

                    {submitted && result && test.respVisible && result.comentario && (
                      <div className="ta-feedback-comment">{result.comentario}</div>
                    )}
                  </div>
                );
              })}
            </div>

            {/* ── Submit / Done bar ── */}
            <div className="ta-bottom">
              {!submitted ? (
                <button
                  className="ca-btn-guardar"
                  type="button"
                  disabled={submitting || !allAnswered || !actividadAlumnoId}
                  onClick={handleSubmit}
                >
                  {submitting ? 'Enviando...' : 'Enviar respuestas'}
                </button>
              ) : (
                <button
                  className="ca-btn-guardar"
                  type="button"
                  onClick={() => navigate(-1)}
                >
                  Volver
                </button>
              )}
              {!allAnswered && !submitted && (
                <p className="ta-hint">Responde todas las preguntas para poder enviar.</p>
              )}
            </div>
          </>
        )}

        {!test && !error && <p className="ca-text">No se encontró el test.</p>}
      </main>
    </div>
  );
}
