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
    readonly temaId?: number;
    readonly preguntas?: CrucigramaFormInitialPregunta[];
    readonly preguntasYRespuestas?: Record<string, string>;
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

export function CrucigramaForm({ mode = 'create', crucigramaId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
    const [titulo, setTitulo] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [puntuacion, setPuntuacion] = useState('');
    const [respVisible, setRespVisible] = useState(true);
    const [preguntas, setPreguntas] = useState<PreguntaLocal[]>([{ pregunta: '', respuesta: '' }]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
    const params = useParams<{ id: string; temaId: string }>();
    const cursoId = cursoIdProp ?? params.id;
    const temaId = temaIdProp ?? params.temaId ?? (initialValues?.temaId != null ? String(initialValues.temaId) : undefined);

    // ── Load initial values ───────────────────────────────────────────────
    useEffect(() => {
        if (!initialValues) return;
        setTitulo(initialValues.titulo || '');
        setDescripcion(initialValues.descripcion || '');
        setPuntuacion(String(initialValues.puntuacion) || '');
        setRespVisible(initialValues.respVisible !== undefined ? initialValues.respVisible : true);

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
        updated[index] = { ...updated[index], [field]: value };
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
        setLoading(true);
        setError('');

        try {
            const mapaPreguntas: Record<string, string> = {};
            preguntas.forEach(p => {
                if (p.pregunta.trim() && p.respuesta.trim()) {
                    mapaPreguntas[p.pregunta.trim()] = p.respuesta.trim().toUpperCase();
                }
            });

            if (Object.keys(mapaPreguntas).length === 0) {
                throw new Error('Debes completar al menos una pregunta y su respuesta.');
            }

            if (puntuacion <= 0) {
                throw new Error('La puntuación debe ser un número mayor a 0');
            }

            const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

            const payload = {
                titulo: titulo.trim(),
                descripcion: descripcion.trim(),
                temaId: Number(temaId) || initialValues?.temaId,
                puntuacion: Number(puntuacion),
                respVisible: Boolean(respVisible),
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
                const errorData = await response.json().catch(() => ({}));
                throw new Error((errorData as any).message || 'Error del servidor al guardar');
            }

            if (onDone) onDone(); else navigate(`/cursos/${cursoId}/temas`);
        } catch (err: any) {
            setError(err.message || 'No se pudo conectar con el servidor.');
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