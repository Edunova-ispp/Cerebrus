import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import GenerarIAModal from '../../components/GenerarIAModal/GenerarIAModal';
import { apiFetch } from '../../utils/api';
import './CartaForm.css';

export type CartaFormMode = 'create' | 'edit';

export interface CartaFormInitialPregunta {
  readonly id: number;
  readonly pregunta: string;
  readonly imagen: string | null;
  readonly respuestas: readonly {
    readonly id: number;
    readonly respuesta: string;
    readonly correcta: boolean;
  }[];
}

export interface CartaFormInitialValues {
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly imagen: string | null;
  readonly respVisible: boolean;
  readonly permitirReintento?: boolean;
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly version: number;
  readonly temaId?: number;
  readonly preguntas?: readonly CartaFormInitialPregunta[];
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
}

const MAX_CARACTERES_TITULO = 60;
const MAX_CARACTERES_DESCRIPCION = 1000;
const MAX_PUNTUACION = 10000;
const MAX_CARACTERES_COMENTARIOS = 250;
const MAX_CARDS = 9;

interface Card {
  localKey: string;
  id?: number;
  pregunta: string;
  respuesta: string;
  imagen: string;
  respuestaId?: number;
}

function makeLocalCardKey(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

interface Props {
  readonly mode?: CartaFormMode;
  readonly generalId?: number;
  readonly initialValues?: CartaFormInitialValues;
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly onDone?: () => void;
  readonly readOnly?: boolean;
}

function makeEmptyCard(): Card {
  return { localKey: makeLocalCardKey(), pregunta: '', respuesta: '', imagen: '' };
}

export function CartaForm({ mode = 'create', generalId, initialValues, temaIdProp, cursoIdProp, onDone, readOnly }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [imagen, setImagen] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [permitirReintento, setPermitirReintento] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [mostrarPuntuacion, setMostrarPuntuacion] = useState(false);
  const [encontrarRespuestaMaestro, setEncontrarRespuestaMaestro] = useState(false);
  const [cards, setCards] = useState<Card[]>([makeEmptyCard()]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const [showIAModal, setShowIAModal] = useState(false);

  const originalCardsRef = useRef<CartaFormInitialPregunta[]>([]);

  const navigate = useNavigate();
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const temaId = temaIdProp ?? params.temaId ?? (initialValues?.temaId != null ? String(initialValues.temaId) : undefined);

  // Tracks which activity ID was last initialized to prevent re-initializing on every render
  const initializedActivityIdRef = useRef<number | null>(null);

  useEffect(() => {
    if (!initialValues) return;

    // Only reinitialize if the activity ID changed, not on every render
    if (initializedActivityIdRef.current === generalId) return;
    
    initializedActivityIdRef.current = generalId ?? null;

    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion ?? ''));
    setImagen(initialValues.imagen ?? '');
    setRespVisible(Boolean(initialValues.respVisible));
    setPermitirReintento(Boolean(initialValues.permitirReintento));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');
    setMostrarPuntuacion(Boolean(initialValues.mostrarPuntuacion));
    setEncontrarRespuestaMaestro(Boolean(initialValues.encontrarRespuestaMaestro));

    if (initialValues.preguntas && initialValues.preguntas.length > 0) {
      originalCardsRef.current = [...initialValues.preguntas];
      setCards(
        initialValues.preguntas.map((p) => ({
          localKey: makeLocalCardKey(),
          id: p.id,
          pregunta: p.pregunta,
          respuesta: p.respuestas[0]?.respuesta ?? '',
          imagen: p.imagen ?? '',
          respuestaId: p.respuestas[0]?.id,
        })),
      );
    }
  }, [generalId, mode]);

  // ── Card state helpers ───────────────────────────────────────────────────

  const addCard = () => setCards((prev) => [...prev, makeEmptyCard()]);

  const removeCard = (ci: number) =>
    setCards((prev) => prev.filter((_, i) => i !== ci));

  const updateCardPregunta = (ci: number, pregunta: string) =>
    setCards((prev) => prev.map((c, i) => (i === ci ? { ...c, pregunta } : c)));

  const updateCardRespuesta = (ci: number, respuesta: string) =>
    setCards((prev) => prev.map((c, i) => (i === ci ? { ...c, respuesta } : c)));

  const updateCardImagen = (ci: number, imagen: string) =>
    setCards((prev) => prev.map((c, i) => (i === ci ? { ...c, imagen } : c)));
  const handleIAResult = (data: unknown) => {
    console.log('Datos crudos de la IA (Cartas):', data);

    const source = data as Record<string, unknown> | null;
    if (!source) return;

    if (typeof source.titulo === 'string') setTitulo(source.titulo);
    if (typeof source.descripcion === 'string') setDescripcion(source.descripcion);
    if (typeof source.puntuacion === 'string' || typeof source.puntuacion === 'number') {
      setPuntuacion(String(source.puntuacion));
    }

    const rawQuestions = [source.preguntas, source.cartas, source.items].find(Array.isArray);
    if (!Array.isArray(rawQuestions) || rawQuestions.length === 0) return;

    const mappedCards: Card[] = rawQuestions.map((item) => {
      const current = item as Record<string, unknown>;
      const preguntaText = current.enunciado ?? current.pregunta ?? current.anverso ?? '';

      let respuestaText = '';
      const respuestaValue = current.respuesta;
      if (typeof respuestaValue === 'string') {
        respuestaText = respuestaValue;
      } else if (respuestaValue && typeof respuestaValue === 'object') {
        const respuestaObject = respuestaValue as Record<string, unknown>;
        respuestaText = String(respuestaObject.texto ?? respuestaObject.text ?? respuestaObject.respuesta ?? '');
      } else if (Array.isArray(current.respuestas) && current.respuestas.length > 0) {
        const firstRespuesta = current.respuestas[0] as Record<string, unknown>;
        respuestaText = String(firstRespuesta.texto ?? firstRespuesta.respuesta ?? '');
      }

      return {
        localKey: makeLocalCardKey(),
        pregunta: String(preguntaText),
        respuesta: String(respuestaText),
        imagen: '',
      };
    });

    setCards(mappedCards);
  };

  // ── Validation ───────────────────────────────────────────────────────────

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';
    if (titulo.trim().length > MAX_CARACTERES_TITULO) return `El título no puede exceder ${MAX_CARACTERES_TITULO} caracteres`;

    if (descripcion.trim().length > MAX_CARACTERES_DESCRIPCION) return `La descripción no puede exceder ${MAX_CARACTERES_DESCRIPCION} caracteres`;
    if (respVisible && comentariosRespVisible.trim().length === 0) return 'Escribe un comentario de corrección para mostrar.';
    if (comentariosRespVisible.trim().length > MAX_CARACTERES_COMENTARIOS) return `Los comentarios no pueden exceder ${MAX_CARACTERES_COMENTARIOS} caracteres.`;

    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(puntuacionNum)) return 'La puntuación debe ser un número válido';
    if (puntuacionNum <= 0) return 'La puntuación debe ser un número mayor a 0';
    if (puntuacionNum > MAX_PUNTUACION) return `La puntuación no puede exceder ${MAX_PUNTUACION.toLocaleString()}`;

    if (!temaId) return 'Falta el id del tema en la URL';
    if (Number.isNaN(Number.parseInt(temaId, 10))) return 'El id del tema no es válido';
    if (!cursoId) return 'Falta el id del curso en la URL';

    if (cards.length === 0) return 'Añade al menos una carta';
    if (cards.length < 2) return 'Necesitas al menos dos cartas para crear la actividad';
    if (cards.length > MAX_CARDS) return `No puedes añadir más de ${MAX_CARDS} cartas`;

    for (let ci = 0; ci < cards.length; ci++) {
      const c = cards[ci];
      if (!c.pregunta.trim()) return `La carta ${ci + 1} no tiene pregunta`;
      if (!c.respuesta.trim()) return `La carta ${ci + 1} no tiene respuesta`;
    }

    const preguntas = cards.map(c => c.pregunta.trim().toLowerCase());
    const respuestas = cards.map(c => c.respuesta.trim().toLowerCase());
    if (new Set(preguntas).size !== preguntas.length) return 'Hay preguntas repetidas en las cartas';
    if (new Set(respuestas).size !== respuestas.length) return 'Hay respuestas repetidas en las cartas';

    if (mode === 'edit' && !generalId) return 'Falta el id de la actividad a editar';

    return null;
  };

  // ── Submit ────────────────────────────────────────────────────────────────

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    const temaIdNum = Number.parseInt(temaId!, 10);

    setLoading(true);
    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

      if (mode === 'create') {
        const generalRes = await apiFetch(`${apiBase}/api/generales/cartas/maestro`, {
          method: 'POST',
          body: JSON.stringify({
            titulo: titulo.trim(),
            descripcion: descripcion.trim() || null,
            puntuacion: puntuacionNum,
            imagen: imagen.trim() || null,
            tema: { id: temaIdNum },
            respVisible,
            permitirReintento,
            comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || null) : null,
            mostrarPuntuacion,
            encontrarRespuestaMaestro,
            encontrarRespuestaAlumno: false,
            preguntas: [],
          }),
        });
        const gId = (await generalRes.json()) as number;

        for (const card of cards) {
          const pregRes = await apiFetch(`${apiBase}/api/preguntas`, {
            method: 'POST',
            body: JSON.stringify({
              pregunta: card.pregunta.trim(),
              imagen: card.imagen.trim() || null,
              actividadId: gId,
            }),
          });
          const pregId = (await pregRes.json()) as number;

          await apiFetch(`${apiBase}/api/respuestas`, {
            method: 'POST',
            body: JSON.stringify({
              respuesta: card.respuesta.trim(),
              imagen: null,
              correcta: true,
              pregunta: { id: pregId },
            }),
          });
        }
      } else {
        await apiFetch(`${apiBase}/api/generales/update/${generalId}`, {
          method: 'PUT',
          body: JSON.stringify({
            titulo: titulo.trim(),
            descripcion: descripcion.trim() || null,
            puntuacion: puntuacionNum,
            mostrarPuntuacion,
            encontrarRespuestaMaestro,
            imagen: imagen.trim() || null,
            tema: { id: temaIdNum },
            respVisible,
            permitirReintento,
            comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || '') : '',
            posicion: initialValues?.posicion ?? 0,
            version: initialValues?.version ?? 1,
            encontrarRespuestaAlumno: false,
          }),
        });

        const currentCardIds = new Set(cards.filter((c) => c.id).map((c) => c.id!));
        for (const orig of originalCardsRef.current) {
          if (!currentCardIds.has(orig.id)) {
            await apiFetch(`${apiBase}/api/preguntas/delete/${orig.id}`, { method: 'DELETE' });
          }
        }

        for (const card of cards) {
          if (card.id) {
            await apiFetch(`${apiBase}/api/preguntas/update/${card.id}`, {
              method: 'PUT',
              body: JSON.stringify({ pregunta: card.pregunta.trim(), imagen: card.imagen.trim() || null }),
            });

            if (card.respuestaId) {
              await apiFetch(`${apiBase}/api/respuestas/update/${card.respuestaId}`, {
                method: 'PUT',
                body: JSON.stringify({
                  respuesta: card.respuesta.trim(),
                  imagen: null,
                  correcta: true,
                  pregunta: { id: card.id },
                }),
              });
            } else {
              await apiFetch(`${apiBase}/api/respuestas`, {
                method: 'POST',
                body: JSON.stringify({
                  respuesta: card.respuesta.trim(),
                  imagen: null,
                  correcta: true,
                  pregunta: { id: card.id },
                }),
              });
            }
          } else {
            const pregRes = await apiFetch(`${apiBase}/api/preguntas`, {
              method: 'POST',
              body: JSON.stringify({
                pregunta: card.pregunta.trim(),
                imagen: card.imagen.trim() || null,
                actividadId: generalId,
              }),
            });
            const newPregId = (await pregRes.json()) as number;

            await apiFetch(`${apiBase}/api/respuestas`, {
              method: 'POST',
              body: JSON.stringify({
                respuesta: card.respuesta.trim(),
                imagen: null,
                correcta: true,
                pregunta: { id: newPregId },
              }),
            });
          }
        }
      }

      if (onDone) onDone(); else navigate(`/cursos/${cursoId}`);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Error guardando la actividad de cartas';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <>
      <form onSubmit={handleSubmit} className="cf-form">
        {/* ── TOP: Metadata ── */}
        <div className="cf-header">
          <div className="cf-col">
            <div>
              <label className="cf-label" htmlFor="cf-titulo">Título *</label>
              <input
                readOnly={readOnly}
                type="text"
                id="cf-titulo"
                className="cf-input"
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                placeholder="Título de la actividad de cartas"
                required
              />
            </div>

            <div>
              <label className="cf-label" htmlFor="cf-descripcion">Descripción</label>
              <textarea
                readOnly={readOnly}
                id="cf-descripcion"
                className="cf-input"
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                rows={3}
                style={{ resize: 'vertical' }}
                placeholder="Descripción opcional"
              />
            </div>

            <div>
              <label className="cf-label" htmlFor="cf-imagen">URL de imagen (opcional)</label>
              <input
                readOnly={readOnly}
                type="url"
                id="cf-imagen"
                className="cf-input"
                value={imagen}
                onChange={(e) => setImagen(e.target.value)}
                placeholder="https://..."
              />
              {imagen.trim() && (
                <img
                  src={imagen.trim()}
                  alt="Preview"
                  className="cf-img-preview"
                  onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
                  onLoad={(e) => { (e.target as HTMLImageElement).style.display = 'block'; }}
                />
              )}
            </div>
          </div>
          

          <div className="cf-col">
            <div style={{ display: 'flex', alignItems: 'flex-end', gap: 80 }}>
              <div>
                <label className="cf-label" htmlFor="cf-puntuacion">Puntuación *</label>
                <input
                  readOnly={readOnly}
                  type="number"
                  id="cf-puntuacion"
                  className="cf-input cf-input-sm"
                  value={puntuacion}
                  onChange={(e) => setPuntuacion(e.target.value)}
                  min="1"
                  required
                />
              </div>
              {!readOnly && (
                <button type="button" className="iam-trigger-btn" onClick={() => setShowIAModal(true)}>
                  Generar con IA
                </button>
              )}
            </div>

            <label className="cf-check-label">
              <input
                disabled={readOnly}
                type="checkbox"
                checked={respVisible}
                onChange={(e) => setRespVisible(e.target.checked)}
              />
              <span>Mostrar comentarios de corrección</span>
            </label>

            <label className="cf-check-label">
              <input
                disabled={readOnly}
                type="checkbox"
                checked={permitirReintento}
                onChange={(e) => setPermitirReintento(e.target.checked)}
              />
              <span>Permitir reintentos</span>
            </label>

            <label className="cf-check-label">
              <input
                disabled={readOnly}
                type="checkbox"
                checked={mostrarPuntuacion}
                onChange={(e) => setMostrarPuntuacion(e.target.checked)}
              />
              <span>Mostrar puntuación</span>
            </label>

            <label className="cf-check-label">
              <input
                disabled={readOnly}
                type="checkbox"
                checked={encontrarRespuestaMaestro}
                onChange={(e) => setEncontrarRespuestaMaestro(e.target.checked)}
              />
              <span>Mostrar respuesta correcta</span>
            </label>

            {respVisible && (
              <div>
                <label className="cf-label" htmlFor="cf-comentarios">Comentarios</label>
                <input
                  readOnly={readOnly}
                  type="text"
                  id="cf-comentarios"
                  className="cf-input"
                  value={comentariosRespVisible}
                  onChange={(e) => setComentariosRespVisible(e.target.value)}
                />
              </div>
              
            )}
          </div>
        </div>

        {/* ── BOTTOM: Cards ── */}
        <div className="cf-cards">
          <p className="cf-help">
            Añade las cartas. Cada carta tiene una <strong>pregunta</strong> (anverso) y una <strong>respuesta</strong> (reverso). El alumno deberá emparejar cada pregunta con su respuesta.
          </p>
          
          <h3 className="cf-section-title">
            Cartas
            <span>{cards.length} / {MAX_CARDS} máx.</span>
          </h3>

          {cards.map((card, ci) => (
            <div key={card.localKey} className="cf-card-block">
              <div className="cf-card-header">
                <span className="cf-card-label">Carta {ci + 1}</span>
                {cards.length > 1 && !readOnly && (
                  <button
                    disabled={readOnly}
                    type="button"
                    className="cf-btn-remove-card"
                    onClick={() => removeCard(ci)}
                    title="Eliminar carta"
                  >
                    ✕
                  </button>
                )}
              </div>

              <div className="cf-card-fields">
                <div className="cf-card-field">
                  <label className="cf-card-field-label">Pregunta</label>
                  <input
                    readOnly={readOnly}
                    type="text"
                    className="cf-card-input"
                    placeholder={`Pregunta de la carta ${ci + 1}...`}
                    value={card.pregunta}
                    onChange={(e) => updateCardPregunta(ci, e.target.value)}
                  />
                </div>
                <div className="cf-card-field">
                  <label className="cf-card-field-label">Respuesta</label>
                  <input
                    readOnly={readOnly}
                    type="text"
                    className="cf-card-input"
                    placeholder={`Respuesta de la carta ${ci + 1}...`}
                    value={card.respuesta}
                    onChange={(e) => updateCardRespuesta(ci, e.target.value)}
                  />
                </div>
              </div>

              <div className="cf-card-imagen">
                <label className="cf-card-field-label">Imagen de la carta (opcional)</label>
                <input
                  readOnly={readOnly}
                  type="url"
                  className="cf-card-input"
                  placeholder="https://..."
                  value={card.imagen}
                  onChange={(e) => updateCardImagen(ci, e.target.value)}
                />
                {card.imagen.trim() && (
                  <img
                    src={card.imagen.trim()}
                    alt={`Imagen carta ${ci + 1}`}
                    className="cf-card-img-preview"
                    onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
                    onLoad={(e) => { (e.target as HTMLImageElement).style.display = 'block'; }}
                  />
                )}
              </div>
            </div>
          ))}

          {cards.length < MAX_CARDS && !readOnly && (
            <button type="button" className="cf-btn-add-card" onClick={addCard}>
              + Añadir carta
            </button>
          )}
        </div>

        <div className="ca-form-footer">
          <div className="tf-footer-stack">
            {!readOnly && (
              <button className="ca-btn-guardar" type="submit" disabled={loading}>
                  {loading ? 'Guardando...' : 'Guardar'}
              </button>
            )}
            {error && (
              <p className="ca-text tf-error" style={{ color: '#c0392b' }}>
                {error}
              </p>
            )}
          </div>
        </div>
      </form>
      <GenerarIAModal
        tipoActividad="CARTA"
        open={showIAModal}
        onClose={() => setShowIAModal(false)}
        onResult={handleIAResult}
      />
    </>
  );
}