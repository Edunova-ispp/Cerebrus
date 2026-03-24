import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../utils/api';
import './PreguntaAbiertaForm.css';

export interface PreguntaAbiertaFormInitialValues {
  titulo: string;
  descripcion?: string;
  puntuacion: number;
  respVisible: boolean;
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

export const PreguntaAbiertaForm: React.FC<PreguntaAbiertaFormProps> = ({
  mode,
  preguntaAbiertaId,
  initialValues,
  temaIdProp,
  onDone,
}) => {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const [titulo, setTitulo] = useState('');
  const [puntos, setPuntos] = useState<number | ''>('');
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
    setPuntos(initialValues.puntuacion ?? '');
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

      // PASO 1: Crear si es nuevo
      if (mode === 'create') {
        const res = await apiFetch(`${apiBase}/api/generales/abierta/maestro`, {
          method: 'POST',
          body: JSON.stringify({
            titulo: titulo.trim(),
            puntuacion: Number(puntos),
            tema: { id: tId },
            tipo: 'ABIERTA',
            preguntas: []
          }),
        });
        gId = await res.json();
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
              body: JSON.stringify({ respuesta: p.respuesta.trim(), correcta: true }),
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
          descripcion: 'ACTIVIDAD_ABIERTA',
          puntuacion: Number(puntos),
          respVisible: true,
          tipo: 'ABIERTA',
          tema: { id: tId },
          posicion: initialValues?.posicion ?? 0,
          version: initialValues?.version ?? 1,
          preguntasId: idsFinales, // Enviamos lista de IDs para el servicio
          preguntas: idsFinales.map(id => ({ id })) 
        }),
      });

      setSuccess('¡Guardado correctamente!');
      setTimeout(() => onDone?.(), 1000);
    } catch (e) {
      setError('Error al guardar');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="paf-wrapper">
      {error && <p className="paf-error">{error}</p>}
      {success && <p className="paf-success">{success}</p>}
      
      <div className="paf-top-row">
        <div className="paf-field">
          <label className="paf-label">Título</label>
          <input className="paf-input" value={titulo} onChange={e => setTitulo(e.target.value)} placeholder="Título de la actividad" />
        </div>
        <div className="paf-field">
          <label className="paf-label">Puntos</label>
          <input className="paf-input-puntos" type="number" value={puntos} onChange={e => setPuntos(e.target.value === '' ? '' : Number(e.target.value))} placeholder="0" />
        </div>
      </div>

      <hr className="paf-divider" />

      <div className="paf-preguntas-header">
        <span>Preguntas y Respuestas</span>
        <span className="paf-badge">{preguntas.length} / 5</span>
      </div>

      {preguntas.map((p, index) => (
        <div key={index} className="paf-pregunta-row">
          <div className="paf-input-group">
            <input className="paf-input" value={p.pregunta} onChange={e => handlePreguntaChange(index, 'pregunta', e.target.value)} placeholder={`Pregunta ${index + 1}`} />
            <input className="paf-input" value={p.respuesta} onChange={e => handlePreguntaChange(index, 'respuesta', e.target.value)} placeholder="Respuesta" />
          </div>
          {preguntas.length > 1 && (
            <button type="button" className="paf-btn-remove" onClick={() => handleRemovePregunta(index)}>✕</button>
          )}
        </div>
      ))}

      {preguntas.length < 5 && (
        <button type="button" className="paf-btn-add" onClick={handleAddPregunta}>
          + Añadir otra pregunta
        </button>
      )}

      <div className="paf-footer">
        <button className="paf-save-btn" onClick={handleGuardar} disabled={saving || !isValid}>
          {saving ? 'Guardando...' : 'GUARDAR ACTIVIDAD'}
        </button>
      </div>
    </div>
  );
};