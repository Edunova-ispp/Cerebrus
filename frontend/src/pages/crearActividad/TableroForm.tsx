import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import { getCurrentUserRoles } from '../../types/curso';
import GenerarIAModal from '../../components/GenerarIAModal/GenerarIAModal';
import './TableroForm.css';

export type TableroFormMode = 'create' | 'edit';

export interface TableroFormInitialValues {
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly respVisible: boolean;
  /** true = 3×3 (8 preguntas), false = 4×4 (15 preguntas) */
  readonly tamano: boolean;
  readonly temaId: number;
  readonly preguntas: readonly { readonly pregunta: string; readonly respuesta: string }[];
}

interface Props {
  readonly mode?: TableroFormMode;
  readonly tableroId?: number;
  readonly initialValues?: TableroFormInitialValues;
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly onDone?: () => void;
}

const PREGUNTAS_3X3 = 8;
const PREGUNTAS_4X4 = 15;

type QPair = { localKey: string; pregunta: string; respuesta: string };

function makeLocalKey(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

function makeQuestions(count: number): QPair[] {
  return Array.from({ length: count }, () => ({ localKey: makeLocalKey(), pregunta: '', respuesta: '' }));
}

const isCellDark = (row: number, col: number) => (row + col) % 2 === 1;

export function TableroForm({ mode = 'create', tableroId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [tamano, setTamano] = useState<boolean | null>(null);
  const [temaIdState, setTemaIdState] = useState<number | null>(null);
  const [preguntas, setPreguntas] = useState<QPair[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [showIAModal, setShowIAModal] = useState(false);

  const navigate = useNavigate();
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const temaId = temaIdProp ?? params.temaId;

  // Role check
  const isMaestro = getCurrentUserRoles().some((r) => r.includes('MAESTRO'));

  useEffect(() => {
    if (!initialValues) return;
    setTitulo(initialValues.titulo);
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion));
    setRespVisible(initialValues.respVisible);
    setTamano(initialValues.tamano);
    setTemaIdState(initialValues.temaId);
    setPreguntas(
      initialValues.preguntas.map((p) => ({ localKey: makeLocalKey(), pregunta: p.pregunta, respuesta: p.respuesta })),
    );
  }, [initialValues]);

  const handleTamanoChange = (nuevo: boolean) => {
    if (tamano === nuevo) return;
    setTamano(nuevo);
    setPreguntas(makeQuestions(nuevo ? PREGUNTAS_3X3 : PREGUNTAS_4X4));
  };

  const updatePregunta = (i: number, value: string) =>
    setPreguntas((prev) => prev.map((p, idx) => (idx === i ? { ...p, pregunta: value } : p)));

  const updateRespuesta = (i: number, value: string) =>
    setPreguntas((prev) => prev.map((p, idx) => (idx === i ? { ...p, respuesta: value } : p)));

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';
    const pts = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(pts) || pts <= 0) return 'La puntuación debe ser un número mayor a 0';
    if (tamano === null) return 'Selecciona el tamaño del tablero';
    if (mode === 'create' && !temaId) return 'Falta el id del tema en la URL';
    if (mode === 'edit' && !tableroId) return 'Falta el id del tablero a editar';
    const expected = tamano ? PREGUNTAS_3X3 : PREGUNTAS_4X4;
    const textosSeen = new Set<string>();
    for (let i = 0; i < expected; i++) {
      if (!preguntas[i]?.pregunta.trim()) return `La pregunta ${i + 1} está vacía`;
      if (!preguntas[i]?.respuesta.trim()) return `La respuesta de la pregunta ${i + 1} está vacía`;
      const clave = preguntas[i].pregunta.trim().toLowerCase();
      if (textosSeen.has(clave)) return `Pregunta repetida: "${preguntas[i].pregunta.trim()}" (pregunta ${i + 1})`;
      textosSeen.add(clave);
    }
    return null;
  };

  const buildPayload = () => {
    const preguntasYRespuestas: Record<string, string> = {};
    const expected = tamano ? PREGUNTAS_3X3 : PREGUNTAS_4X4;
    for (let i = 0; i < expected; i++) {
      preguntasYRespuestas[preguntas[i].pregunta.trim()] = preguntas[i].respuesta.trim();
    }
    return {
      titulo: titulo.trim(),
      descripcion: descripcion.trim() || null,
      puntuacion: Number.parseInt(puntuacion.trim(), 10),
      tamano,
      temaId: mode === 'create' ? Number.parseInt(temaId!, 10) : temaIdState,
      respVisible,
      preguntasYRespuestas,
    };
  };

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }
    const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
    setLoading(true);
    try {
      if (mode === 'create') {
        await apiFetch(`${apiBase}/api/tableros`, {
          method: 'POST',
          body: JSON.stringify(buildPayload()),
        });
        if (onDone) onDone(); else navigate(`/cursos/${cursoId}`);
      } else {
        await apiFetch(`${apiBase}/api/tableros/${tableroId}`, {
          method: 'PUT',
          body: JSON.stringify(buildPayload()),
        });
        if (onDone) onDone(); else navigate(`/cursos/${cursoId}`);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al guardar el tablero');
    } finally {
      setLoading(false);
    }
  };

  if (!isMaestro) {
    return (
      <p className="tbl-error">Solo los profesores pueden gestionar actividades de tablero.</p>
    );
  }

  const numPreguntas = tamano === null ? 0 : tamano ? PREGUNTAS_3X3 : PREGUNTAS_4X4;

  const handleIAResult = (data: any) => {
    console.log("Datos crudos de la IA (Tablero):", data);

    if (data.titulo) setTitulo(String(data.titulo));
    if (data.descripcion) setDescripcion(String(data.descripcion));
    if (data.puntuacion) setPuntuacion(String(data.puntuacion));

    const arrayPreguntas = data.preguntas || data.casillas || data.items || data.tablero || [];

    if (Array.isArray(arrayPreguntas) && arrayPreguntas.length > 0) {
      const esTresPorTres = arrayPreguntas.length <= 8;
      setTamano(esTresPorTres);
      
      const expectedCount = esTresPorTres ? PREGUNTAS_3X3 : PREGUNTAS_4X4;
      const mappedPreguntas = makeQuestions(expectedCount);

for (let i = 0; i < Math.min(arrayPreguntas.length, expectedCount); i++) {
        const q = arrayPreguntas[i];
        const textoPregunta = q.pregunta || q.enunciado || q.texto || '';
        let textoRespuesta = '';
        if (q.respuesta) {
          if (typeof q.respuesta === 'string') {
            textoRespuesta = q.respuesta;
          } else if (typeof q.respuesta === 'object') {
            textoRespuesta = q.respuesta.texto || q.respuesta.respuesta || q.respuesta.text || '';
          }
        } else {
          textoRespuesta = q.solucion || q.correcta || '';
        }

        mappedPreguntas[i] = {
          ...mappedPreguntas[i],
          pregunta: String(textoPregunta),
          respuesta: String(textoRespuesta)
        };
      }
      
      setPreguntas(mappedPreguntas);
    }
  };

  return (
    <form className="tbl-form" onSubmit={handleSubmit}>
       <GenerarIAModal
              tipoActividad="TABLERO"
              open={showIAModal}
              onClose={() => setShowIAModal(false)}
              onResult={handleIAResult}
            />
      {/* ── Datos básicos ─────────────────────────────────────── */}
      <div className="tbl-header">
        <div className="tbl-col">
          <label className="tbl-label">Título *</label>
          <input
            className="tbl-input"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Título del tablero"
            required
          />
          <label className="tbl-label">Descripción</label>
          <textarea
            className="tbl-textarea"
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            placeholder="Descripción opcional"
            rows={3}
          />
        </div>
        <div className="tbl-col">
          <label className="tbl-label">Puntuación *</label>
          <input
            className="tbl-input"
            type="number"
            min={1}
            value={puntuacion}
            onChange={(e) => setPuntuacion(e.target.value)}
            placeholder="Ej. 100"
            required
          />
          <label className="tbl-label tbl-label--check">
            <input
              type="checkbox"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            <span>Mostrar respuesta correcta al alumno</span>
          </label>
          <div>
            <button type="button" className="iam-trigger-btn" onClick={() => setShowIAModal(true)}>
              Generar con IA
            </button>
          </div>
        </div>
        
      </div>

      {/* ── Selector de tamaño ────────────────────────────────── */}
      <div className="tbl-section">
        <p className="tbl-section-title">Tamaño del tablero *</p>
        <div className="tbl-tamano-selector">
          {([true, false] as const).map((esTres) => {
            const dim = esTres ? 3 : 4;
            const numQ = esTres ? PREGUNTAS_3X3 : PREGUNTAS_4X4;
            const active = tamano === esTres;
            return (
              <button
                key={String(esTres)}
                type="button"
                className={`tbl-tamano-btn${active ? ' tbl-tamano-btn--active' : ''}`}
                onClick={() => handleTamanoChange(esTres)}
              >
                <div
                  className="tbl-board-preview"
                  style={{ gridTemplateColumns: `repeat(${dim}, 1fr)` }}
                >
                  {Array.from({ length: dim * dim }).map((_, idx) => {
                    const row = Math.floor(idx / dim);
                    const col = idx % dim;
                    return (
                      <div
                        key={`cell-${dim}-${row}-${col}`}
                        className={`tbl-board-cell${isCellDark(row, col) ? ' tbl-board-cell--dark' : ''}`}
                      />
                    );
                  })}
                </div>
                <span className="tbl-tamano-label">{dim}×{dim}</span>
                <span className="tbl-tamano-sub">{numQ} preguntas</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* ── Lista de preguntas ────────────────────────────────── */}
      {tamano !== null && (
        <div className="tbl-questions">
          <p className="tbl-section-title">Preguntas ({numPreguntas})</p>
          <div className="tbl-q-row tbl-q-row--header">
            <span />
            <span className="tbl-q-col-label">Enunciado de la pregunta</span>
            <span className="tbl-q-col-label">Respuesta correcta</span>
          </div>
          {preguntas.slice(0, numPreguntas).map((q, i) => (
            <div key={q.localKey} className="tbl-q-row">
              <span className="tbl-q-number">{i + 1}</span>
              <input
                className="tbl-input"
                value={q.pregunta ?? ''}
                onChange={(e) => updatePregunta(i, e.target.value)}
                placeholder={`Pregunta ${i + 1}`}
              />
              <input
                className="tbl-input tbl-respuesta-input"
                value={q.respuesta ?? ''}
                onChange={(e) => updateRespuesta(i, e.target.value)}
                placeholder="Respuesta"
              />
            </div>
          ))}
        </div>
      )}

      {error && <p className="tbl-error">{error}</p>}
      {success && <p className="tbl-success">{success}</p>}

      <div className="tbl-footer">
        <button
          type="submit"
          className="ca-btn-guardar"
          disabled={loading || tamano === null}
        >
          {loading ? 'Guardando...' : mode === 'create' ? 'Crear Tablero' : 'Guardar cambios'}
        </button>
      </div>
    </form>
  );
}
