import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import perritoImg from '../../assets/props/perritoCerberito.png';
import caballeroImg from '../../assets/props/caballero.png';
import './TableroAlumno.css';

// ── Types ─────────────────────────────────────────────────

type PreguntaDTO = { id: number; pregunta: string };

type TableroAlumnoDTO = {
  id: number;
  titulo: string;
  tamano: boolean; // true = 3×3, false = 4×4
  respVisible: boolean;
  preguntas: PreguntaDTO[];
};

// ── Helpers ───────────────────────────────────────────────

/** Snake path: (0,0)→(0,1)→…→(0,n-1)→(1,n-1)→(1,n-2)→… */
function buildSnakePath(size: number): [number, number][] {
  const path: [number, number][] = [];
  for (let r = 0; r < size; r++) {
    for (let step = 0; step < size; step++) {
      const c = r % 2 === 0 ? step : size - 1 - step;
      path.push([r, c]);
    }
  }
  return path;
}

interface EdgePos { row: number; col: number; horizontal: boolean; }

function edgePos(path: [number, number][], i: number): EdgePos {
  const [r1, c1] = path[i];
  const [r2, c2] = path[i + 1];
  if (r1 === r2) return { row: r1, col: Math.min(c1, c2), horizontal: true };
  return { row: Math.min(r1, r2), col: c1, horizontal: false };
}

// ── Component ─────────────────────────────────────────────

export default function TableroAlumno() {
  const { tableroId } = useParams<{ tableroId: string }>();
  const navigate = useNavigate();

  const [tablero, setTablero] = useState<TableroAlumnoDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [answered, setAnswered] = useState<boolean[]>([]);
  const [activeIdx, setActiveIdx] = useState<number | null>(null);
  const [inputAnswer, setInputAnswer] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [feedbackCorrect, setFeedbackCorrect] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  useEffect(() => {
    if (!tableroId) return;
    setLoading(true);
    apiFetch(`${apiBase}/api/tableros/${tableroId}`)
      .then((r) => r.json())
      .then((data: TableroAlumnoDTO) => {
        setTablero(data);
        setAnswered(new Array(data.preguntas.length).fill(false));
      })
      .catch((e) => setError(e instanceof Error ? e.message : 'Error al cargar el tablero'))
      .finally(() => setLoading(false));
  }, [tableroId, apiBase]);

  const size = tablero ? (tablero.tamano ? 3 : 4) : 3;
  const path = buildSnakePath(size);
  const totalCells = size * size;
  const gridDim = size * 2 - 1;

  const nextUnanswered = answered.indexOf(false);
  const allDone = answered.length > 0 && answered.every(Boolean);

  const handleEdgeClick = (ei: number) => {
    if (ei !== nextUnanswered) return;
    setActiveIdx(ei);
    setInputAnswer('');
    setFeedback(null);
  };

  const handleSubmit = async () => {
    if (activeIdx === null || !tablero || submitting) return;
    const q = tablero.preguntas[activeIdx];
    setSubmitting(true);
    try {
      const res = await apiFetch(`${apiBase}/api/tableros/${tablero.id}/${q.id}`, {
        method: 'POST',
        body: JSON.stringify(inputAnswer.trim()),
      });
      const msg = await res.text();
      const correct =
        msg.toLowerCase().includes('correcta') && !msg.toLowerCase().includes('incorrecta');
      setFeedback(msg);
      setFeedbackCorrect(correct);
      if (correct) {
        setAnswered((prev) => {
          const next = [...prev];
          next[activeIdx] = true;
          return next;
        });
        setTimeout(() => {
          setActiveIdx(null);
          setFeedback(null);
        }, 1100);
      }
    } catch {
      setFeedback('Error al enviar la respuesta');
      setFeedbackCorrect(false);
    } finally {
      setSubmitting(false);
    }
  };

  // ── Render ─────────────────────────────────────────────

  if (loading)
    return (
      <div className="ta-page">
        <NavbarMisCursos />
        <main className="ta-main">
          <p className="ta-info-msg">Cargando tablero...</p>
        </main>
      </div>
    );

  if (error || !tablero)
    return (
      <div className="ta-page">
        <NavbarMisCursos />
        <main className="ta-main">
          <p className="ta-info-msg ta-info-msg--err">{error || 'No se encontró el tablero'}</p>
        </main>
      </div>
    );

  return (
    <div className="ta-page">
      <NavbarMisCursos />
      <main className="ta-main">
        <div className="ta-wrapper">

          {/* ── Header ── */}
          <div className="ta-header">
            <span className="ta-title">{tablero.titulo}</span>
            <span className="ta-badge">{tablero.tamano ? '3×3' : '4×4'}</span>
          </div>

          {/* ── Board ── */}
          <div className="ta-board-wrap">
            <div
              className="ta-board"
              style={{
                gridTemplateColumns: `repeat(${gridDim}, auto)`,
                gridTemplateRows: `repeat(${gridDim}, auto)`,
              }}
            >
              {/* Cells */}
              {Array.from({ length: totalCells }, (_, pidx) => {
                const [r, c] = path[pidx];
                const isDark = (r + c) % 2 === 1;
                const isStart = pidx === 0;
                const isEnd = pidx === totalCells - 1;
                return (
                  <div
                    key={`cell-${pidx}`}
                    className={`ta-cell ${isDark ? 'ta-cell--dark' : 'ta-cell--light'}`}
                    style={{ gridRow: r * 2 + 1, gridColumn: c * 2 + 1 }}
                  >
                    {isStart && <img src={perritoImg} className="ta-char" alt="inicio" />}
                    {isEnd && allDone && <img src={caballeroImg} className="ta-char" alt="fin" />}
                    {isEnd && !allDone && (
                      <img
                        src={caballeroImg}
                        className="ta-char ta-char--dimmed"
                        alt="fin"
                      />
                    )}
                  </div>
                );
              })}

              {/* Edges (questions) */}
              {tablero.preguntas.map((_, ei) => {
                const { row, col, horizontal } = edgePos(path, ei);
                const gridRow = horizontal ? row * 2 + 1 : row * 2 + 2;
                const gridCol = horizontal ? col * 2 + 2 : col * 2 + 1;
                const isAnswered = answered[ei];
                const isActive = ei === nextUnanswered;
                const isSelected = ei === activeIdx;

                return (
                  <div
                    key={`edge-${ei}`}
                    className={`ta-edge ${horizontal ? 'ta-edge--h' : 'ta-edge--v'}`}
                    style={{ gridRow, gridColumn: gridCol }}
                  >
                    <button
                      type="button"
                      className={`ta-qcard ${
                        isAnswered
                          ? 'ta-qcard--done'
                          : isSelected
                          ? 'ta-qcard--selected'
                          : isActive
                          ? 'ta-qcard--active'
                          : 'ta-qcard--locked'
                      }`}
                      onClick={() => handleEdgeClick(ei)}
                      disabled={isAnswered || (!isActive)}
                      aria-label={
                        isAnswered
                          ? `Pregunta ${ei + 1} respondida`
                          : isActive
                          ? `Responder pregunta ${ei + 1}`
                          : `Pregunta ${ei + 1} bloqueada`
                      }
                    >
                      {isAnswered ? '✓' : '?'}
                    </button>
                  </div>
                );
              })}
            </div>
          </div>

          {/* ── Question panel ── */}
          {activeIdx !== null && !allDone && (
            <div className="ta-panel">
              <p className="ta-panel-num">
                Pregunta {activeIdx + 1} / {tablero.preguntas.length}
              </p>
              <p className="ta-panel-q">{tablero.preguntas[activeIdx].pregunta}</p>
              <div className="ta-panel-row">
                <input
                  className="ta-panel-input"
                  type="text"
                  value={inputAnswer}
                  onChange={(e) => setInputAnswer(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && !submitting && handleSubmit()}
                  placeholder="Tu respuesta..."
                  maxLength={200}
                  autoFocus
                />
                <button
                  className="ta-panel-btn"
                  onClick={handleSubmit}
                  disabled={submitting || !inputAnswer.trim()}
                >
                  {submitting ? '…' : 'Enviar'}
                </button>
              </div>
              {feedback && (
                <p
                  className={`ta-panel-feedback ${
                    feedbackCorrect ? 'ta-panel-feedback--ok' : 'ta-panel-feedback--err'
                  }`}
                >
                  {feedback}
                </p>
              )}
            </div>
          )}

          {/* ── Finished ── */}
          {allDone && (
            <div className="ta-done">
              <p className="ta-done-text">¡Tablero completado!</p>
              <button className="ta-done-btn" onClick={() => navigate(-1)}>
                Volver
              </button>
            </div>
          )}

          {/* ── Hint ── */}
          {activeIdx === null && !allDone && nextUnanswered >= 0 && (
            <p className="ta-hint">
              Haz clic en el <strong>?</strong> resaltado para avanzar por el tablero.
            </p>
          )}
        </div>
      </main>
    </div>
  );
}
