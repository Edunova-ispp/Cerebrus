import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import GenerarIAModal from '../../components/GenerarIAModal/GenerarIAModal';
import './TestForm.css';

export type ClasificacionFormMode = 'create' | 'edit';

export interface ClasificacionFormInitialPregunta {
  readonly id: number;
  readonly pregunta: string;
  readonly respuestas: readonly {
    readonly id: number;
    readonly respuesta: string;
  }[];
}

export interface ClasificacionFormInitialValues {
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly respVisible: boolean;
  readonly comentariosRespVisible: string | null;
  readonly posicion: number;
  readonly version: number;
  readonly temaId?: number;
  readonly preguntas?: readonly ClasificacionFormInitialPregunta[];
}

interface RespuestaOption {
  localKey: string;
  id?: number;
  text: string;
}

interface Pregunta {
  localKey: string;
  id?: number;
  text: string;
  respuestas: RespuestaOption[];
}

function makeLocalKey(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

interface Props {
  readonly mode?: ClasificacionFormMode;
  readonly clasificacionId?: number;
  readonly initialValues?: ClasificacionFormInitialValues;
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly onDone?: () => void;
}

function makeEmptyRespuesta(): RespuestaOption {
  return { localKey: makeLocalKey(), text: '' };
}

function makeEmptyPregunta(): Pregunta {
  return { localKey: makeLocalKey(), text: '', respuestas: [makeEmptyRespuesta()] };
}

export function ClasificacionForm({ mode = 'create', clasificacionId, initialValues, temaIdProp, cursoIdProp, onDone }: Props) {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntuacion, setPuntuacion] = useState('');
  const [respVisible, setRespVisible] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [preguntas, setPreguntas] = useState<Pregunta[]>([makeEmptyPregunta()]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
const [showIAModal, setShowIAModal] = useState(false);

  const originalPreguntasRef = useRef<ClasificacionFormInitialPregunta[]>([]);
  const navigate = useNavigate();
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const temaId = temaIdProp ?? params.temaId ?? (initialValues?.temaId != null ? String(initialValues.temaId) : undefined);

  useEffect(() => {
    if (!initialValues) return;
    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntuacion(String(initialValues.puntuacion ?? ''));
    setRespVisible(Boolean(initialValues.respVisible));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');

    if (initialValues.preguntas && initialValues.preguntas.length > 0) {
      originalPreguntasRef.current = JSON.parse(JSON.stringify(initialValues.preguntas));
      setPreguntas(
        initialValues.preguntas.map((p) => ({
          localKey: makeLocalKey(),
          id: p.id,
          text: p.pregunta,
          respuestas: p.respuestas.map((r) => ({
            localKey: makeLocalKey(),
            id: r.id,
            text: r.respuesta,
          })),
        }))
      );
    }
  }, [initialValues]);

  const addPregunta = () => setPreguntas([...preguntas, makeEmptyPregunta()]);

  const removePregunta = (index: number) => {
    if (preguntas.length > 2) {
      setPreguntas(preguntas.filter((_, i) => i !== index));
      setError('');
    } else {
      setError('Debe haber al menos 2 categorías.');
    }
  };

  const updatePregunta = (index: number, updates: Partial<Pregunta>) => {
    const updated = [...preguntas];
    updated[index] = { ...updated[index], ...updates };
    setPreguntas(updated);
  };

  const addRespuesta = (pIdx: number) => {
    const updated = [...preguntas];
    updated[pIdx].respuestas = [...updated[pIdx].respuestas, makeEmptyRespuesta()];
    setPreguntas(updated);
  };

  const removeRespuesta = (pIdx: number, rIdx: number) => {
    const updated = [...preguntas];
    if (updated[pIdx].respuestas.length > 1) {
      updated[pIdx].respuestas = updated[pIdx].respuestas.filter((_, i) => i !== rIdx);
      setPreguntas(updated);
    } else {
      setError('No puede haber menos de un elemento por categoría.');
    }
  };

  const updateRespuesta = (pIdx: number, rIdx: number, updates: Partial<RespuestaOption>) => {
    const updated = [...preguntas];
    updated[pIdx].respuestas[rIdx] = { ...updated[pIdx].respuestas[rIdx], ...updates };
    setPreguntas(updated);
  };

  const validate = (): string | null => {
    if (!titulo.trim()) return 'El título es requerido';
    if (!puntuacion.trim()) return 'La puntuación es requerida';

    const pNum = Number.parseInt(puntuacion.trim(), 10);
    if (isNaN(pNum) || pNum <= 0) return 'La puntuación debe ser un número mayor a 0';

    if (preguntas.length < 2) return 'Debe haber al menos 2 categorías';

    for (let i = 0; i < preguntas.length; i++) {
      if (!preguntas[i].text.trim()) {
        return `La categoría ${i + 1} debe tener un nombre`;
      }
      const elementosConTexto = preguntas[i].respuestas.filter(r => r.text.trim());
      if (elementosConTexto.length === 0) {
        return `La categoría ${i + 1} debe tener al menos 1 elemento`;
      }
    }

    if (!temaId) return 'Falta el id del tema en la URL';
    if (Number.isNaN(Number.parseInt(temaId, 10))) return 'El id del tema no es válido';
    if (!cursoId) return 'Falta el id del curso en la URL';

    return null;
  };

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    // Validar campos requeridos
    if (!titulo.trim()) {
      setError('El título es requerido.');
      return;
    }

    if (!puntuacion.trim()) {
      setError('La puntuación es requerida.');
      return;
    }

    const pNum = Number.parseInt(puntuacion.trim(), 10);
    if (isNaN(pNum) || pNum <= 0) {
      setError('La puntuación debe ser un número mayor a 0.');
      return;
    }

    // Validar al menos 2 categorías
    if (preguntas.length < 2) {
      setError('Debe haber al menos 2 categorías.');
      return;
    }

    // Validar que cada categoría tenga al menos 1 elemento con texto
    for (let i = 0; i < preguntas.length; i++) {
      if (!preguntas[i].text.trim()) {
        setError(`La categoría ${i + 1} debe tener un nombre.`);
        return;
      }
      const elementosConTexto = preguntas[i].respuestas.filter(r => r.text.trim());
      if (elementosConTexto.length === 0) {
        setError(`La categoría ${i + 1} debe tener al menos 1 elemento.`);
        return;
      }
    }

    const tIdNum = Number.parseInt(temaId!, 10);

    setLoading(true);
    try {
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      const commonBody = {
        titulo: titulo.trim(),
        descripcion: descripcion.trim() || null,
        puntuacion: pNum,
        tema: { id: tIdNum },
        respVisible: Boolean(respVisible),
        comentariosRespVisible: respVisible ? (comentariosRespVisible.trim() || null) : null,
        imagen: null,
      };

      if (mode === 'create') {
        const resAct = await apiFetch(`${apiBase}/api/generales/clasificacion`, {
          method: 'POST',
          body: JSON.stringify({ ...commonBody, preguntas: [] }),
        });
        const gId = (await resAct.json()) as number;

        for (const q of preguntas) {
          const resPreg = await apiFetch(`${apiBase}/api/preguntas`, {
            method: 'POST',
            body: JSON.stringify({ pregunta: q.text.trim(), actividadId: gId }),
          });
          const pregId = (await resPreg.json()) as number;
          await Promise.all(q.respuestas.map(r => 
            apiFetch(`${apiBase}/api/respuestas`, {
              method: 'POST',
              body: JSON.stringify({ respuesta: r.text.trim(), correcta: true, pregunta: { id: pregId } }),
            })
          ));
        }
      } else {
        const currentPregIds = new Set(preguntas.filter(p => p.id).map(p => p.id!));
        for (const origP of originalPreguntasRef.current) {
          if (!currentPregIds.has(origP.id)) {
            await apiFetch(`${apiBase}/api/preguntas/delete/${origP.id}`, { method: 'DELETE' }).catch(() => {});
          }
        }

        const esqueletoPreguntas = preguntas.filter(p => p.id).map(p => ({
          id: p.id,
          respuestas: p.respuestas.filter(r => r.id).map(r => ({ id: r.id }))
        }));

        try {
          await apiFetch(`${apiBase}/api/generales/clasificacion/update/${clasificacionId}`, {
            method: 'PUT',
            body: JSON.stringify({
              ...commonBody,
              posicion: initialValues?.posicion ?? 0,
              version: initialValues?.version ?? 1,
              preguntas: esqueletoPreguntas
            }),
          });
        } catch (e) { /* Ignorado */ }

        for (const q of preguntas) {
          if (q.id) {
            try {
              await apiFetch(`${apiBase}/api/preguntas/update/${q.id}`, {
                method: 'PUT',
                body: JSON.stringify({ pregunta: q.text.trim(), imagen: null }),
              });
            } catch (e) { /* Ignorado */ }

            const origP = originalPreguntasRef.current.find(o => o.id === q.id);
            if (origP) {
              const currentRespIds = new Set(q.respuestas.filter(r => r.id).map(r => r.id!));
              for (const origR of origP.respuestas) {
                if (!currentRespIds.has(origR.id)) {
                  await apiFetch(`${apiBase}/api/respuestas/delete/${origR.id}`, { method: 'DELETE' }).catch(() => {});
                }
              }
            }

            await Promise.all(q.respuestas.map(async (r) => {
              const body = { respuesta: r.text.trim(), correcta: true, pregunta: { id: q.id }, imagen: null };
              try {
                if (r.id) {
                  await apiFetch(`${apiBase}/api/respuestas/update/${r.id}`, { method: 'PUT', body: JSON.stringify(body) });
                } else {
                  await apiFetch(`${apiBase}/api/respuestas`, { method: 'POST', body: JSON.stringify(body) });
                }
              } catch (e) { /* Ignorado */ }
            }));
          } else {
            const resPreg = await apiFetch(`${apiBase}/api/preguntas`, {
              method: 'POST',
              body: JSON.stringify({ pregunta: q.text.trim(), actividadId: clasificacionId, imagen: null }),
            });
            const newPId = await resPreg.json();
            await Promise.all(q.respuestas.map(r =>
              apiFetch(`${apiBase}/api/respuestas`, {
                method: 'POST',
                body: JSON.stringify({ respuesta: r.text.trim(), correcta: true, pregunta: { id: newPId }, imagen: null }),
              })
            ));
          }
        }
      }
      if (onDone) onDone(); else navigate(`/cursos/${cursoId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al guardar la actividad');
    } finally {
      setLoading(false);
    }
  };

const handleIAResult = (data: any) => {
    // 1. Imprimimos el JSON exacto en la consola por si necesitamos investigarlo
    console.log("Datos crudos de la IA:", data);

    if (data.titulo) setTitulo(data.titulo);
    if (data.descripcion) setDescripcion(data.descripcion);
    if (data.puntuacion) setPuntuacion(String(data.puntuacion));

    // 2. Buscamos el array principal (la IA a veces lo llama 'categorias' en vez de 'preguntas')
    const arrayPrincipal = data.preguntas || data.categorias || data.categories;

    if (Array.isArray(arrayPrincipal) && arrayPrincipal.length > 0) {
      const mappedPreguntas: Pregunta[] = arrayPrincipal.map((p: any) => {
        const categoriaText = p.pregunta || p.categoria || p.nombre || p.titulo || p.name || p.enunciado || 'Categoría sin nombre';
        const arrayElementos = p.respuestas || p.elementos || p.items || p.opciones || [];
        let elementosMapeados: RespuestaOption[] = [makeEmptyRespuesta()];
        if (Array.isArray(arrayElementos) && arrayElementos.length > 0) {
          elementosMapeados = arrayElementos.map((r: any) => {
            if (typeof r === 'string') {
              return { localKey: makeLocalKey(), text: r };
            }
            const textoRespuesta = r.respuesta || r.texto || r.text || r.nombre || r.elemento || '';
            return { localKey: makeLocalKey(), text: String(textoRespuesta) };
          });
        }
        return {
          localKey: makeLocalKey(),
          text: categoriaText,
          respuestas: elementosMapeados
        };
      });
      
      setPreguntas(mappedPreguntas);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="tf-form">
      {error && <p className="tf-error">{error}</p>}
      <GenerarIAModal
        tipoActividad="CLASIFICACION"
        open={showIAModal}
        onClose={() => setShowIAModal(false)}
        onResult={handleIAResult}
      />
    
      <div className="ca-contenedor-blanco tf-header">
        <div className="tf-col">
          <div>
            <label className="cf-label" htmlFor="cf-titulo">Título *</label>
            <input type="text" value={titulo} onChange={(e) => setTitulo(e.target.value)} required style={{ width: '100%' }} />
          </div>
          <div>
            <label className="cf-label" htmlFor="cf-descripcion">Descripción</label>
            <textarea value={descripcion} onChange={(e) => setDescripcion(e.target.value)} rows={3} style={{ width: '100%' }} />
          </div>
        </div>

        <div className="tf-col">
            <div>
            <button type="button" className="iam-trigger-btn" onClick={() => setShowIAModal(true)}>
              Generar con IA
            </button>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <label className="cf-label" htmlFor="cf-puntuacion">Puntuación *</label>
            <input type="number" value={puntuacion} onChange={(e) => setPuntuacion(e.target.value)} required style={{ width: 90 }} />
          </div>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10 }}>
            <input type="checkbox" id="respVisible" checked={respVisible} onChange={(e) => setRespVisible(e.target.checked)} />
            <label className="ca-text" htmlFor="respVisible">Corregir automáticamente</label>
          </div>
          {respVisible && (
            <div style={{ marginTop: 10 }}>
              <label className="ca-text">Comentarios de corrección</label>
              <input type="text" value={comentariosRespVisible} onChange={(e) => setComentariosRespVisible(e.target.value)} style={{ width: '100%' }} />
            </div>
            
          )}
        </div>
      </div>

      <div className="ca-contenedor-blanco" style={{ marginTop: 16, flexDirection: 'column', alignItems: 'stretch' }}>
        <h3 className="ca-text">Configuración de Categorías</h3>
        {preguntas.map((p, pIdx) => (
          <div key={p.localKey} className="tf-question-block" style={{ border: '1px solid #ddd', borderRadius: '8px', padding: '15px', marginBottom: '15px' }}>
            <div className="tf-question-header">
              <span className="tf-question-label">Categoría {pIdx + 1}</span>
              <button type="button" className="tf-btn-remove-question" onClick={() => removePregunta(pIdx)}>✕</button>
            </div>
            <input 
              type="text" 
              className="tf-question-input"
              placeholder="Nombre de la categoría" 
              value={p.text} 
              onChange={(e) => updatePregunta(pIdx, { text: e.target.value })} 
            />
            <div className="tf-options" style={{ marginLeft: '20px' }}>
              {p.respuestas.map((r, rIdx) => (
                <div key={r.localKey} className="tf-option" style={{ display: 'flex', gap: '8px', marginBottom: '8px' }}>
                  <input 
                    type="text" 
                    className="tf-option-input"
                    placeholder="Elemento a clasificar" 
                    value={r.text} 
                    onChange={(e) => updateRespuesta(pIdx, rIdx, { text: e.target.value })} 
                  />
                  <button type="button" className="tf-btn-remove-option" onClick={() => removeRespuesta(pIdx, rIdx)}>✕</button>
                </div>
              ))}
              <button type="button" className="tf-btn-add-option" onClick={() => addRespuesta(pIdx)}>+ Añadir Elemento</button>
            </div>
          </div>
        ))}
        <button type="button" className="tf-btn-add-question" onClick={addPregunta} style={{ width: '100%' }}>
          + Añadir Nueva Categoría
        </button>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 20 }}>
        <button className="ca-btn-guardar" type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar Actividad'}
        </button>
      </div>
    </form>
  );
}