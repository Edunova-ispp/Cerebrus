import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import ActivityResultScreen, { type ActivityResultConfig } from '../../components/ActivityResultScreen/ActivityResultScreen';
import AnswerViewModal from '../../components/AnswerViewModal/AnswerViewModal';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './CrucigramaAlumno.css';

function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo();
  if (!info) return null;
  const raw = info?.id ?? info?.userId ?? info?.sub;
  const userId = typeof raw === 'string' ? Number(raw) : raw;
  return typeof userId === 'number' && Number.isFinite(userId) ? userId : null;
}

type ActividadAlumnoDTO = {
  readonly id: number;
  readonly puntuacion?: number | null;
  readonly nota?: number;
  readonly numAbandonos?: number;
  readonly alumnoId?: number;
  readonly actividadId?: number;
  readonly fechaInicio?: string | null;
  readonly fechaFin?: string | null;
  readonly solucionUsada?: boolean | null;
};

function isCompletedAttempt(fechaFin?: string | null): boolean {
  if (!fechaFin) return false;
  const parsed = new Date(fechaFin);
  if (Number.isNaN(parsed.getTime())) return false;
  return parsed.getFullYear() !== 1970;
}

// ── Types ─────────────────────────────────────────────────────────────────

type CrucigramaDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly respVisible: boolean;
  readonly temaId: number;
  readonly preguntasYRespuestas: Record<string, string>;
  readonly permitirReintento?: boolean;
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
  readonly encontrarRespuestaAlumno?: boolean;
};

type WordEntry = {
  readonly word: string;
  readonly clue: string;
  readonly row: number;
  readonly col: number;
  readonly dir: 'h' | 'v';
  readonly num: number;
};

type CellState = {
  readonly letter: string;
  readonly wordIds: number[];
  readonly numLabel?: number;
};

type Selection = {
  readonly r: number;
  readonly c: number;
  readonly wordIdx: number;
};

// ── Constants ─────────────────────────────────────────────────────────────

const GRID_SIZE = 10; // always 10x10
const CELL_SIZE = 46; // px
const GAP = 6;        // px between cells
// ── Crossword layout builder ──────────────────────────────────────────────

function buildLayout(preguntasYRespuestas: Record<string, string>): {
  words: WordEntry[];
} {
  const entries = Object.entries(preguntasYRespuestas).map(([clue, answer]) => ({
    clue,
    word: answer.toUpperCase().replace(/\s/g, ''),
  }));

  if (entries.length === 0) return { words: [] };

  const placed: WordEntry[] = [];
  const grid: Record<string, string> = {};

  const place = (word: string, row: number, col: number, dir: 'h' | 'v') => {
    for (let i = 0; i < word.length; i++) {
      const r = dir === 'h' ? row : row + i;
      const c = dir === 'h' ? col + i : col;
      grid[`${r},${c}`] = word[i];
    }
  };

  // Place first word horizontally, centered
  const first = entries[0];
  const firstCol = Math.max(0, Math.floor((GRID_SIZE - first.word.length) / 2));
  const firstRow = Math.floor(GRID_SIZE / 2);
  placed.push({ word: first.word, clue: first.clue, row: firstRow, col: firstCol, dir: 'h', num: 1 });
  place(first.word, firstRow, firstCol, 'h');

  let numCounter = 2;

  for (let ei = 1; ei < entries.length; ei++) {
    const { word, clue } = entries[ei];
    let bestPlacement: { row: number; col: number; dir: 'h' | 'v' } | null = null;
    let bestScore = -1;

    for (const pw of placed) {
      const otherDir: 'h' | 'v' = pw.dir === 'h' ? 'v' : 'h';

      for (let pi = 0; pi < pw.word.length; pi++) {
        const pivotR = pw.dir === 'h' ? pw.row : pw.row + pi;
        const pivotC = pw.dir === 'h' ? pw.col + pi : pw.col;
        const pivotLetter = pw.word[pi];

        for (let wi = 0; wi < word.length; wi++) {
          if (word[wi] !== pivotLetter) continue;

          const newRow = otherDir === 'h' ? pivotR : pivotR - wi;
          const newCol = otherDir === 'h' ? pivotC - wi : pivotC;

          // Must fit inside 10x10
          const endRow = otherDir === 'h' ? newRow : newRow + word.length - 1;
          const endCol = otherDir === 'h' ? newCol + word.length - 1 : newCol;
          if (newRow < 0 || newCol < 0 || endRow >= GRID_SIZE || endCol >= GRID_SIZE) continue;

          let valid = true;
          let intersections = 0;
          for (let k = 0; k < word.length; k++) {
            const r = otherDir === 'h' ? newRow : newRow + k;
            const c = otherDir === 'h' ? newCol + k : newCol;
            const existing = grid[`${r},${c}`];
            if (existing) {
              if (existing === word[k]) intersections++;
              else { valid = false; break; }
            }
            if (otherDir === 'h' && k > 0 && grid[`${r},${c - 1}`] &&
              !placed.some(p => p.dir === 'h' && p.row === r && p.col <= c - 1 && p.col + p.word.length > c - 1)) {
              valid = false; break;
            }
          }
          if (valid) {
            const beforeR = otherDir === 'h' ? newRow : newRow - 1;
            const beforeC = otherDir === 'h' ? newCol - 1 : newCol;
            const afterR = otherDir === 'h' ? newRow : newRow + word.length;
            const afterC = otherDir === 'h' ? newCol + word.length : newCol;
            if (grid[`${beforeR},${beforeC}`] || grid[`${afterR},${afterC}`]) valid = false;
          }

          if (valid && intersections > bestScore) {
            bestScore = intersections;
            bestPlacement = { row: newRow, col: newCol, dir: otherDir };
          }
        }
      }
    }

    if (bestPlacement) {
      placed.push({ word, clue, row: bestPlacement.row, col: bestPlacement.col, dir: bestPlacement.dir, num: numCounter++ });
      place(word, bestPlacement.row, bestPlacement.col, bestPlacement.dir);
    } else {
      // Fallback: find first row/col that fits
      let placed2 = false;
      for (let row = 0; row < GRID_SIZE && !placed2; row++) {
        const col = Math.max(0, Math.floor((GRID_SIZE - word.length) / 2));
        if (col + word.length > GRID_SIZE) continue;
        let rowFree = true;
        for (let k = 0; k < word.length; k++) {
          if (grid[`${row},${col + k}`]) { rowFree = false; break; }
        }
        if (rowFree) {
          placed.push({ word, clue, row, col, dir: 'h', num: numCounter++ });
          place(word, row, col, 'h');
          placed2 = true;
        }
      }
    }
  }

  // Renumber by reading order
  const sorted = [...placed].sort((a, b) => a.row !== b.row ? a.row - b.row : a.col - b.col);
  const numMap: Record<number, number> = {};
  sorted.forEach((w, i) => { numMap[w.num] = i + 1; });
  const renumbered = placed.map(w => ({ ...w, num: numMap[w.num] }));

  return { words: renumbered };
}

// ── Component ─────────────────────────────────────────────────────────────

export default function CrucigramaAlumno() {
  const { crucigramaId } = useParams<{ crucigramaId: string }>();
  const navigate = useNavigate();

  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const [crucigrama, setCrucigrama] = useState<CrucigramaDTO | null>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [activityConfig, setActivityConfig] = useState<ActivityResultConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [checked, setChecked] = useState(false);
  const [answers, setAnswers] = useState<Map<string, string>>(new Map());
  const [selection, setSelection] = useState<Selection | null>(null);
  const [cellResults, setCellResults] = useState<Map<string, 'correct' | 'wrong'>>(new Map());
  const [elapsed, setElapsed] = useState(0);
  const [earnedPoints, setEarnedPoints] = useState<number | null>(null);
  const [lastAttemptScore, setLastAttemptScore] = useState<number | null>(null);
  const [showAnswerModal, setShowAnswerModal] = useState(false);
  const [answerModalMode, setAnswerModalMode] = useState<'student' | 'correct'>('student');

  const containerRef = useRef<HTMLDivElement>(null);
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  // Keep ref in sync
  useEffect(() => {
    actividadAlumnoIdRef.current = actividadAlumnoId;
  }, [actividadAlumnoId]);

  // Abandon on unmount if not finished
  useEffect(() => {
    return () => {
      const id = actividadAlumnoIdRef.current;
      if (!id || completedRef.current || abandonReportedRef.current) return;
      abandonReportedRef.current = true;
      apiFetch(`${apiBase}/api/actividades-alumno/${id}/abandon`, { method: 'POST' }).catch(() => {});
    };
  }, [apiBase]);

  // ── Load + ensure ActividadAlumno ─────────────────────────────────────
  useEffect(() => {
    if (!crucigramaId || initInFlightRef.current) return;
    initInFlightRef.current = true;
    const run = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await apiFetch(`${apiBase}/api/generales/crucigrama/${crucigramaId}`);
        const data = (await res.json()) as CrucigramaDTO;
        setCrucigrama(data);

        // Load activity configuration
        setActivityConfig({
          showScore: data.mostrarPuntuacion ?? true,
          allowRetry: data.permitirReintento ?? false,
          showCorrectAnswer: data.encontrarRespuestaMaestro ?? true,
          showStudentAnswer: data.encontrarRespuestaAlumno ?? true,
        });

        // Resolve ActividadAlumno
        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new TypeError('No se pudo identificar al alumno.');

        let hasExisting = false;
        try {
          const getAA = await apiFetch(
            `${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${data.id}`,
          );
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number') {
            hasExisting = true;
            setActividadAlumnoId(aaData.id);
            if (isCompletedAttempt(aaData.fechaFin)) {
              completedRef.current = true;
              setSubmitted(true);
              setLastAttemptScore(aaData.puntuacion ?? 0);
            }
          }
        } catch {
          hasExisting = false;
        }

        if (!hasExisting) {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: data.id }),
          });
          const aaData = (await createAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number') setActividadAlumnoId(aaData.id);
        }
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Error cargando el crucigrama');
      } finally {
        setLoading(false);
        initInFlightRef.current = false;
      }
    };
    run();
  }, [crucigramaId, apiBase]);

  // ── Timer ──────────────────────────────────────────────────────────────
  useEffect(() => {
    if (!actividadAlumnoId || submitted) return;
    const id = setInterval(() => setElapsed((e) => e + 1), 1000);
    return () => clearInterval(id);
  }, [actividadAlumnoId, submitted]);

  // ── Layout ────────────────────────────────────────────────────────────
  const { words, cellMap } = useMemo(() => {
    if (!crucigrama) return { words: [], cellMap: new Map<string, CellState>() };

    const { words: w } = buildLayout(crucigrama.preguntasYRespuestas);

    const map = new Map<string, CellState>();
    w.forEach((word, wi) => {
      for (let i = 0; i < word.word.length; i++) {
        const r2 = word.dir === 'h' ? word.row : word.row + i;
        const c2 = word.dir === 'h' ? word.col + i : word.col;
        const key = `${r2},${c2}`;
        const existing = map.get(key);
        map.set(key, {
          letter: word.word[i],
          wordIds: [...(existing?.wordIds ?? []), wi],
          numLabel: existing?.numLabel,
        });
      }
    });

    w.forEach(word => {
      const key = `${word.row},${word.col}`;
      const existing = map.get(key)!;
      if (!existing.numLabel) {
        map.set(key, { ...existing, numLabel: word.num });
      }
    });

    return { words: w, cellMap: map };
  }, [crucigrama]);

  const horizontalWords = useMemo(() => words.filter(w => w.dir === 'h').sort((a, b) => a.num - b.num), [words]);
  const verticalWords   = useMemo(() => words.filter(w => w.dir === 'v').sort((a, b) => a.num - b.num), [words]);

  const completedWordIds = useMemo(() => {
    const done = new Set<number>();
    words.forEach((word, wi) => {
      let complete = true;
      for (let i = 0; i < word.word.length; i++) {
        const r = word.dir === 'h' ? word.row : word.row + i;
        const c = word.dir === 'h' ? word.col + i : word.col;
        if (!answers.get(`${r},${c}`)) { complete = false; break; }
      }
      if (complete) done.add(wi);
    });
    return done;
  }, [words, answers]);

  const progressPct = words.length > 0 ? Math.round((completedWordIds.size / words.length) * 100) : 0;
  const allFilled = words.length > 0 && words.every((_, wi) => completedWordIds.has(wi));

  const activeWordCells = useMemo(() => {
    if (!selection) return new Set<string>();
    const word = words[selection.wordIdx];
    if (!word) return new Set<string>();
    const cells = new Set<string>();
    for (let i = 0; i < word.word.length; i++) {
      const r = word.dir === 'h' ? word.row : word.row + i;
      const c = word.dir === 'h' ? word.col + i : word.col;
      cells.add(`${r},${c}`);
    }
    return cells;
  }, [selection, words]);

  const handleRetry = async () => {
    if (!crucigrama) return;

    try {
      const alumnoId = getCurrentUserIdFromJwt();
      if (!alumnoId) throw new TypeError('No se pudo identificar al alumno.');

      const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
        method: 'POST',
        body: JSON.stringify({ alumnoId, actividadId: crucigrama.id }),
      });
      const aaData = (await createAA.json()) as ActividadAlumnoDTO;
      if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
        setActividadAlumnoId(aaData.id);
        actividadAlumnoIdRef.current = aaData.id;
      }

      completedRef.current = false;
      abandonReportedRef.current = false;
      setAnswers(new Map());
      setCellResults(new Map());
      setSubmitted(false);
      setChecked(false);
      setElapsed(0);
      setEarnedPoints(null);
      setLastAttemptScore(null);
      setError('');
      setShowAnswerModal(false);
      setAnswerModalMode('student');
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

  const handleCellClick = useCallback((r: number, c: number) => {
    if (submitted) return;
    const cell = cellMap.get(`${r},${c}`);
    if (!cell) return;
    if (selection?.r === r && selection?.c === c && cell.wordIds.length > 1) {
      const currentIdx = cell.wordIds.indexOf(selection.wordIdx);
      setSelection({ r, c, wordIdx: cell.wordIds[(currentIdx + 1) % cell.wordIds.length] });
    } else {
      setSelection({ r, c, wordIdx: cell.wordIds[0] });
    }
    containerRef.current?.focus();
  }, [cellMap, selection, submitted]);

  const handleCheck = async () => {
    const results = new Map<string, 'correct' | 'wrong'>();
    cellMap.forEach((cell, key) => {
      results.set(key, (answers.get(key) ?? '') === cell.letter ? 'correct' : 'wrong');
    });
    setCellResults(results);
    setChecked(true);
    const allCorrect = [...results.values()].every(v => v === 'correct');
    if (allCorrect) {
      setSubmitted(true);
      if (actividadAlumnoId) {
        try {
          const res = await apiFetch(
            `${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`,
            { method: 'PUT', body: JSON.stringify([]) },
          );
          const data = (await res.json()) as ActividadAlumnoDTO;
          if (typeof data?.puntuacion === 'number') setEarnedPoints(data.puntuacion);
          completedRef.current = true;
        } catch {
          completedRef.current = true;
        }
      }
    }
  };

  const handleReveal = async () => {
    const full = new Map<string, string>();
    cellMap.forEach((cell, key) => full.set(key, cell.letter));
    setAnswers(full);
    setSubmitted(true);

    if (actividadAlumnoId) {
      try {
        const currentAARes = await apiFetch(`${apiBase}/api/actividades-alumno/${actividadAlumnoId}`);
        const currentAA = (await currentAARes.json()) as ActividadAlumnoDTO;

        await apiFetch(`${apiBase}/api/actividades-alumno/update/${actividadAlumnoId}`, {
          method: 'PUT',
          body: JSON.stringify({
            id: actividadAlumnoId,
            puntuacion: currentAA.puntuacion ?? 0,
            fechaInicio: currentAA.fechaInicio ?? null,
            fechaFin: currentAA.fechaFin ?? null,
            nota: currentAA.nota ?? 0,
            numAbandonos: currentAA.numAbandonos ?? 0,
            alumnoId: currentAA.alumnoId,
            actividadId: currentAA.actividadId,
            solucionUsada: true,
          }),
        });

        const res = await apiFetch(
          `${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`,
          { method: 'PUT', body: JSON.stringify([]) },
        );
        const data = (await res.json()) as ActividadAlumnoDTO;
        if (typeof data?.puntuacion === 'number') setEarnedPoints(data.puntuacion);
      } catch (e) {
        // Error checking answers, but still mark as completed
        console.error('Error checking answers:', e);
      } finally {
        completedRef.current = true;
      }
    }
  };

  const handleClueClick = (wi: number) => {
    const word = words[wi];
    if (!word) return;
    setSelection({ r: word.row, c: word.col, wordIdx: wi });
  };

  const renderClueSection = (
    clueWords: WordEntry[],
    orientation: 'horizontal' | 'vertical',
    title: string,
    badge: '→' | '↓',
  ) => {
    if (clueWords.length === 0) return null;

    return (
      <div className="cr-clue-section">
        <div className={`cr-clue-title ${orientation}`}>
          <span className={`cr-arrow-badge ${orientation === 'horizontal' ? 'h' : 'v'}`}>{badge}</span>
          {title}
        </div>
        {clueWords.map(word => {
          const wi = words.indexOf(word);
          return (
            <button
              key={`${orientation[0]}-${word.num}`}
              className={`cr-clue-item ${selection?.wordIdx === wi ? 'active-clue-item' : ''}`}
              type="button"
              onClick={() => handleClueClick(wi)}
            >
              <span className={`cr-clue-num ${completedWordIds.has(wi) ? 'done' : ''}`}>{word.num}</span>
              <span className="cr-clue-text">{word.clue}</span>
            </button>
          );
        })}
      </div>
    );
  };

  const activeClueInfo = useMemo(() => {
    if (!selection) return null;
    const word = words[selection.wordIdx];
    if (!word) return null;
    return { dir: word.dir, num: word.num, clue: word.clue };
  }, [selection, words]);

  const score = useMemo(() => {
    if (!checked) return null;
    const correct = words.filter(word => {
      for (let i = 0; i < word.word.length; i++) {
        const r = word.dir === 'h' ? word.row : word.row + i;
        const c = word.dir === 'h' ? word.col + i : word.col;
        if ((answers.get(`${r},${c}`) ?? '') !== word.word[i]) return false;
      }
      return true;
    }).length;
    return { correct, total: words.length };
  }, [checked, words, answers]);

  const getCellClass = (r: number, c: number): string => {
    const key = `${r},${c}`;
    const cell = cellMap.get(key);
    if (!cell) return 'cr-cell blocked';
    const classes = ['cr-cell'];
    const hasH = cell.wordIds.some(wi => words[wi]?.dir === 'h');
    const hasV = cell.wordIds.some(wi => words[wi]?.dir === 'v');
    if (hasH && hasV) classes.push('cell-hv');
    else if (hasH) classes.push('cell-h');
    else if (hasV) classes.push('cell-v');
    if (checked) {
      const res = cellResults.get(key);
      if (res === 'correct') classes.push('correct');
      else if (res === 'wrong') classes.push('wrong');
    }
    if (selection?.r === r && selection?.c === c) classes.push('active');
    else if (activeWordCells.has(key)) classes.push('active-word');
    return classes.join(' ');
  };

  if (loading) {
    return (
      <div className="crucigrama-page">
        <NavbarMisCursos />
        <main className="crucigrama-main"><p className="ca-text">Cargando...</p></main>
      </div>
    );
  }

  const gridPx = GRID_SIZE * CELL_SIZE + (GRID_SIZE - 1) * GAP;

  return (
    <div className="crucigrama-page">
      <NavbarMisCursos />
      <main className="crucigrama-main">
        {error && <p className="ca-text" style={{ color: '#c0392b' }}>{error}</p>}

        {crucigrama && (
          <>
            <ActivityHeader
              title={crucigrama.titulo}
              subtitle={crucigrama.descripcion?.trim() || undefined}
              guideType="crucigrama"
              guideRole="alumno"
            />

            {checked && score && (
              <div className={`cr-score-banner ${score.correct === score.total ? 'perfect' : ''}`}>
                {score.correct === score.total
                  ? `¡Perfecto! ${score.correct} / ${score.total} palabras correctas`
                  : `${score.correct} / ${score.total} palabras correctas`}
              </div>
            )}

            <div className="cr-progress-wrap">
              <span className="cr-timer">
                ⏱ {String(Math.floor(elapsed / 60)).padStart(2, '0')}:{String(elapsed % 60).padStart(2, '0')}
              </span>
              <span>Progreso:</span>
              <div className="cr-progress-bar">
                <div className="cr-progress-fill" style={{ width: `${progressPct}%` }} />
              </div>
              <span className="cr-progress-label">{completedWordIds.size} / {words.length}</span>
            </div>

            <div className="cr-active-clue">
              {activeClueInfo ? (
                <>
                  <span className={`cr-active-clue-dir ${activeClueInfo.dir === 'v' ? 'vertical' : ''}`}>
                    {activeClueInfo.dir === 'h' ? `→ ${activeClueInfo.num}` : `↓ ${activeClueInfo.num}`}
                  </span>
                  <span className="cr-active-clue-text">{activeClueInfo.clue}</span>
                </>
              ) : (
                <span className="cr-active-clue-text" style={{ color: '#aaa' }}>
                  Haz clic en una celda para seleccionar una pista
                </span>
              )}
            </div>

            <div className="cr-body">
              {/* Grid — always 10×10, black = no word */}
              <div className="cr-grid-wrap">
                <div
                  ref={containerRef}
                  className="cr-grid"
                  style={{
                    gridTemplateColumns: `repeat(${GRID_SIZE}, ${CELL_SIZE}px)`,
                    gridTemplateRows: `repeat(${GRID_SIZE}, ${CELL_SIZE}px)`,
                    gap: `${GAP}px`,
                    width: `${gridPx}px`,
                    height: `${gridPx}px`,
                    outline: 'none',
                    flexShrink: 0,
                  }}
                >
                  {Array.from({ length: GRID_SIZE }, (_, r) =>
                    Array.from({ length: GRID_SIZE }, (_, c) => {
                      const key = `${r},${c}`;
                      const cell = cellMap.get(key);
                      return (
                        <button
                          key={key}
                          className={getCellClass(r, c)}
                          type="button"
                          onClick={() => cell && handleCellClick(r, c)}
                          disabled={!cell}
                        >
                          {cell && (
                            <>
                              {cell.numLabel && <span className="cr-cell-num">{cell.numLabel}</span>}
                              <span className="cr-cell-letter">{answers.get(key) ?? ''}</span>
                            </>
                          )}
                        </button>
                      );
                    })
                  )}
                </div>
              </div>

              {/* Clues */}
              <div className="cr-clues">
                {renderClueSection(horizontalWords, 'horizontal', 'Horizontal', '→')}
                {renderClueSection(verticalWords, 'vertical', 'Vertical', '↓')}
              </div>
            </div>

            <div className="cr-btn-row">
              {submitted ? (
                <button type="button" className="cr-btn cr-btn-back" onClick={() => navigate(-1)}>
                  VOLVER AL CURSO
                </button>
              ) : (
                <>
                  <button type="button" className="cr-btn cr-btn-check" onClick={handleCheck} disabled={!allFilled}>
                    COMPROBAR
                  </button>
                  {crucigrama.respVisible && (
                    <button
                      type="button"
                      className="cr-btn cr-btn-reveal"
                      onClick={handleReveal}
                      title="Ver solución cierra el intento y aplica penalización máxima por tiempo"
                    >
                      VER SOLUCIÓN
                    </button>
                  )}
                </>
              )}
            </div>

            {!submitted && crucigrama.respVisible && (
              <p className="cr-hint">⚠️ Ver solución cierra el intento y aplica penalización máxima por tiempo.</p>
            )}

            {!submitted && !allFilled && (
              <p className="cr-hint">Escribe con el teclado · Haz clic en una celda para seleccionarla</p>
            )}
          </>
        )}

        {!crucigrama && !error && <p className="ca-text">No se encontró el crucigrama.</p>}
        {submitted && score?.correct === score?.total && activityConfig ? (
          <ActivityResultScreen
            title="¡CRUCIGRAMA COMPLETADO!"
            score={lastAttemptScore ?? (earnedPoints || 0)}
            maxScore={crucigrama?.puntuacion || 100}
            config={activityConfig}
            onContinue={() => navigate(-1)}
            onRetry={handleRetry}
            onViewStudentAnswer={handleViewStudentAnswers}
            onViewCorrectAnswer={handleViewCorrectAnswers}
            onCancel={() => navigate(-1)}
          />
        ) : submitted && score?.correct === score?.total ? (
          <CompletionPopup
            title="¡CRUCIGRAMA COMPLETADO!"
            subtitle={`⏱ ${String(Math.floor(elapsed / 60)).padStart(2, '0')}:${String(elapsed % 60).padStart(2, '0')}${earnedPoints != null ? `  •  ${earnedPoints} puntos` : ''}`}
            onContinue={() => navigate(-1)}
          />
        ) : null}

        {showAnswerModal && (
          <AnswerViewModal
            title={answerModalMode === 'student' ? 'Mi respuesta' : 'Respuesta correcta'}
            answers={crucigrama ? Object.entries(crucigrama.preguntasYRespuestas).map(([clue, correctAnswer]) => ({
              question: clue,
              studentAnswer: '(Ver crucigrama completado)',
              correctAnswer: correctAnswer.toUpperCase(),
              isCorrect: checked || submitted,
            })) : []}
            onClose={() => setShowAnswerModal(false)}
            mode={answerModalMode}
          />
        )}
      </main>
    </div>
  );
}