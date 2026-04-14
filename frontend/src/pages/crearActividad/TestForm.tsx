import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import GenerarIAModal from '../../components/GenerarIAModal/GenerarIAModal';
import './TestForm.css';

export type TestFormMode = 'create' | 'edit';

export interface TestFormInitialPregunta {
  readonly id: number;
  readonly pregunta: string;
  readonly respuestas: readonly {
    readonly id: number;
    readonly respuesta: string;
    readonly correcta: boolean;
  }[];
}

export interface TestFormInitialValues {
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly imagen: string | null;
  readonly respVisible: boolean;
  readonly permitirReintento?: boolean;
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
  readonly encontrarRespuestaAlumno?: boolean;
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly version: number;
  readonly temaId?: number;
  readonly preguntas?: readonly TestFormInitialPregunta[];
}

interface QuestionOption {
  localKey: string;
  id?: number;
  text: string;
  correcta: boolean;
}

interface Question {
  localKey: string;
  id?: number;
  text: string;
  options: QuestionOption[];
}

function makeLocalKey(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

interface Props {
  readonly mode?: TestFormMode;
  readonly generalId?: number;
  readonly initialValues?: TestFormInitialValues;
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly onDone?: () => void;
}

function makeEmptyOption(): QuestionOption {
  return { localKey: makeLocalKey(), text: '', correcta: false };
}

function makeEmptyQuestion(): Question {
  return { localKey: makeLocalKey(), text: '', options: [makeEmptyOption(), makeEmptyOption()] };
}

export function TestForm({ mode = 'create', generalId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [imagen, setImagen] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [permitirReintento, setPermitirReintento] = useState(false);
  const [mostrarPuntuacion, setMostrarPuntuacion] = useState(false);
  const [encontrarRespuestaMaestro, setEncontrarRespuestaMaestro] = useState(false);
  const [encontrarRespuestaAlumno, setEncontrarRespuestaAlumno] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [questions, setQuestions] = useState<Question[]>([makeEmptyQuestion()]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [iaModalOpen, setIaModalOpen] = useState(false);

  // Tracks original server-loaded preguntas for deletion detection in edit mode
  const originalQuestionsRef = useRef<TestFormInitialPregunta[]>([]);

  const navigate = useNavigate();
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const temaId = temaIdProp ?? params.temaId ?? (initialValues?.temaId != null ? String(initialValues.temaId) : undefined);

  useEffect(() => {
    if (!initialValues) return;
    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion ?? ''));
    setImagen(initialValues.imagen ?? '');
    setRespVisible(Boolean(initialValues.respVisible));
    setPermitirReintento(Boolean(initialValues.permitirReintento));
    setMostrarPuntuacion(Boolean(initialValues.mostrarPuntuacion ?? false));
    setEncontrarRespuestaMaestro(Boolean(initialValues.encontrarRespuestaMaestro ?? false));
    setEncontrarRespuestaAlumno(Boolean(initialValues.encontrarRespuestaAlumno ?? false));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');

    if (initialValues.preguntas && initialValues.preguntas.length > 0) {
      originalQuestionsRef.current = [...initialValues.preguntas];
      setQuestions(
        initialValues.preguntas.map((p) => ({
          localKey: makeLocalKey(),
          id: p.id,
          text: p.pregunta,
          options: p.respuestas.map((r) => ({
            localKey: makeLocalKey(),
            id: r.id,
            text: r.respuesta,
            correcta: r.correcta,
          })),
        })),
      );
    }
  }, [initialValues]);

  // ── Question / option state helpers ──────────────────────────────────────

  const addQuestion = () =>
    setQuestions((prev) => [...prev, makeEmptyQuestion()]);

  const removeQuestion = (qi: number) =>
    setQuestions((prev) => prev.filter((_, i) => i !== qi));

  const updateQuestionText = (qi: number, text: string) =>
    setQuestions((prev) => prev.map((q, i) => (i === qi ? { ...q, text } : q)));

  const addOption = (qi: number) =>
    setQuestions((prev) =>
      prev.map((q, i) =>
        i === qi ? { ...q, options: [...q.options, makeEmptyOption()] } : q,
      ),
    );

  const removeOption = (qi: number, oi: number) =>
    setQuestions((prev) =>
      prev.map((q, i) =>
        i === qi ? { ...q, options: q.options.filter((_, j) => j !== oi) } : q,
      ),
    );

  const updateOptionText = (qi: number, oi: number, text: string) =>
    setQuestions((prev) =>
      prev.map((q, i) =>
        i === qi
          ? { ...q, options: q.options.map((opt, j) => (j === oi ? { ...opt, text } : opt)) }
          : q,
      ),
    );

  const toggleCorrect = (qi: number, oi: number) =>
    setQuestions((prev) =>
      prev.map((q, i) =>
        i === qi
          ? {
              ...q,
              options: q.options.map((opt, j) =>
                j === oi ? { ...opt, correcta: !opt.correcta } : opt,
              ),
            }
          : q,
      ),
    );

  // ── Validation ───────────────────────────────────────────────────────────

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';

    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(puntuacionNum)) return 'La puntuación debe ser un número válido';
    if (puntuacionNum <= 0) return 'La puntuación debe ser un número mayor a 0';

    if (!temaId) return 'Falta el id del tema en la URL';
    if (Number.isNaN(Number.parseInt(temaId, 10))) return 'El id del tema no es válido';
    if (!cursoId) return 'Falta el id del curso en la URL';

    if (descripcion.trim().length > 500) return 'La descripción no puede exceder los 500 caracteres.';
    if (titulo.trim().length > 25) return 'El título no puede exceder los 25 caracteres.';

    if (questions.length === 0) return 'Añade al menos una pregunta';

    for (let qi = 0; qi < questions.length; qi++) {
      const q = questions[qi];
      if (!q.text.trim()) return `La pregunta ${qi + 1} no tiene texto`;
      if (q.options.length < 2)
        return `La pregunta ${qi + 1} debe tener al menos 2 opciones`;
      for (let oi = 0; oi < q.options.length; oi++) {
        if (!q.options[oi].text.trim())
          return `La opción ${oi + 1} de la pregunta ${qi + 1} está vacía`;
      }
      if (!q.options.some((o) => o.correcta))
        return `Marca al menos una respuesta correcta en la pregunta ${qi + 1}`;
    }

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
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      if (mode === 'create') {
        // Step 1 – create the General (test) shell with no questions
        const generalRes = await apiFetch(`${apiBase}/api/generales/test`, {
          method: 'POST',
          body: JSON.stringify({
            titulo: titulo.trim(),
            descripcion: descripcion.trim() || null,
            puntuacion: puntuacionNum,
            imagen: imagen.trim() || null,
            tema: { id: temaIdNum },
            respVisible,
            permitirReintento,
            mostrarPuntuacion,
            encontrarRespuestaMaestro,
            encontrarRespuestaAlumno,
            comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || null) : null,
            preguntas: [],
          }),
        });
        const gId = (await generalRes.json()) as number;

        // Step 2 – create questions sequentially (each must reference the general id)
        for (const q of questions) {
          const pregRes = await apiFetch(`${apiBase}/api/preguntas`, {
            method: 'POST',
            body: JSON.stringify({
              pregunta: q.text.trim(),
              imagen: null,
              actividadId: gId,
            }),
          });
          const pregId = (await pregRes.json()) as number;

          // Step 3 – create options for this question in parallel
          await Promise.all(
            q.options.map((opt) =>
              apiFetch(`${apiBase}/api/respuestas`, {
                method: 'POST',
                body: JSON.stringify({
                  respuesta: opt.text.trim(),
                  imagen: null,
                  correcta: opt.correcta,
                  pregunta: { id: pregId },
                }),
              }),
            ),
          );
        }
      } else {
        // Edit – metadata
        await apiFetch(`${apiBase}/api/generales/update/${generalId}`, {
          method: 'PUT',
          body: JSON.stringify({
            titulo: titulo.trim(),
            descripcion: descripcion.trim() || null,
            puntuacion: puntuacionNum,
            imagen: imagen.trim() || null,
            tema: { id: temaIdNum },
            respVisible,
            permitirReintento,
            mostrarPuntuacion,
            encontrarRespuestaMaestro,
            encontrarRespuestaAlumno,
            comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || '') : '',
            posicion: initialValues?.posicion ?? 0,
            version: initialValues?.version ?? 1,
          }),
        });

        // Edit – questions: delete removed, update existing, create new
        const currentQuestionIds = new Set(questions.filter((q) => q.id).map((q) => q.id!));
        for (const orig of originalQuestionsRef.current) {
          if (!currentQuestionIds.has(orig.id)) {
            await apiFetch(`${apiBase}/api/preguntas/delete/${orig.id}`, { method: 'DELETE' });
          }
        }

        for (const q of questions) {
          if (q.id) {
            // Existing question – update text
            await apiFetch(`${apiBase}/api/preguntas/update/${q.id}`, {
              method: 'PUT',
              body: JSON.stringify({ pregunta: q.text.trim(), imagen: null }),
            });

            const origQ = originalQuestionsRef.current.find((o) => o.id === q.id);
            const currentOptIds = new Set(q.options.filter((o) => o.id).map((o) => o.id!));

            // Delete removed options
            for (const origR of origQ?.respuestas ?? []) {
              if (!currentOptIds.has(origR.id)) {
                await apiFetch(`${apiBase}/api/respuestas/delete/${origR.id}`, { method: 'DELETE' });
              }
            }

            // Update existing / create new options
            await Promise.all(
              q.options.map((opt) => {
                if (opt.id) {
                  return apiFetch(`${apiBase}/api/respuestas/update/${opt.id}`, {
                    method: 'PUT',
                    body: JSON.stringify({
                      respuesta: opt.text.trim(),
                      imagen: null,
                      correcta: opt.correcta,
                      pregunta: { id: q.id },
                    }),
                  });
                }
                return apiFetch(`${apiBase}/api/respuestas`, {
                  method: 'POST',
                  body: JSON.stringify({
                    respuesta: opt.text.trim(),
                    imagen: null,
                    correcta: opt.correcta,
                    pregunta: { id: q.id },
                  }),
                });
              }),
            );
          } else {
            // New question – create then create all its options
            const pregRes = await apiFetch(`${apiBase}/api/preguntas`, {
              method: 'POST',
              body: JSON.stringify({ pregunta: q.text.trim(), imagen: null, actividadId: generalId }),
            });
            const newPregId = (await pregRes.json()) as number;
            await Promise.all(
              q.options.map((opt) =>
                apiFetch(`${apiBase}/api/respuestas`, {
                  method: 'POST',
                  body: JSON.stringify({
                    respuesta: opt.text.trim(),
                    imagen: null,
                    correcta: opt.correcta,
                    pregunta: { id: newPregId },
                  }),
                }),
              ),
            );
          }
        }
      }

      if (onDone) onDone(); else navigate(`/cursos/${cursoId}`);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Error guardando el test';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────

  const handleIAResult = (data: Record<string, unknown>) => {
    if (data.titulo) setTitulo(data.titulo as string);
    if (data.descripcion) setDescripcion(data.descripcion as string);

    const preguntas = data.preguntas as { enunciado: string; opciones: { texto: string; correcta: boolean }[] }[] | undefined;
    if (preguntas && Array.isArray(preguntas)) {
      setQuestions(
        preguntas.map((p) => ({
          localKey: makeLocalKey(),
          text: p.enunciado,
          options: (p.opciones ?? []).map((o) => ({
            localKey: makeLocalKey(),
            text: o.texto,
            correcta: Boolean(o.correcta),
          })),
        })),
      );
    }
  };

  return (
    <form onSubmit={handleSubmit} className="tf-form">
      <GenerarIAModal
        tipoActividad="TEST"
        open={iaModalOpen}
        onClose={() => setIaModalOpen(false)}
        onResult={handleIAResult}
      />

      {/* ── TOP: Metadata ── */}
      <div className="tf-header">
        <div className="tf-col">
          <div>
            <label className="tf-label" htmlFor="tf-titulo">Título *</label>
            <input
              type="text"
              id="tf-titulo"
              className="tf-input"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              placeholder="Título del test"
              required
            />
          </div>

          <div>
            <label className="tf-label" htmlFor="tf-descripcion">Descripción</label>
            <textarea
              id="tf-descripcion"
              className="tf-input"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              rows={3}
              style={{ resize: 'vertical' }}
              placeholder="Descripción opcional"
            />
          </div>

          <div>
            <label className="tf-label" htmlFor="tf-imagen">URL de imagen (opcional)</label>
            <input
              type="url"
              id="tf-imagen"
              className="tf-input"
              value={imagen}
              onChange={(e) => setImagen(e.target.value)}
              placeholder="https://..."
            />
            {imagen.trim() && (
              <img
                src={imagen.trim()}
                alt="Preview"
                className="tf-img-preview"
                onError={(e) => { (e.target as HTMLImageElement).style.display = 'none'; }}
                onLoad={(e) => { (e.target as HTMLImageElement).style.display = 'block'; }}
              />
            )}
          </div>
        </div>

        <div className="tf-col">
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 80 }}>
            <div>
              <label className="tf-label" htmlFor="tf-puntuacion">Puntuación *</label>
              <input
                type="number"
                id="tf-puntuacion"
                className="tf-input tf-input-sm"
                value={puntuacion}
                onChange={(e) => setPuntuacion(e.target.value)}
                min="1"
                required
              />
            </div>
            <button type="button" className="iam-trigger-btn" onClick={() => setIaModalOpen(true)}>
              Generar con IA
            </button>
          </div>

          <label className="tf-check-label">
            <input
              type="checkbox"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            <span>Mostrar correcciones al alumno</span>
          </label>

          <label className="tf-check-label">
            <input
              type="checkbox"
              checked={permitirReintento}
              onChange={(e) => setPermitirReintento(e.target.checked)}
            />
            <span>Permitir reintentos</span>
          </label>

          <label className="tf-check-label">
            <input
              type="checkbox"
              checked={mostrarPuntuacion}
              onChange={(e) => setMostrarPuntuacion(e.target.checked)}
            />
            <span>Mostrar puntuación</span>
          </label>

          <label className="tf-check-label">
            <input
              type="checkbox"
              checked={encontrarRespuestaMaestro}
              onChange={(e) => setEncontrarRespuestaMaestro(e.target.checked)}
            />
            <span>Mostrar respuesta correcta</span>
          </label>

          <label className="tf-check-label">
            <input
              type="checkbox"
              checked={encontrarRespuestaAlumno}
              onChange={(e) => setEncontrarRespuestaAlumno(e.target.checked)}
            />
            <span>Mostrar mi respuesta</span>
          </label>

          {respVisible && (
            <div>
              <label className="tf-label" htmlFor="tf-comentarios">Comentarios</label>
              <input
                type="text"
                id="tf-comentarios"
                className="tf-input"
                value={comentariosRespVisible}
                onChange={(e) => setComentariosRespVisible(e.target.value)}
              />
            </div>
          )}
        </div>
      </div>

      <div className="tf-questions">
          <p className="tf-help">
            Añade las preguntas y opciones. Marca todas las correctas con <strong>✓</strong>. Las opciones se mostrarán en orden aleatorio al alumno.
          </p>

          {questions.map((q, qi) => (
            <div key={q.localKey} className="tf-question-block">
              <div className="tf-question-header">
                <span className="tf-question-label">Pregunta {qi + 1}</span>
                {questions.length > 1 && (
                  <button
                    type="button"
                    className="tf-btn-remove-question"
                    onClick={() => removeQuestion(qi)}
                    title="Eliminar pregunta"
                  >
                    ✕
                  </button>
                )}
              </div>

              <input
                type="text"
                className="tf-question-input"
                placeholder={`Escribe la pregunta ${qi + 1}...`}
                value={q.text}
                onChange={(e) => updateQuestionText(qi, e.target.value)}
              />

              <div className="tf-options">
                {q.options.map((opt, oi) => (
                  <div
                    key={opt.localKey}
                    className={`tf-option${opt.correcta ? ' tf-option--correct' : ''}`}
                  >
                    <span className="tf-option-letter">{String.fromCharCode(65 + oi)}.</span>
                    <input
                      type="text"
                      className="tf-option-input"
                      placeholder={`Opción ${String.fromCharCode(65 + oi)}`}
                      value={opt.text}
                      onChange={(e) => updateOptionText(qi, oi, e.target.value)}
                    />
                    <button
                      type="button"
                      className={`tf-btn-correct${opt.correcta ? ' tf-btn-correct--active' : ''}`}
                      onClick={() => toggleCorrect(qi, oi)}
                      title={opt.correcta ? 'Respuesta correcta (clic para desmarcar)' : 'Marcar como correcta'}
                    >
                      ✓
                    </button>
                    {q.options.length > 2 && (
                      <button
                        type="button"
                        className="tf-btn-remove-option"
                        onClick={() => removeOption(qi, oi)}
                        title="Eliminar opción"
                      >
                        ✕
                      </button>
                    )}
                  </div>
                ))}
              </div>

              {q.options.length < 26 && (
                <button type="button" className="tf-btn-add-option" onClick={() => addOption(qi)}>
                  + Añadir opción
                </button>
              )}
            </div>
          ))}

          {questions.length < 100 && (
            <button type="button" className="tf-btn-add-question" onClick={addQuestion}>
            + Añadir pregunta
            </button>
          )}
        </div>

      <div className="ca-form-footer">
        <div className="tf-footer-stack">
          <button className="ca-btn-guardar" type="submit" disabled={loading}>
            {loading ? 'Guardando...' : 'Guardar'}
          </button>
          {error && <p className="ca-text tf-error" style={{ color: '#c0392b' }}>
            {error}
          </p>}
        </div>
      </div>
    </form>
  );
}
