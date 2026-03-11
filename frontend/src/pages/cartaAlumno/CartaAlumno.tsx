import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import reversoCarta from '../../assets/props/reversoCarta.png';
import './CartaAlumno.css';

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

type GeneralCartaDTO = {
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

type ActividadAlumnoDTO = { readonly id: number; readonly puntuacion?: number };

type RespAlumnoGeneralCreateResponse = {
  readonly id: number;
  readonly correcta: boolean;
  readonly comentario: string;
};

/** A card on the board – either a "pregunta" side or a "respuesta" side */
type BoardCard = {
  readonly uid: string;
  readonly kind: 'pregunta' | 'respuesta';
  readonly preguntaId: number;
  readonly respuestaId: number;
  readonly text: string;
  readonly imagen: string | null;
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

function shuffle<T>(arr: T[]): T[] {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

// ── Component ─────────────────────────────────────────────────────────────

export default function CartaAlumno() {
  const { cartaId } = useParams<{ cartaId: string }>();
  const navigate = useNavigate();

  const initInFlightRef = useRef(false);
  const completedRef = useRef(false);
  const abandonReportedRef = useRef(false);
  const actividadAlumnoIdRef = useRef<number | null>(null);

  const [carta, setCarta] = useState<GeneralCartaDTO | null>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [board, setBoard] = useState<BoardCard[]>([]);
  const [flipped, setFlipped] = useState<Set<string>>(new Set());
  const [matched, setMatched] = useState<Set<string>>(new Set());
  const [firstPick, setFirstPick] = useState<string | null>(null);
  const [checking, setChecking] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [respAlumnoIds, setRespAlumnoIds] = useState<number[]>([]);
  const [finished, setFinished] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [earnedPoints, setEarnedPoints] = useState<number | null>(null);

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const cartaIdNum = useMemo(() => {
    if (!cartaId) return NaN;
    return Number.parseInt(cartaId, 10);
  }, [cartaId]);

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ── Initial load ──────────────────────────────────────────────────────────

  useEffect(() => {
    const run = async () => {
      if (initInFlightRef.current) return;
      initInFlightRef.current = true;

      if (!cartaId || Number.isNaN(cartaIdNum)) {
        setError('Falta el id de la actividad en la URL');
        setLoading(false);
        initInFlightRef.current = false;
        return;
      }

      setLoading(true);
      setError('');

      try {
        // 1. Load carta data
        const cartaRes = await apiFetch(`${apiBase}/api/generales/cartas/${cartaIdNum}`);
        const cartaData = (await cartaRes.json()) as GeneralCartaDTO;
        setCarta(cartaData);

        // 2. Build board cards (one pregunta + one respuesta per question, shuffled)
        const cards: BoardCard[] = [];
        for (const p of cartaData.preguntas) {
          const resp = p.respuestas[0];
          if (!resp) continue;
          cards.push({
            uid: `p-${p.id}`,
            kind: 'pregunta',
            preguntaId: p.id,
            respuestaId: resp.id,
            text: p.pregunta,
            imagen: p.imagen,
          });
          cards.push({
            uid: `r-${resp.id}`,
            kind: 'respuesta',
            preguntaId: p.id,
            respuestaId: resp.id,
            text: resp.respuesta,
            imagen: null,
          });
        }
        setBoard(shuffle(cards));

        // 3. Resolve ActividadAlumno
        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new Error('No se pudo identificar al alumno. Inicia sesión de nuevo.');

        const ensureRes = await apiFetch(`${apiBase}/api/actividades-alumno/ensure/${cartaData.id}`);
        const ensureValue = (await ensureRes.json()) as unknown;
        const exists = ensureValue === 1 || ensureValue === '1' || ensureValue === true;

        if (exists) {
          const getAA = await apiFetch(
            `${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${cartaData.id}`,
          );
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            setActividadAlumnoId(aaData.id);
          } else {
            throw new Error('Respuesta inválida al obtener ActividadAlumno');
          }
        } else {
          const createAA = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: cartaData.id }),
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
  }, [cartaId, cartaIdNum, apiBase]);

  // ── Timer ──────────────────────────────────────────────────────────────

  useEffect(() => {
    if (!actividadAlumnoId || finished) return;
    const id = setInterval(() => setElapsed((e) => e + 1), 1000);
    return () => clearInterval(id);
  }, [actividadAlumnoId, finished]);

  // ── Check if game finished ────────────────────────────────────────────────

  useEffect(() => {
    if (board.length > 0 && matched.size === board.length && !finished && actividadAlumnoId) {
      // All matched → auto-correct
      const doCorrect = async () => {
        try {
          const res = await apiFetch(
            `${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`,
            { method: 'PUT', body: JSON.stringify(respAlumnoIds) },
          );
          const data = (await res.json()) as ActividadAlumnoDTO;
          if (typeof data?.puntuacion === 'number') {
            setEarnedPoints(data.puntuacion);
          }
          completedRef.current = true;
          setFinished(true);
        } catch {
          // Already scored or error – still mark finished
          setFinished(true);
        }
      };
      doCorrect();
    }
  }, [matched, board.length, actividadAlumnoId, respAlumnoIds, finished, apiBase]);

  // ── Card click handler ────────────────────────────────────────────────────

  const handleCardClick = async (uid: string) => {
    if (checking || matched.has(uid) || flipped.has(uid) || !actividadAlumnoId) return;

    if (!firstPick) {
      // First card
      setFlipped((prev) => new Set(prev).add(uid));
      setFirstPick(uid);
      return;
    }

    // Second card
    const secondUid = uid;
    setFlipped((prev) => new Set(prev).add(secondUid));
    setChecking(true);

    const card1 = board.find((c) => c.uid === firstPick)!;
    const card2 = board.find((c) => c.uid === secondUid)!;

    // Check if they belong to the same pregunta AND are different kinds
    const isMatch =
      card1.preguntaId === card2.preguntaId && card1.kind !== card2.kind;

    if (isMatch) {
      // Send answer to backend
      try {
        const res = await apiFetch(`${apiBase}/api/respuestas-alumno-general`, {
          method: 'POST',
          body: JSON.stringify({
            actividadAlumnoId,
            preguntaId: card1.preguntaId,
            respuestaId: card1.kind === 'respuesta' ? card1.respuestaId : card2.respuestaId,
          }),
        });
        const data = (await res.json()) as RespAlumnoGeneralCreateResponse;
        if (data.id) {
          setRespAlumnoIds((prev) => [...prev, data.id]);
        }
      } catch {
        // If this fails, still mark as matched visually
      }

      setMatched((prev) => {
        const next = new Set(prev);
        next.add(firstPick);
        next.add(secondUid);
        return next;
      });
      setFirstPick(null);
      setChecking(false);
    } else {
      // Wrong match – wait a moment then flip both back
      await new Promise((r) => setTimeout(r, 900));
      setFlipped((prev) => {
        const next = new Set(prev);
        next.delete(firstPick);
        next.delete(secondUid);
        return next;
      });
      setFirstPick(null);
      setChecking(false);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────

  const matchedCount = matched.size / 2;
  const totalPairs = board.length / 2;

  return (
    <div className="carta-alumno-page">
      <NavbarMisCursos />

      <main className="carta-alumno-main">
        {error && (
          <p style={{ marginTop: 0, color: '#c0392b', fontFamily: "'Pixelify Sans', sans-serif" }}>
            {error}
          </p>
        )}

        {loading && (
          <p style={{ fontFamily: "'Pixelify Sans', sans-serif", fontSize: '1.2rem' }}>
            Cargando...
          </p>
        )}

        {carta && !loading && (
          <>
            {/* ── Header ── */}
            <ActivityHeader title={carta.titulo} />

            {/* ── Progress ── */}
            <div className="ca-al-progress">
              <span className="ca-al-progress-text">
                {finished
                  ? '¡Has emparejado todas las cartas!'
                  : `Parejas: ${matchedCount} / ${totalPairs}`}
              </span>
              <span className="ca-al-timer">
                ⏱ {String(Math.floor(elapsed / 60)).padStart(2, '0')}:{String(elapsed % 60).padStart(2, '0')}
              </span>
            </div>

            {/* ── Board ── */}
            <div className="ca-al-board">
              {board.map((card) => {
                const isFlipped = flipped.has(card.uid) || matched.has(card.uid);
                const isMatched = matched.has(card.uid);

                return (
                  <div
                    key={card.uid}
                    className={`ca-al-card ${isFlipped ? 'ca-al-card--flipped' : ''} ${isMatched ? 'ca-al-card--matched' : ''}`}
                    onClick={() => handleCardClick(card.uid)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') handleCardClick(card.uid); }}
                  >
                    <div className="ca-al-card-inner">
                      {/* Back face */}
                      <div className="ca-al-card-back">
                        <img src={reversoCarta} alt="" className="ca-al-card-back-img" />
                      </div>
                      {/* Front face */}
                      <div className={`ca-al-card-front ca-al-card-front--${card.kind}`}>
                        <span className="ca-al-card-kind">
                          {card.kind === 'pregunta' ? 'PREGUNTA' : 'SOLUCIÓN'}
                        </span>
                        {card.imagen && (
                          <img
                            src={card.imagen}
                            alt=""
                            className="ca-al-card-img"
                            onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
                          />
                        )}
                        <span className="ca-al-card-text">{card.text}</span>
                      </div>
                      {/* Checkmark for matched */}
                      {isMatched && (
                        <span className="ca-al-card-check">✓</span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* ── Completion popup ── */}
            {finished && (
              <CompletionPopup
                title="¡ACTIVIDAD CARTA COMPLETADA!"
                subtitle={`⏱ ${String(Math.floor(elapsed / 60)).padStart(2, '0')}:${String(elapsed % 60).padStart(2, '0')}${earnedPoints != null ? `  •  ${earnedPoints} puntos` : ''}`}
                onContinue={() => navigate(-1)}
              />
            )}
          </>
        )}
      </main>
    </div>
  );
}
