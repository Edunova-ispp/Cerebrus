import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
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
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly version: number;
  readonly preguntas?: readonly TestFormInitialPregunta[];
}

interface QuestionOption {
  id?: number;
  text: string;
  correcta: boolean;
}

interface Question {
  id?: number;
  text: string;
  options: QuestionOption[];
}

interface Props {
  readonly mode?: TestFormMode;
  readonly generalId?: number;
  readonly initialValues?: TestFormInitialValues;
}

function makeEmptyOption(): QuestionOption {
  return { text: '', correcta: false };
}

function makeEmptyQuestion(): Question {
  return { text: '', options: [makeEmptyOption(), makeEmptyOption()] };
}

export function TestForm({ mode = 'create', generalId, initialValues }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [imagen, setImagen] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [questions, setQuestions] = useState<Question[]>([makeEmptyQuestion()]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Tracks original server-loaded preguntas for deletion detection in edit mode
  const originalQuestionsRef = useRef<TestFormInitialPregunta[]>([]);

  const navigate = useNavigate();
  const { id: cursoId, temaId } = useParams<{ id: string; temaId: string }>();

  useEffect(() => {
    if (!initialValues) return;
    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion ?? ''));
    setImagen(initialValues.imagen ?? '');
    setRespVisible(Boolean(initialValues.respVisible));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');

    if (initialValues.preguntas && initialValues.preguntas.length > 0) {
      originalQuestionsRef.current = [...initialValues.preguntas];
      setQuestions(
        initialValues.preguntas.map((p) => ({
          id: p.id,
          text: p.pregunta,
          options: p.respuestas.map((r) => ({
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

  const setCorrect = (qi: number, oi: number) =>
    setQuestions((prev) =>
      prev.map((q, i) =>
        i === qi
          ? { ...q, options: q.options.map((opt, j) => ({ ...opt, correcta: j === oi })) }
          : q,
      ),
    );

  // ── Validation ───────────────────────────────────────────────────────────

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';

    const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
    if (Number.isNaN(puntuacionNum)) return 'La puntuación debe ser un número válido';

    if (!temaId) return 'Falta el id del tema en la URL';
    if (Number.isNaN(Number.parseInt(temaId, 10))) return 'El id del tema no es válido';
    if (!cursoId) return 'Falta el id del curso en la URL';

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
        return `Marca la respuesta correcta en la pregunta ${qi + 1}`;
    }

    if (mode === 'edit' && !generalId) return 'Falta el id de la actividad a editar';

    return null;
  };

  // ── Submit ────────────────────────────────────────────────────────────────

  const handleSubmit = async (e: React.FormEvent) => {
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

      navigate(`/cursos/${cursoId}/temas`);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Error guardando el test';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <form onSubmit={handleSubmit} className="tf-form">
      {error && <p className="ca-text tf-error">{error}</p>}

      {/* ── TOP: Metadata ── */}
      <div className="ca-contenedor-blanco tf-header">
        <div className="tf-col">
          <div>
            <label className="ca-text" htmlFor="tf-titulo">
              Título
            </label>
            <input
              type="text"
              id="tf-titulo"
              value={titulo}
              onChange={(e) => setTitulo(e.target.value)}
              style={{ width: '100%' }}
            />
          </div>

          <div>
            <label className="ca-text" htmlFor="tf-descripcion">
              Descripción
            </label>
            <textarea
              id="tf-descripcion"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              rows={3}
              style={{ width: '100%' }}
            />
          </div>

          <div>
            <label className="ca-text" htmlFor="tf-imagen">
              URL de imagen (opcional)
            </label>
            <input
              type="url"
              id="tf-imagen"
              value={imagen}
              onChange={(e) => setImagen(e.target.value)}
              placeholder="https://..."
              style={{ width: '100%' }}
            />
            {imagen.trim() && (
              <img
                src={imagen.trim()}
                alt="Preview"
                className="tf-img-preview"
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = 'none';
                }}
                onLoad={(e) => {
                  (e.target as HTMLImageElement).style.display = 'block';
                }}
              />
            )}
          </div>
        </div>

        <div className="tf-col">
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <label className="ca-text" htmlFor="tf-puntuacion" style={{ whiteSpace: 'nowrap' }}>
              Puntuación
            </label>
            <input
              type="number"
              id="tf-puntuacion"
              value={puntuacion}
              onChange={(e) => setPuntuacion(e.target.value)}
              style={{ width: 90 }}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="tf-respVisible"
              checked={respVisible}
              onChange={(e) => setRespVisible(e.target.checked)}
            />
            <label className="ca-text" htmlFor="tf-respVisible">
              Correcciones visibles
            </label>
          </div>

          {respVisible && (
            <div>
              <label className="ca-text" htmlFor="tf-comentarios">
                Comentarios
              </label>
              <input
                type="text"
                id="tf-comentarios"
                value={comentariosRespVisible}
                onChange={(e) => setComentariosRespVisible(e.target.value)}
                style={{ width: '100%' }}
              />
            </div>
          )}
        </div>
      </div>

      {/* ── BOTTOM: Questions ── */}
      <div
        className="ca-contenedor-blanco tf-questions"
        style={{ marginTop: 16, flexDirection: 'column', alignItems: 'stretch' }}
      >
          <p className="ca-ordenacion-help" style={{ marginTop: 0, marginBottom: 12 }}>
            Añade las preguntas y opciones. Marca cuál es la correcta con{' '}
            <strong>✓</strong>. Las opciones se mostrarán en orden aleatorio al alumno.
          </p>

          {questions.map((q, qi) => (
            <div key={qi} className="tf-question-block">
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
                    key={oi}
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
                      onClick={() => setCorrect(qi, oi)}
                      title={opt.correcta ? 'Respuesta correcta' : 'Marcar como correcta'}
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

              <button type="button" className="tf-btn-add-option" onClick={() => addOption(qi)}>
                + Añadir opción
              </button>
            </div>
          ))}

          <button type="button" className="tf-btn-add-question" onClick={addQuestion}>
            + Añadir pregunta
          </button>
        </div>

      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 16 }}>
        <button className="ca-btn-guardar" type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
