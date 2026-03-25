import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../utils/api';
import '../crearActividad/TestForm.css';
import './PreguntaAbiertaForm.css';

export interface PreguntaAbiertaFormInitialValues {
  titulo: string;
  descripcion?: string;
  puntuacion: number;
  imagen?: string | null;
  respVisible: boolean;
  comentariosRespVisible?: string | null;
  version?: number;
  posicion?: number;
  preguntas: {
    id?: number;
    pregunta: string;
    respuesta: string;
    respuestaId?: number; 
  }[];
}

interface PreguntaAbiertaFormProps {
  mode: 'create' | 'edit';
  preguntaAbiertaId?: number;
  initialValues?: PreguntaAbiertaFormInitialValues;
  temaIdProp?: string;
  cursoIdProp?: string;
  onDone?: () => void;
}

interface PreguntaAbiertaMeta {
  posicion?: number;
  version?: number;
}

export const PreguntaAbiertaForm: React.FC<PreguntaAbiertaFormProps> = ({
  mode,
  preguntaAbiertaId,
  initialValues,
  temaIdProp,
  onDone,
}) => {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [puntos, setPuntos] = useState<number | ''>('');
  const [imagen, setImagen] = useState('');
  // NUEVO ESTADO PARA CONTROLAR ERRORES DE IMAGEN
  const [imagenError, setImagenError] = useState(false); 
  const [respVisible, setRespVisible] = useState(false);
  const [comentariosRespVisible, setComentariosRespVisible] = useState('');
  const [preguntas, setPreguntas] = useState<{ id?: number; pregunta: string; respuesta: string; respuestaId?: number }[]>(
    [{ pregunta: '', respuesta: '' }]
  );

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');  

  const originalQuestionsRef = useRef<PreguntaAbiertaFormInitialValues['preguntas']>([]);

  useEffect(() => {
    if (!initialValues) return;
    setTitulo(initialValues.titulo ?? '');
    setDescripcion(initialValues.descripcion ?? '');
    setPuntos(initialValues.puntuacion ?? '');
    
    // Al cargar los datos para edición, seteamos la imagen y reseteamos el error
    setImagen(initialValues.imagen ?? '');
    setImagenError(false);
    
    setRespVisible(Boolean(initialValues.respVisible));
    setComentariosRespVisible(initialValues.comentariosRespVisible ?? '');
    
    if (initialValues.preguntas && initialValues.preguntas.length > 0) {
      originalQuestionsRef.current = [...initialValues.preguntas];
      setPreguntas(initialValues.preguntas);
    }
  }, [initialValues]);

  // --- HANDLERS PARA AÑADIR/ELIMINAR ---

  const handleAddPregunta = () => {
    if (preguntas.length < 5) {
      setPreguntas([...preguntas, { pregunta: '', respuesta: '' }]);
    }
  };

  const handleRemovePregunta = (index: number) => {
    if (preguntas.length > 1) {
      setPreguntas(preguntas.filter((_, i) => i !== index));
    }
  };

  const handlePreguntaChange = (index: number, field: 'pregunta' | 'respuesta', value: string) => {
    const updated = [...preguntas];
    updated[index] = { ...updated[index], [field]: value };
    setPreguntas(updated);
  };

  const isValid = titulo.trim().length > 0 && puntos !== '' && preguntas.every(p => p.pregunta.trim() && p.respuesta.trim());

  const handleGuardar = async () => {
    if (!isValid) return;
    setSaving(true);
    setError('');

    try {
      const tId = Number(temaIdProp);
      let gId = preguntaAbiertaId;
      const idsFinales: number[] = [];
      let posicionFinal = initialValues?.posicion;
      let versionFinal = initialValues?.version;

      // PASO 1: Crear si es nuevo
      if (mode === 'create') {
        const res = await apiFetch(`${apiBase}/api/generales/abierta/maestro`, {
          method: 'POST',
          body: JSON.stringify({
            titulo: titulo.trim(),
            descripcion: descripcion.trim() || null,
            puntuacion: Number(puntos),
            imagen: imagen.trim() || null,
            respVisible,
            comentariosRespVisible: comentariosRespVisible.trim() || null,
            tema: { id: tId },
            tipo: 'ABIERTA',
            preguntas: []
          }),
        });
        gId = await res.json();

        if (gId) {
          const metaRes = await apiFetch(`${apiBase}/api/generales/abierta/${gId}/maestro`);
          const meta = (await metaRes.json()) as PreguntaAbiertaMeta;
          posicionFinal = meta?.posicion;
          versionFinal = meta?.version;
        }
      }

      // PASO 2: Borrar preguntas eliminadas de la DB (Solo en edición)
      if (mode === 'edit') {
        const currentIdsSet = new Set(preguntas.filter(p => p.id).map(p => p.id));
        for (const orig of originalQuestionsRef.current) {
          if (orig.id && !currentIdsSet.has(orig.id)) {
            await apiFetch(`${apiBase}/api/preguntas/delete/${orig.id}`, { method: 'DELETE' });
          }
        }
      }

      // PASO 3: Crear/Actualizar preguntas y respuestas
      for (const p of preguntas) {
        if (p.id) {
          await apiFetch(`${apiBase}/api/preguntas/update/${p.id}`, {
            method: 'PUT',
            body: JSON.stringify({ pregunta: p.pregunta.trim() }),
          });
          if (p.respuestaId) {
            await apiFetch(`${apiBase}/api/respuestas/update/${p.respuestaId}`, {
              method: 'PUT',
              body: JSON.stringify({ respuesta: p.respuesta.trim(), correcta: true, pregunta: { id: p.id } }),
            });
          }
          idsFinales.push(p.id);
        } else {
          const resPreg = await apiFetch(`${apiBase}/api/preguntas`, {
            method: 'POST',
            body: JSON.stringify({ pregunta: p.pregunta.trim(), actividadId: gId }),
          });
          const newId = await resPreg.json();
          await apiFetch(`${apiBase}/api/respuestas`, {
            method: 'POST',
            body: JSON.stringify({ respuesta: p.respuesta.trim(), correcta: true, pregunta: { id: newId } }),
          });
          idsFinales.push(newId);
        }
      }

      // PASO 4: PUT FINAL
      await apiFetch(`${apiBase}/api/generales/abierta/update/${gId}`, {
        method: 'PUT',
        body: JSON.stringify({
          titulo: titulo.trim(),
          descripcion: descripcion.trim() || null,
          puntuacion: Number(puntos),
          imagen: imagen.trim() || null,
          respVisible,
          comentariosRespVisible: comentariosRespVisible.trim() || null,
          tipo: 'ABIERTA',
          tema: { id: tId },
          posicion: typeof posicionFinal === 'number' ? posicionFinal : 1,
          version: typeof versionFinal === 'number' ? versionFinal : 1,
          preguntasId: idsFinales,
          preguntas: idsFinales.map(id => ({ id })) 
        }),
      });

      setSuccess('¡Guardado correctamente!');
      setTimeout(() => onDone?.(), 1000);
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Error al guardar';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="paf-wrapper tf-form">
      {error && <p className="tf-error">{error}</p>}
      {success && <p className="ca-text" style={{ color: '#27ae60' }}>{success}</p>}

      {/* ── TOP: Metadata ── */}
      <div className="tf-header">
        <div className="tf-col">
          <div>
            <label className="tf-label">Título *</label>
            <input className="tf-input" value={titulo} onChange={e => setTitulo(e.target.value)} placeholder="Título de la actividad" />
          </div>

          <div>
            <label className="tf-label">Descripción</label>
            <textarea className="tf-input" value={descripcion} onChange={e => setDescripcion(e.target.value)} rows={3} style={{ resize: 'vertical' }} placeholder="Descripción opcional" />
          </div>

          <div>
            <label className="tf-label">URL de imagen (opcional)</label>
            <input 
              type="url" 
              className="tf-input" 
              value={imagen} 
              onChange={e => {
                setImagen(e.target.value);
                setImagenError(false); // Reseteamos el error si el usuario cambia la URL
              }} 
              placeholder="https://..." 
            />
            
            {/* Renderizado condicional de la imagen usando el estado */}
            {imagen.trim() && !imagenError && (
              <img 
                src={imagen.trim()} 
                alt="Preview" 
                className="tf-img-preview" 
                onError={() => setImagenError(true)} 
              />
            )}
            
            {/* Mensaje de error visual para el usuario (opcional pero recomendado) */}
            {imagen.trim() && imagenError && (
              <span style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px', display: 'block' }}>
                No se pudo cargar la imagen. Comprueba la URL.
              </span>
            )}
          </div>
        </div>

        <div className="tf-col">
          <div>
            <label className="tf-label">Puntuación *</label>
            <input type="number" className="tf-input tf-input-sm" value={puntos} onChange={e => setPuntos(e.target.value === '' ? '' : Number(e.target.value))} min="1" />
          </div>

          <label className="tf-check-label">
            <input type="checkbox" checked={respVisible} onChange={e => setRespVisible(e.target.checked)} />
            <span>Mostrar correcciones al alumno</span>
          </label>

          {respVisible && (
            <div>
              <label className="tf-label">Comentarios</label>
              <input type="text" className="tf-input" value={comentariosRespVisible} onChange={e => setComentariosRespVisible(e.target.value)} />
            </div>
          )}
        </div>
      </div>

      <div className="tf-questions">
        <p className="tf-help">
          Añade las preguntas y la respuesta modelo. La IA evaluará la respuesta del alumno comparándola con tu respuesta.
        </p>

        <div className="paf-preguntas-header">
          <span className="ca-text" style={{ fontWeight: 'bold' }}>Preguntas y Respuestas</span>
          <span className="paf-badge">{preguntas.length} / 5</span>
        </div>

        {preguntas.map((p, index) => (
          <div key={index} className="paf-pregunta-row">
            <div className="paf-input-group">
              <input className="tf-input" value={p.pregunta} onChange={e => handlePreguntaChange(index, 'pregunta', e.target.value)} placeholder={`Pregunta ${index + 1}`} />
              <input className="tf-input" value={p.respuesta} onChange={e => handlePreguntaChange(index, 'respuesta', e.target.value)} placeholder="Respuesta modelo" />
            </div>
            {preguntas.length > 1 && (
              <button type="button" className="paf-btn-remove" onClick={() => handleRemovePregunta(index)}>✕</button>
            )}
          </div>
        ))}

        {preguntas.length < 5 && (
          <button type="button" className="paf-btn-add" onClick={handleAddPregunta}>
            + Añadir pregunta
          </button>
        )}
      </div>{/* close tf-questions */}

      <div className="ca-form-footer">
        <button className="ca-btn-guardar" onClick={handleGuardar} disabled={saving || !isValid}>
          {saving ? 'GUARDANDO...' : 'GUARDAR'}
        </button>
      </div>
    </div>
  );
};