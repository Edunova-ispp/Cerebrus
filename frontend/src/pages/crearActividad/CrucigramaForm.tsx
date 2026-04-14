import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import './CrucigramaForm.css';

export type CrucigramaFormMode = 'create' | 'edit';

export interface CrucigramaFormInitialPregunta {
    readonly id?: number;
    readonly pregunta: string;
    readonly respuesta?: string;
}

export interface CrucigramaFormInitialValues {
    readonly titulo: string;
    readonly descripcion: string;
    readonly puntuacion: number;
    readonly respVisible: boolean;
    readonly permitirReintento?: boolean;
    readonly temaId?: number;
    readonly preguntas?: CrucigramaFormInitialPregunta[];
    readonly preguntasYRespuestas?: Record<string, string>;
    readonly mostrarPuntuacion?: boolean;
    readonly encontrarRespuestaMaestro?: boolean;
}

interface Props {
    readonly mode?: CrucigramaFormMode;
    readonly crucigramaId?: number;
    readonly initialValues?: CrucigramaFormInitialValues;
    readonly temaIdProp?: string;
    readonly cursoIdProp?: string;
    readonly onDone?: () => void;
}

interface PreguntaLocal {
    pregunta: string;
    respuesta: string;
}

const MAX_PALABRAS = 10;
const ONLY_LETTERS_REGEX = /^\p{L}+$/u;

function sanitizeRespuesta(value: string): string {
    return value.replace(/[^\p{L}]/gu, '').toUpperCase();
}

export function CrucigramaForm({ mode = 'create', crucigramaId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
    const [titulo, setTitulo] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [puntuacion, setPuntuacion] = useState('');
    const [respVisible, setRespVisible] = useState(true);
    const [permitirReintento, setPermitirReintento] = useState(false);
    const [mostrarPuntuacion, setMostrarPuntuacion] = useState(false);
    const [encontrarRespuestaMaestro, setEncontrarRespuestaMaestro] = useState(false);
    const [preguntas, setPreguntas] = useState<PreguntaLocal[]>([{ pregunta: '', respuesta: '' }]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
    const params = useParams<{ id: string; temaId: string }>();
    const cursoId = cursoIdProp ?? params.id;
    const temaId = temaIdProp ?? params.temaId ?? (initialValues?.temaId != null ? String(initialValues.temaId) : undefined);

    const readErrorMessage = (value: unknown): string => {
        if (typeof value === 'object' && value !== null && 'message' in value) {
            const message = (value as { message?: unknown }).message;
            if (typeof message === 'string' && message.trim()) {
                return message;
            }
        }

        return 'Error del servidor al guardar';
    };

    const validate = (): string | null => {
        if (!titulo.trim()) return 'El título es requerido';
        if (titulo.trim().length > 25) return 'El título no puede exceder los 25 caracteres.';

        if (descripcion.trim().length > 1000) return 'La descripción no puede exceder los 1000 caracteres.';

        if (!temaId) return 'Falta el id del tema en la URL';
        if (Number.isNaN(Number.parseInt(temaId, 10))) return 'El id del tema no es válido';

        if (!puntuacion.trim()) return 'La puntuación es requerida';
        const puntuacionNum = Number.parseInt(puntuacion.trim(), 10);
        if (Number.isNaN(puntuacionNum)) return 'La puntuación debe ser un número válido';
        if (puntuacionNum <= 0) return 'La puntuación debe ser un número mayor a 0';
        if (puntuacionNum > 999999999) return 'La puntuación no puede exceder 999.999.999';

        const nonEmptyPairs = preguntas.filter((p) => p.pregunta.trim() || p.respuesta.trim());
        if (nonEmptyPairs.length === 0) return 'Debes completar al menos una pregunta y su respuesta.';
        if (preguntas.length > MAX_PALABRAS) return `No puedes añadir más de ${MAX_PALABRAS} palabras.`;

        for (let i = 0; i < preguntas.length; i++) {
            const clue = preguntas[i].pregunta.trim();
            const rawAnswer = preguntas[i].respuesta.trim();

            if (!clue && !rawAnswer) continue;
            if (!clue) return `La pista de la palabra ${i + 1} es requerida`;
            if (!rawAnswer) return `La palabra (respuesta) ${i + 1} es requerida`;

            const normalizedRespuesta = sanitizeRespuesta(rawAnswer);
            if (!normalizedRespuesta) return `La palabra (respuesta) ${i + 1} es requerida`;
            if (!ONLY_LETTERS_REGEX.test(normalizedRespuesta)) {
                return 'Las respuestas del crucigrama solo pueden contener letras.';
            }
        }

        if (mode === 'edit' && !crucigramaId) return 'Falta el id del crucigrama a editar';
        if (!cursoId) return 'Falta el id del curso en la URL';

        return null;
    };

    // ── Load initial values ───────────────────────────────────────────────
    useEffect(() => {
        if (!initialValues) return;
        setTitulo(initialValues.titulo || '');
        setDescripcion(initialValues.descripcion || '');
        setPuntuacion(String(initialValues.puntuacion) || '');
        setRespVisible(initialValues.respVisible !== undefined ? initialValues.respVisible : true);
        setPermitirReintento(Boolean(initialValues.permitirReintento));

        if (initialValues.preguntasYRespuestas && Object.keys(initialValues.preguntasYRespuestas).length > 0) {
            setPreguntas(
                Object.entries(initialValues.preguntasYRespuestas).map(([pista, palabra]) => ({
                    pregunta: pista,
                    respuesta: palabra,
                }))
            );
        } else if (initialValues.preguntas && initialValues.preguntas.length > 0) {
            setPreguntas(
                initialValues.preguntas.map(p => ({
                    pregunta: p.pregunta || '',
                    respuesta: p.respuesta || '',
                }))
            );
        }
    }, [initialValues]);

    // ── Handlers ──────────────────────────────────────────────────────────
    const handlePreguntaChange = (index: number, field: keyof PreguntaLocal, value: string) => {
        const updated = [...preguntas];
        const normalizedValue = field === 'respuesta' ? sanitizeRespuesta(value) : value;
        updated[index] = { ...updated[index], [field]: normalizedValue };
        setPreguntas(updated);
    };

    const addPregunta = () => {
        if (preguntas.length < MAX_PALABRAS) {
            setPreguntas([...preguntas, { pregunta: '', respuesta: '' }]);
        }
    };

    const removePregunta = (index: number) => {
        if (preguntas.length > 1) {
            setPreguntas(preguntas.filter((_, i) => i !== index));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setLoading(true);
        setError('');

        try {
            const mapaPreguntas: Record<string, string> = {};
            preguntas.forEach(p => {
                if (p.pregunta.trim() && p.respuesta.trim()) {
                    const normalizedRespuesta = sanitizeRespuesta(p.respuesta.trim());
                    mapaPreguntas[p.pregunta.trim()] = normalizedRespuesta;
                }
            });

            const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

            const payload = {
                titulo: titulo.trim(),
                descripcion: descripcion.trim(),
                temaId: Number(temaId) || initialValues?.temaId,
                puntuacion: Number.parseInt(puntuacion.trim(), 10),
                respVisible: Boolean(respVisible),
                permitirReintento: Boolean(permitirReintento),
                mostrarPuntuacion: Boolean(mostrarPuntuacion),
                encontrarRespuestaMaestro: Boolean(encontrarRespuestaMaestro),
                encontrarRespuestaAlumno: false,
                preguntasYRespuestas: mapaPreguntas,
            };

            const url =
                mode === 'edit'
                    ? `${apiBase}/api/generales/crucigrama/${crucigramaId}`
                    : `${apiBase}/api/generales/crucigrama`;

            const response = await apiFetch(url, {
                method: mode === 'edit' ? 'PUT' : 'POST',
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const errorData: unknown = await response.json().catch(() => null);
                throw new Error(readErrorMessage(errorData));
            }

            if (onDone) onDone(); else navigate(`/cursos/${cursoId}/temas`);
        } catch (err: unknown) {
            if (err instanceof Error) {
                setError(err.message || 'No se pudo conectar con el servidor.');
            } else {
                setError('No se pudo conectar con el servidor.');
            }
        } finally {
            setLoading(false);
        }
    };

    // ── Render ────────────────────────────────────────────────────────────
    return (
        <form onSubmit={handleSubmit} className="cf-form">

            {/* Error */}
            {error && (
                <div className="cf-error">
                    ¡Atención! {error}
                </div>
            )}

            {/* ── Datos generales ── */}
            <div className="cf-card">
                <div className="cf-grid-2">
                    <div>
                        <label className="cf-label" htmlFor="cf-titulo">Título del crucigrama</label>
                        <input
                            id="cf-titulo"
                            type="text"
                            className="cf-input"
                            value={titulo}
                            onChange={e => setTitulo(e.target.value)}
                            required
                            placeholder="Ej: Repaso de Historia"
                        />

                        <label className="cf-label" htmlFor="cf-desc">Descripción</label>
                        <textarea
                            id="cf-desc"
                            className="cf-textarea"
                            value={descripcion}
                            onChange={e => setDescripcion(e.target.value)}
                            rows={3}
                            placeholder="Breve explicación del crucigrama..."
                        />
                    </div>

                    <div>
                        <label className="cf-label" htmlFor="cf-punt">Puntuación total</label>
                        <input
                            id="cf-punt"
                            type="number"
                            className="cf-input"
                            value={puntuacion}
                            min={1}
                            onChange={e => setPuntuacion(e.target.value)}
                            required
                        />

                        <label
                            className="cf-checkbox-row"
                            htmlFor="cf-visible"
                        >
                            <input
                                id="cf-visible"
                                type="checkbox"
                                checked={respVisible}
                                onChange={e => setRespVisible(e.target.checked)}
                            />
                            <span className="cf-checkbox-label">Mostrar solución al finalizar</span>
                        </label>
                        <label
                            className="cf-checkbox-row"
                            htmlFor="cf-reintento"
                        >
                            <input
                                id="cf-reintento"
                                type="checkbox"
                                checked={permitirReintento}
                                onChange={e => setPermitirReintento(e.target.checked)}
                            />
                            <span className="cf-checkbox-label">Permitir reintentos</span>
                        </label>
                        <label
                            className="cf-checkbox-row"
                            htmlFor="cf-mostrar-puntuacion"
                        >
                            <input
                                id="cf-mostrar-puntuacion"
                                type="checkbox"
                                checked={mostrarPuntuacion}
                                onChange={e => setMostrarPuntuacion(e.target.checked)}
                            />
                            <span className="cf-checkbox-label">Mostrar puntuación</span>
                        </label>
                        <label
                            className="cf-checkbox-row"
                            htmlFor="cf-mostrar-resp-maest"
                        >
                            <input
                                id="cf-mostrar-resp-maest"
                                type="checkbox"
                                checked={encontrarRespuestaMaestro}
                                onChange={e => setEncontrarRespuestaMaestro(e.target.checked)}
                            />
                            <span className="cf-checkbox-label">Mostrar respuesta correcta</span>
                        </label>
                    </div>
                </div>
            </div>

            {/* ── Palabras y pistas ── */}
            <div className="cf-card">
                <h3 className="cf-section-title">
                    Palabras y pistas
                    <span>{preguntas.length} / {MAX_PALABRAS}</span>
                </h3>

                {preguntas.map((p, index) => (
                    <div key={index} className="cf-word-row">
                        <div className="cf-word-col-clue">
                            <label className="cf-label">Pista (pregunta que verá el alumno)</label>
                            <input
                                className="cf-input"
                                placeholder="Ej: Capital de España"
                                value={p.pregunta}
                                onChange={e => handlePreguntaChange(index, 'pregunta', e.target.value)}
                                required
                            />
                        </div>
                        <div className="cf-word-col-word">
                            <label className="cf-label">Palabra (respuesta)</label>
                            <input
                                className="cf-input cf-input-upper"
                                placeholder="MADRID"
                                value={p.respuesta}
                                inputMode="text"
                                pattern="[A-Za-zÁÉÍÓÚáéíóúÑñÜü]+"
                                onChange={e => handlePreguntaChange(index, 'respuesta', e.target.value)}
                                required
                            />
                        </div>
                        {preguntas.length > 1 && (
                            <button
                                type="button"
                                className="cf-btn-remove"
                                onClick={() => removePregunta(index)}
                                title="Eliminar palabra"
                            >
                                ✕
                            </button>
                        )}
                    </div>
                ))}

                {preguntas.length < MAX_PALABRAS && (
                    <button type="button" className="cf-btn-add" onClick={addPregunta}>
                        + Añadir palabra
                    </button>
                )}
            </div>

            {/* ── Submit ── */}
            <button
                className="cf-btn-submit"
                type="submit"
                disabled={loading}
            >
                {loading
                    ? 'PROCESANDO...'
                    : mode === 'edit'
                        ? 'GUARDAR'
                        : 'GUARDAR '}
            </button>
        </form>
    );
}