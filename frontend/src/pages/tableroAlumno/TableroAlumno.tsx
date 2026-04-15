import { useCallback, useEffect, useRef, useState, type ReactElement } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import hombreMisteriosoImg from '../../assets/props/hombreMisterioso.png';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import ActivityResultScreen, { type ActivityResultConfig } from '../../components/ActivityResultScreen/ActivityResultScreen';
import AnswerViewModal from '../../components/AnswerViewModal/AnswerViewModal';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { getCurrentUserInfo } from '../../types/curso';
import { apiFetch } from '../../utils/api';
import './TableroAlumno.css';

import niv3 from '../../assets/props/Cerbero_despierto_niv3.png';
import niv2 from '../../assets/props/Guardian_de_la_primera_cabeza_niv2.png';
import niv1 from '../../assets/props/perritoCerberito.png';
import niv5 from '../../assets/props/Rey_de_cerebrus_niv5.png';
import niv4 from '../../assets/props/Vigia_de_las_tres_mentes_niv4.png';

// ── Types ─────────────────────────────────────────────────

type PreguntaDTO = { id: number; pregunta: string };

type TableroAlumnoDTO = {
  id: number;
  titulo: string;
  descripcion?: string | null;
  tamano: boolean; // true = 3×3, false = 4×4
  respVisible: boolean;
  puntuacion?: number;
  permitirReintento?: boolean;
  mostrarPuntuacion?: boolean;
  encontrarRespuestaMaestro?: boolean;
  encontrarRespuestaAlumno?: boolean;
  preguntas: PreguntaDTO[];
};

type ActividadAlumnoDTO = {
  id: number;
  puntuacion?: number | null;
  fechaFin?: string | null;
};

type RespAlumnoGeneralResumenDTO = {
  readonly preguntaId: number | null;
  readonly respuesta: string;
  readonly correcta?: boolean | null;
  readonly respuestaCorrecta?: string | null;
  readonly numFallos?: number | null;
};

// ── Helpers ───────────────────────────────────────────────

function getMascotaEvolucionada(puntos: number): string {
  if (puntos <= 100) return niv1;
  if (puntos <= 300) return niv2;
  if (puntos <= 600) return niv3;
  if (puntos <= 1000) return niv4;
  return niv5;
}


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

function cellToQIndex(r: number, c: number, size: number): number | null {
  if (r === 0 && c === 0) return null;
  return r * size + c - 1;
}

function isNeighbor(r1: number, c1: number, r2: number, c2: number): boolean {
  return Math.abs(r1 - r2) + Math.abs(c1 - c2) === 1;
}

function cx(...classes: (string | false | undefined | null)[]): string {
  return classes.filter(Boolean).join(' ');
}

const DOTS_3x3 = 4;
const DOTS_4x4 = 5;

const API_BASE = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

// ── Sub-components ────────────────────────────────────────

function StatusMessage({ error }: { error?: string }) {
  return (
    <div className="ta-page">
      <NavbarMisCursos />
      <main className="ta-main">
        <p className={cx('ta-info-msg', error && 'ta-info-msg--err')}>
          {error ?? 'Cargando tablero...'}
        </p>
      </main>
    </div>
  );
}

function QuestionModal({
  question,
  inputAnswer,
  feedback,
  submitting,
  onInputChange,
  onSubmit,
  onClose,
}: {
  question: PreguntaDTO;
  inputAnswer: string;
  feedback: { correct: boolean; msg: string } | null;
  submitting: boolean;
  onInputChange: (v: string) => void;
  onSubmit: () => void;
  onClose: () => void;
}) {
  return (
    <div className="ta-modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ta-modal">
        <h3 className="ta-modal-title">Pregunta</h3>
        <p className="ta-modal-q">{question.pregunta}</p>
        <label className="ta-modal-label">Solución</label>
        <input
          className="ta-modal-input"
          type="text"
          value={inputAnswer}
          onChange={(e) => onInputChange(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && !submitting && onSubmit()}
          maxLength={200}
          autoFocus
        />
        {feedback && (
          <p className={cx('ta-modal-feedback', feedback.correct ? 'ta-modal-feedback--ok' : 'ta-modal-feedback--err')}>
            {feedback.correct ? '¡Correcto! ✓' : feedback.msg}
          </p>
        )}
        <button
          className="ta-accent-btn"
          onClick={onSubmit}
          disabled={submitting || !inputAnswer.trim()}
        >
          {submitting ? '…' : 'Enviar'}
        </button>
      </div>
    </div>
  );
}

// ── Grid builder ──────────────────────────────────────────

function buildGridItems(
  size: number,
  is3x3: boolean,
  cerberoPos: [number, number],
  answeredSet: Set<number>,
  isComplete: boolean,
  onCellClick: (r: number, c: number) => void,
  puntosAlumno: number,
): ReactElement[] {
  const [cr, cc] = cerberoPos;
  const hDots = is3x3 ? DOTS_3x3 : DOTS_4x4;
  const items: ReactElement[] = [];

  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      const isCerbero = r === cr && c === cc;
      const qIdx = cellToQIndex(r, c, size);
      const isAnswered = qIdx !== null && answeredSet.has(qIdx);
      const isClickable = qIdx !== null && isNeighbor(cr, cc, r, c) && !isComplete;
      const isDark = (r + c) % 2 === 1;

      const cellVariant =
        isCerbero || (r === 0 && c === 0) || isAnswered
          ? 'ta-cell--cerbero'
          : isDark ? 'ta-cell--dark' : 'ta-cell--light';

      items.push(
        <div
          key={`cell-${r}-${c}`}
          className={cx('ta-cell', cellVariant, isClickable && 'ta-cell--clickable', !isCerbero && isAnswered && 'ta-cell--done')}
          style={{ gridRow: r * 2 + 1, gridColumn: c * 2 + 1 }}
          onClick={() => onCellClick(r, c)}
          role={isClickable ? 'button' : undefined}
          tabIndex={isClickable ? 0 : undefined}
          onKeyDown={(e) => e.key === 'Enter' && onCellClick(r, c)}
        >
          {isCerbero && (
            <img src={getMascotaEvolucionada(puntosAlumno)} className={cx('ta-char', isComplete && 'ta-char--victory')} alt="Cerbero" aria-hidden="true"/>
          )}
          {qIdx !== null && !isCerbero && (
            <span className={cx('ta-cell-label', isDark ? 'ta-cell-label--light' : 'ta-cell-label--dark')}>
              {isAnswered ? '✓' : '?'}
            </span>
          )}
        </div>,
      );

      if (c < size - 1) {
        items.push(
          <div key={`h-${r}-${c}`} className="ta-connector ta-connector--h" style={{ gridRow: r * 2 + 1, gridColumn: c * 2 + 2 }}>
            {Array.from({ length: hDots }, (_, i) => <span key={i} className="ta-dot" />)}
          </div>,
        );
      }

      if (r < size - 1) {
        items.push(
          <div key={`v-${r}-${c}`} className="ta-connector ta-connector--v" style={{ gridRow: r * 2 + 2, gridColumn: c * 2 + 1 }} />,
        );
      }
    }
  }

  return items;
}

// ── Main component ────────────────────────────────────────

export default function TableroAlumno() {
  const { tableroId } = useParams<{ tableroId: string }>();
  const navigate = useNavigate();

  // ── Refs para controlar el tiempo y estado ──
  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const [tablero, setTablero] = useState<TableroAlumnoDTO | null>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [lastAttemptScore, setLastAttemptScore] = useState<number | null>(null);
  const [activityConfig, setActivityConfig] = useState<ActivityResultConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [cerberoPos, setCerberoPos] = useState<[number, number]>([0, 0]);
  const [answeredSet, setAnsweredSet] = useState<Set<number>>(new Set());

  const [modalCell, setModalCell] = useState<[number, number] | null>(null);
  const [inputAnswer, setInputAnswer] = useState('');
  const [feedback, setFeedback] = useState<{ correct: boolean; msg: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [puntosAlumno, setPuntosAlumno] = useState(0);
  const [attemptCompleted, setAttemptCompleted] = useState(false);
  const [showAnswerModal, setShowAnswerModal] = useState(false);
  const [answerModalMode, setAnswerModalMode] = useState<'student' | 'correct'>('student');
  const [answerHistory, setAnswerHistory] = useState<Map<number, string>>(new Map());
  const [submittedAnswersByQuestion, setSubmittedAnswersByQuestion] = useState<Map<number, string>>(new Map());
  const [correctAnswersByQuestion, setCorrectAnswersByQuestion] = useState<Map<number, string>>(new Map());
  const [boardFailureCount, setBoardFailureCount] = useState(0);

  const hydrateAnswersFromHistory = useCallback(async (actividadAlumnoIdValue: number) => {
    const histRes = await apiFetch(`${API_BASE}/api/respuestas-alumno-general/actividad-alumno/${actividadAlumnoIdValue}`);
    const histData = (await histRes.json()) as RespAlumnoGeneralResumenDTO[];
    const studentMap = new Map<number, string>();
    const correctMap = new Map<number, string>();
    let failureCount = 0;

    for (const item of histData) {
      if (typeof item.preguntaId !== 'number') continue;
      studentMap.set(item.preguntaId, item.respuesta);
      if (item.respuestaCorrecta && !correctMap.has(item.preguntaId)) {
        correctMap.set(item.preguntaId, item.respuestaCorrecta);
      }
      failureCount += Number(item.numFallos ?? 0);
    }

    setSubmittedAnswersByQuestion(studentMap);
    setCorrectAnswersByQuestion(correctMap);
    setBoardFailureCount(failureCount);
  }, []);

  // Sincronizar el ID para el cleanup del abandono
  useEffect(() => {
    actividadAlumnoIdRef.current = actividadAlumnoId;
  }, [actividadAlumnoId]);

  // Si se va sin terminar, registrar abandono
  useEffect(() => {
    return () => {
      const id = actividadAlumnoIdRef.current;
      if (!id || completedRef.current || abandonReportedRef.current) return;
      abandonReportedRef.current = true;
      apiFetch(`${API_BASE}/api/actividades-alumno/${id}/abandon`, { method: 'POST' }).catch(() => {});
    };
  }, []);

  // Carga inicial y registro del inicio del cronómetro
  useEffect(() => {
    const run = async () => {
      if (initInFlightRef.current) return;
      initInFlightRef.current = true;
      if (!tableroId) return;

      try {
        const res = await apiFetch(`${API_BASE}/api/tableros/${tableroId}`);
        const data = (await res.json()) as TableroAlumnoDTO;
        setTablero(data);
        setActivityConfig({
          showScore: data.mostrarPuntuacion ?? true,
          allowRetry: data.permitirReintento ?? false,
          showCorrectAnswer: data.encontrarRespuestaMaestro ?? true,
          showStudentAnswer: data.encontrarRespuestaAlumno ?? true,
        });

        // Registro de la actividad del alumno (¡Empieza el tiempo!)
        const alumnoId = getCurrentUserIdFromJwt();
        if (alumnoId) {
                try {
                  const resAl = await apiFetch(`${API_BASE}/api/alumnos/mi-puntuacion-total`);
                  console.log('[MapaCurso] Respuesta de puntos:', resAl);
                  if (resAl.ok) {
                    const dataAl = await resAl.json();
                    // Asegúrate de que el backend devuelve un objeto con la propiedad 'puntos'
                    setPuntosAlumno(dataAl || 0);
                  }
                } catch (e) {
                  // Si falla, solo avisamos por consola pero seguimos adelante
                  console.warn('[MapaCurso] No se pudo cargar la evolución, usando nivel inicial.', e);
                  setPuntosAlumno(0);
                }
              }
        if (alumnoId) {
          let hasExisting = false;
          try {
            const getAA = await apiFetch(`${API_BASE}/api/actividades-alumno/alumno/${alumnoId}/actividad/${data.id}`);
            const aaData = (await getAA.json()) as ActividadAlumnoDTO;
            if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
              hasExisting = true;
              setActividadAlumnoId(aaData.id);
              if (isCompletedAttempt(aaData.fechaFin)) {
                completedRef.current = true;
                setAttemptCompleted(true);
                setLastAttemptScore(aaData.puntuacion ?? 0);
                try {
                  await hydrateAnswersFromHistory(aaData.id);
                } catch {
                  setSubmittedAnswersByQuestion(new Map());
                  setCorrectAnswersByQuestion(new Map());
                  setBoardFailureCount(0);
                }
              }
            }
          } catch {
            hasExisting = false;
          }

          if (!hasExisting) {
            const createAA = await apiFetch(`${API_BASE}/api/actividades-alumno`, {
              method: 'POST',
              body: JSON.stringify({ alumnoId, actividadId: data.id }),
            });
            const aaData = (await createAA.json()) as ActividadAlumnoDTO;
            setActividadAlumnoId(aaData.id);
          }
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Error al cargar el tablero');
      } finally {
        setLoading(false);
        initInFlightRef.current = false;
      }
    };
    run();
  }, [tableroId]);

  const closeModal = useCallback(() => {
    if (submitting) return;
    setModalCell(null);
    setFeedback(null);
    setInputAnswer('');
  }, [submitting]);

  // Variables de finalización
  const size = tablero?.tamano ? 3 : 4;
  const completionTarget = tablero?.tamano ? 8 : 15;
  const isComplete = answeredSet.size >= completionTarget;
  const showCompleted = attemptCompleted || isComplete;

  // Registrar el final de la actividad (¡Termina el tiempo!)
  useEffect(() => {
    if (isComplete && !completedRef.current && actividadAlumnoId) {
      completedRef.current = true;
      apiFetch(`${API_BASE}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`, {
        method: 'PUT',
        body: JSON.stringify([]) // Tablero no usa Ids aquí, pero llama al endpoint para sellar la fechaFin
      })
        .then(async (r) => {
          const maybeAA = (await r.json()) as ActividadAlumnoDTO;
          if (typeof maybeAA?.puntuacion === 'number') {
            setLastAttemptScore(maybeAA.puntuacion);
          }
          if (typeof maybeAA?.id === 'number') {
            try {
              await hydrateAnswersFromHistory(maybeAA.id);
            } catch {
              // Si falla, el modal seguirá mostrando el historial local del alumno.
            }
          }
        })
        .catch(() => {});
    }
  }, [isComplete, actividadAlumnoId, hydrateAnswersFromHistory]);


  if (loading) return <StatusMessage />;
  if (error || !tablero) return <StatusMessage error={error || 'No se encontró el tablero'} />;

  // ── Handlers ──────────────────────────────────────────

  const handleCellClick = (r: number, c: number) => {
    if (showCompleted) return;
    const qIdx = cellToQIndex(r, c, size);
    if (qIdx === null) return;
    if (!isNeighbor(cerberoPos[0], cerberoPos[1], r, c)) return;

    if (answeredSet.has(qIdx)) {
      setCerberoPos([r, c]);
      return;
    }

    setModalCell([r, c]);
    setInputAnswer('');
    setFeedback(null);
  };

  const handleSubmit = async () => {
    if (!modalCell || !tablero || submitting) return;
    const [mr, mc] = modalCell;
    const qIdx = cellToQIndex(mr, mc, size)!;
    const q = tablero.preguntas[qIdx];
    setSubmitting(true);
    try {
      const res = await apiFetch(`${API_BASE}/api/tableros/${tablero.id}/${q.id}`, {
        method: 'POST',
        body: JSON.stringify(inputAnswer.trim()),
      });
      const msg = await res.text();
      const correct = msg.toLowerCase().includes('correcta') && !msg.toLowerCase().includes('incorrecta');
      setFeedback({ correct, msg });
      setAnswerHistory((prev) => {
        const next = new Map(prev);
        next.set(q.id, inputAnswer.trim());
        return next;
      });
      if (correct) {
        setAnsweredSet((prev) => new Set(prev).add(qIdx));
        setCerberoPos([mr, mc]);
        setTimeout(closeModal, 600);
      }
    } catch {
      setFeedback({ correct: false, msg: 'Error al enviar la respuesta' });
    } finally {
      setSubmitting(false);
    }
  };

  // ── Grid ──────────────────────────────────────────────

  const gridDim = size * 2 - 1;
  const gridItems = buildGridItems(size, tablero.tamano, cerberoPos, answeredSet, isComplete, handleCellClick, puntosAlumno);

  const modalQIdx = modalCell ? cellToQIndex(modalCell[0], modalCell[1], size) : null;
  const modalQuestion = modalQIdx !== null ? tablero.preguntas[modalQIdx] : null;

  const handleRetry = () => {
    if (!tablero) return;

    void (async () => {
      try {
        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new Error('No se pudo identificar al alumno.');

        const createAA = await apiFetch(`${API_BASE}/api/actividades-alumno`, {
          method: 'POST',
          body: JSON.stringify({ alumnoId, actividadId: tablero.id }),
        });
        const aaData = (await createAA.json()) as ActividadAlumnoDTO;
        if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
          setActividadAlumnoId(aaData.id);
        }

        setLastAttemptScore(null);
        setCerberoPos([0, 0]);
        setAnsweredSet(new Set());
        setModalCell(null);
        setInputAnswer('');
        setFeedback(null);
        setAttemptCompleted(false);
        setAnswerHistory(new Map());
        setSubmittedAnswersByQuestion(new Map());
        setCorrectAnswersByQuestion(new Map());
        setBoardFailureCount(0);
        completedRef.current = false;
      } catch (e) {
        setError(e instanceof Error ? e.message : 'No se pudo crear un nuevo intento');
      }
    })();
  };

  const handleViewStudentAnswers = () => {
    setAnswerModalMode('student');
    setShowAnswerModal(true);
  };

  const handleViewCorrectAnswers = () => {
    setAnswerModalMode('correct');
    setShowAnswerModal(true);
  };

  // ── Render ────────────────────────────────────────────

  return (
    <div className="ta-page">
      <NavbarMisCursos />
      <main className="ta-main">
        <div className="ta-wrapper">
          <ActivityHeader
            title={tablero.titulo}
            subtitle={tablero.descripcion?.trim() || undefined}
            guideType="tablero"
            guideRole="alumno"
          />

          <div className="ta-progress">
            <img src={hombreMisteriosoImg} alt="Hombre misterioso" className="ta-progress-avatar" />
            <span className="ta-progress-label">{answeredSet.size} / {completionTarget}</span>
            <div className="ta-progress-track">
              <div className="ta-progress-fill" style={{ width: `${Math.min(100, (answeredSet.size / completionTarget) * 100)}%` }} />
            </div>
          </div>

          <div className="ta-board-wrap">
            <div
              className="ta-board"
              style={{
                gridTemplateColumns: Array.from({ length: gridDim }, (_, i) => (i % 2 === 0 ? 'auto' : '1fr')).join(' '),
                gridTemplateRows: Array.from({ length: gridDim }, (_, i) => (i % 2 === 0 ? 'auto' : '40px')).join(' '),
              }}
            >
              {gridItems}
            </div>
          </div>

          {!showCompleted && !modalCell && (
            <p className="tablero-hint">Pulsa una casilla adyacente a Cerbero para responder o retroceder</p>
          )}

          {showCompleted && activityConfig ? (
            <>
              <ActivityResultScreen
                title="¡HAS COMPLETADO EL TABLERO!"
                score={lastAttemptScore ?? answeredSet.size}
                maxScore={tablero.puntuacion ?? completionTarget}
                config={activityConfig}
                onContinue={() => navigate(-1)}
                onRetry={handleRetry}
                onViewStudentAnswer={handleViewStudentAnswers}
                onViewCorrectAnswer={handleViewCorrectAnswers}
                onCancel={() => navigate(-1)}
                detail={boardFailureCount > 0
                  ? `Penalización acumulada: ${boardFailureCount} fallo${boardFailureCount === 1 ? '' : 's'} antes de acertar.`
                  : 'Sin penalización por fallos.'}
              />
            </>
          ) : showCompleted ? (
            <CompletionPopup title="¡HAS COMPLETADO EL TABLERO!" onContinue={() => navigate(-1)} />
          ) : null}
        </div>
      </main>

      {modalCell && modalQuestion && (
        <QuestionModal
          question={modalQuestion}
          inputAnswer={inputAnswer}
          feedback={feedback}
          submitting={submitting}
          onInputChange={setInputAnswer}
          onSubmit={handleSubmit}
          onClose={closeModal}
        />
      )}

      {showAnswerModal && (
        <AnswerViewModal
          title={answerModalMode === 'student' ? 'Mi respuesta' : 'Respuesta correcta'}
          answers={tablero.preguntas.map((p) => ({
            question: p.pregunta,
            studentAnswer: submittedAnswersByQuestion.get(p.id) || answerHistory.get(p.id) || '(No respondida)',
            correctAnswer: correctAnswersByQuestion.get(p.id) || '(No disponible)',
            isCorrect: undefined,
          }))}
          onClose={() => setShowAnswerModal(false)}
          mode={answerModalMode}
        />
      )}
    </div>
  );
}