import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import perritoImg from '../../assets/props/perritoCerberito.png';
import mapaIcon from '../../assets/icons/mapa.svg';
import caballeroImg from '../../assets/props/caballeroGanador.png';
import mounstruoImg from '../../assets/props/mounstroMuerto.png';
import hombreMisteriosoImg from '../../assets/props/hombreMisterioso.png';
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

/**
 * Maps a grid cell (r,c) to a question index.
 * Cell (0,0) is Cerbero's start and has no question → returns null.
 * All other cells in row-major order map to preguntas[0], preguntas[1], …
 */
function cellToQIndex(r: number, c: number, size: number): number | null {
  if (r === 0 && c === 0) return null;
  return r * size + c - 1;
}

function isNeighbor(r1: number, c1: number, r2: number, c2: number): boolean {
  return Math.abs(r1 - r2) + Math.abs(c1 - c2) === 1;
}

// ── Dot counts (change these to taste) ────────────────────
const DOTS_3x3 = 4;  // dots in horizontal connectors for 3×3 board
const DOTS_4x4 = 5;  // dots in horizontal connectors for 4×4 board

// ── Component ─────────────────────────────────────────────

export default function TableroAlumno() {
  const { tableroId } = useParams<{ tableroId: string }>();
  const navigate = useNavigate();

  const [tablero, setTablero] = useState<TableroAlumnoDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Game state
  const [cerberoPos, setCerberoPos] = useState<[number, number]>([0, 0]);
  const [answeredSet, setAnsweredSet] = useState<Set<number>>(new Set());

  // Modal state
  const [modalCell, setModalCell] = useState<[number, number] | null>(null);
  const [inputAnswer, setInputAnswer] = useState('');
  const [feedback, setFeedback] = useState<{ correct: boolean; msg: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  useEffect(() => {
    if (!tableroId) return;
    apiFetch(`${apiBase}/api/tableros/${tableroId}`)
      .then((r) => r.json())
      .then((data: TableroAlumnoDTO) => setTablero(data))
      .catch((e) => setError(e instanceof Error ? e.message : 'Error al cargar el tablero'))
      .finally(() => setLoading(false));
  }, [tableroId, apiBase]);

  if (loading)
    return (
      <div className="ta-page">
        <NavbarMisCursos />
        <main className="ta-main"><p className="ta-info-msg">Cargando tablero...</p></main>
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

  const size = tablero.tamano ? 3 : 4;
  // 3×3 completes at 6 correct, 4×4 at 10 correct (provisional)
  const completionTarget = tablero.tamano ? 6 : 10;
  const isComplete = answeredSet.size >= completionTarget;
  const [cr, cc] = cerberoPos;

  // ── Interaction ────────────────────────────────────────

  const handleCellClick = (r: number, c: number) => {
    if (isComplete) return;
    const qIdx = cellToQIndex(r, c, size);
    if (qIdx === null) return;                      // cerbero's own cell
    if (answeredSet.has(qIdx)) return;              // already answered
    if (!isNeighbor(cr, cc, r, c)) return;          // not adjacent
    setModalCell([r, c]);
    setInputAnswer('');
    setFeedback(null);
  };

  const closeModal = () => {
    if (submitting) return;
    setModalCell(null);
    setFeedback(null);
    setInputAnswer('');
  };

  const handleSubmit = async () => {
    if (!modalCell || !tablero || submitting) return;
    const [mr, mc] = modalCell;
    const qIdx = cellToQIndex(mr, mc, size)!;
    const q = tablero.preguntas[qIdx];
    setSubmitting(true);
    try {
      const res = await apiFetch(`${apiBase}/api/tableros/${tablero.id}/${q.id}`, {
        method: 'POST',
        body: JSON.stringify(inputAnswer.trim()),
      });
      const msg = await res.text();
      const correct =
        msg.toLowerCase().includes('correcta') && !msg.toLowerCase().includes('incorrecta');
      setFeedback({ correct, msg });
      if (correct) {
        const newSet = new Set(answeredSet);
        newSet.add(qIdx);
        setAnsweredSet(newSet);
        setCerberoPos([mr, mc]);
        setTimeout(closeModal, 800);
      }
    } catch {
      setFeedback({ correct: false, msg: 'Error al enviar la respuesta' });
    } finally {
      setSubmitting(false);
    }
  };

  // ── Build cells (expanded grid: 2n-1 × 2n-1) ───────────

  const modalQIdx =
    modalCell !== null ? cellToQIndex(modalCell[0], modalCell[1], size) : null;
  const modalQuestion = modalQIdx !== null ? tablero.preguntas[modalQIdx] : null;

  const gridItems: React.ReactElement[] = [];
  const gridDim = size * 2 - 1;
  const hDots = tablero.tamano ? DOTS_3x3 : DOTS_4x4;

  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      const isCerbero = r === cr && c === cc;
      const qIdx = cellToQIndex(r, c, size);
      const isAnswered = qIdx !== null && answeredSet.has(qIdx);
      const isClickable = qIdx !== null && !isAnswered && isNeighbor(cr, cc, r, c) && !isComplete;
      const isDark = (r + c) % 2 === 1;

      // ── Cell ──
      gridItems.push(
        <div
          key={`cell-${r}-${c}`}
          className={[
            'ta-cell',
            (isCerbero || (r === 0 && c === 0)) ? 'ta-cell--cerbero' : (isDark ? 'ta-cell--dark' : 'ta-cell--light'),
            isClickable ? 'ta-cell--clickable' : '',
            !isCerbero && isAnswered ? 'ta-cell--done' : '',
          ].join(' ').trim()}
          style={{ gridRow: r * 2 + 1, gridColumn: c * 2 + 1 }}
          onClick={() => handleCellClick(r, c)}
          role={isClickable ? 'button' : undefined}
          tabIndex={isClickable ? 0 : undefined}
          onKeyDown={(e) => e.key === 'Enter' && handleCellClick(r, c)}
        >
          {isCerbero && (
            <img
              src={perritoImg}
              className={`ta-char${isComplete ? ' ta-char--victory' : ''}`}
              alt="Cerbero"
            />
          )}
          {qIdx !== null && !isCerbero && (
            <span className={`ta-cell-label${isDark ? ' ta-cell-label--light' : ' ta-cell-label--dark'}`}>
              {isAnswered ? '✓' : '?'}
            </span>
          )}
        </div>
      );

      // ── Horizontal connector (right) ──
      if (c < size - 1) {
        gridItems.push(
          <div
            key={`h-${r}-${c}`}
            className="ta-connector ta-connector--h"
            style={{ gridRow: r * 2 + 1, gridColumn: c * 2 + 2 }}
          >
            {Array.from({ length: hDots }).map((_, i) => (
              <span key={i} className="ta-dot" />
            ))}
          </div>
        );
      }

      // ── Vertical connector (below) ──
      if (r < size - 1) {
        gridItems.push(
          <div
            key={`v-${r}-${c}`}
            className="ta-connector ta-connector--v"
            style={{ gridRow: r * 2 + 2, gridColumn: c * 2 + 1 }}
          />
        );
      }
    }
  }

  // ── Render ─────────────────────────────────────────────

  return (
    <div className="ta-page">
      <NavbarMisCursos />
      <main className="ta-main">
        <div className="ta-wrapper">

          {/* Header */}
          <button className="ta-map-btn" onClick={() => navigate(-1)}>
            <img src={mapaIcon} alt="Mapa" className="ta-map-icon" />
            <span>Mapa</span>
          </button>
          <div className="ta-header">
            <span className="ta-title">{tablero.titulo}</span>
          </div>
          {/* Progress */}
          <div className="ta-progress">
            <img src={hombreMisteriosoImg} alt="Hombre misterioso" className="ta-progress-avatar" />
            <span className="ta-progress-label">
              {answeredSet.size} / {completionTarget}
            </span>
            <div className="ta-progress-track">
              <div
                className="ta-progress-fill"
                style={{
                  width: `${Math.min(100, (answeredSet.size / completionTarget) * 100)}%`,
                }}
              />
            </div>
          </div>

          {/* Board */}
          <div className="ta-board-wrap">
            <div
              className="ta-board"
              style={{
                gridTemplateColumns: Array.from({length: gridDim}, (_, i) => i % 2 === 0 ? 'auto' : '1fr').join(' '),
                gridTemplateRows: Array.from({length: gridDim}, (_, i) => i % 2 === 0 ? 'auto' : '80px').join(' '),
              }}
            >
              {gridItems}
            </div>
          </div>

          {/* Hint */}
          {!isComplete && !modalCell && (
            <p className="ta-hint">
              Pulsa una casilla <strong>?</strong> adyacente a Cerbero para responder
            </p>
          )}

          {/* Completion popup */}
          {isComplete && (
            <div className="ta-done-overlay">
              <div className="ta-done-popup">
                <h2 className="ta-done-title">¡HAS COMPLETADO EL TABLERO!</h2>
                <div className="ta-done-imgs">
                  <img src={caballeroImg} alt="Caballero ganador" className="ta-done-caballero" />
                  <img src={mounstruoImg} alt="Monstruo derrotado" className="ta-done-monstruo" />
                </div>
                <button className="ta-done-btn" onClick={() => navigate(-1)}>
                  Continuar
                </button>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* Question modal */}
      {modalCell && modalQuestion && (
        <div
          className="ta-modal-overlay"
          onClick={(e) => e.target === e.currentTarget && closeModal()}
        >
          <div className="ta-modal">
            <h3 className="ta-modal-title">Pregunta</h3>
            <p className="ta-modal-q">{modalQuestion.pregunta}</p>
            <label className="ta-modal-label">Solución</label>
            <input
              className="ta-modal-input"
              type="text"
              value={inputAnswer}
              onChange={(e) => setInputAnswer(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && !submitting && handleSubmit()}
              maxLength={200}
              autoFocus
            />
            {feedback && (
              <p
                className={`ta-modal-feedback ${
                  feedback.correct
                    ? 'ta-modal-feedback--ok'
                    : 'ta-modal-feedback--err'
                }`}
              >
                {feedback.correct ? '¡Correcto! ✓' : feedback.msg}
              </p>
            )}
            <button
              className="ta-modal-btn"
              onClick={handleSubmit}
              disabled={submitting || !inputAnswer.trim()}
            >
              {submitting ? '…' : 'Enviar'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
