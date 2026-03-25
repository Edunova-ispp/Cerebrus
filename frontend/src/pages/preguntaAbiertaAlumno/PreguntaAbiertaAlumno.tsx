import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import { apiFetch } from '../../utils/api'; 
import { getCurrentUserInfo } from '../../types/curso';

import './PreguntaAbiertaAlumno.css'; 
import '../testAlumno/TestAlumno.css';
import dragonImg from '../../assets/props/dragon.png';
import caballeroImg from '../../assets/props/caballero.png';

type ModalPhase = 'confirm' | 'evaluating' | 'result';

interface EvaluacionAbiertaResponse {
  notaFinal?: number;
  puntuacionFinal?: number;
}

export default function PreguntaAbiertaAlumno() {
  const { actividadId } = useParams<{ actividadId?: string }>();
  const navigate = useNavigate();
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

  const [actividad, setActividad] = useState<any>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  
  const [respuestas, setRespuestas] = useState<Map<number, string>>(new Map());
  
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [finished, setFinished] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [modalPhase, setModalPhase] = useState<ModalPhase>('confirm');
  const [notaFinal, setNotaFinal] = useState<number | null>(null);
  const [puntuacionFinal, setPuntuacionFinal] = useState<number | null>(null);

  useEffect(() => {
    if (!actividadId) {
      setLoading(false);
      return;
    }
    
    const loadData = async () => {
      try {
        setLoading(true);
        const res = await apiFetch(`${apiBase}/api/generales/abierta/${actividadId}`);
        const data = await res.json();
        setActividad(data);

        // Inicializar respuestas con todas las preguntas
        const initialMap = new Map<number, string>();
        if (data.preguntas) {
          for (const p of data.preguntas) {
            initialMap.set(p.id, '');
          }
        }
        setRespuestas(initialMap);

        const info = getCurrentUserInfo() as any;
        const alumnoId = info?.id || info?.userId;
        
        const createRes = await apiFetch(`${apiBase}/api/actividades-alumno`, {
          method: 'POST',
          body: JSON.stringify({ alumnoId, actividadId: data.id }),
        });
        const aaData = await createRes.json();
        setActividadAlumnoId(aaData.id);

      } catch (e) {
        console.error("Error al cargar la actividad:", e);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [actividadId, apiBase]);

  const handleContinuar = async () => {
    if (!actividadAlumnoId || !actividad) return;
    
    setSubmitting(true);
    setModalPhase('evaluating');
    try {
      const respuestasAlumnoObj: Record<string, string> = {};
      respuestas.forEach((valor, clave) => {
        respuestasAlumnoObj[clave.toString()] = valor;
      });

      const payload = {
        actividadAlumnoId: Number(actividadAlumnoId),
        respuestasAlumno: respuestasAlumnoObj
      };

      const res = await apiFetch(`${apiBase}/api/respuestas-alumno-general/abierta`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json();
        const backendMessage = errorData?.mensaje || errorData?.message || "Error al procesar la corrección";
        throw new Error(backendMessage);
      }

      const resultado: EvaluacionAbiertaResponse = await res.json();
      setNotaFinal(typeof resultado?.notaFinal === 'number' ? resultado.notaFinal : null);
      setPuntuacionFinal(typeof resultado?.puntuacionFinal === 'number' ? resultado.puntuacionFinal : null);
      setModalPhase('result');

    } catch (error) {
      console.error("Error en el envío final:", error);
      const msg = error instanceof Error ? error.message : '';
      if (msg.includes('cuota') || msg.includes('límite') || msg.includes('429') || msg.toLowerCase().includes('reintenta')) {
        alert(msg || 'La IA ha alcanzado su límite de peticiones. Inténtalo de nuevo más tarde.');
      } else {
        alert("No se pudo procesar la entrega. Verifica tu conexión.");
      }
      setModalPhase('confirm');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="ta-loading">Preparando tu desafío...</div>;

  const currentPregunta = actividad?.preguntas[currentIndex];
  const totalPreguntas = actividad?.preguntas.length || 0;

  return (
    <div className="test-alumno-page">
      <NavbarMisCursos />
      <main className="test-alumno-main">
        {actividad && currentPregunta && (
          <>
            <ActivityHeader title={actividad.titulo} />
            
            <div className="ta-battle-bar">
              <img src={caballeroImg} className="ta-knight-img" alt="Knight" />
              <div className="ta-progress-track">
                <div 
                  className="ta-progress-fill" 
                  style={{ width: `${((currentIndex + 1) / totalPreguntas) * 100}%` }} 
                />
              </div>
              <img src={dragonImg} className="ta-dragon-img" alt="Dragon" />
            </div>

            <div className="ta-question-card">
              <h3 className="ta-question-counter">Pregunta {currentIndex + 1} de {totalPreguntas}</h3>

              {/* Layout dos columnas: texto+textarea izquierda · imagen derecha */}
              <div className="ta-card-body">
                <div className="ta-card-left">
                  <p className="ta-question-text">{currentPregunta.pregunta}</p>
                  <textarea
                    className="ta-open-input"
                    placeholder="Escribe aquí tu respuesta detallada..."
                    value={respuestas.get(currentPregunta.id) || ''}
                    onChange={(e) => {
                      const newMap = new Map(respuestas);
                      newMap.set(currentPregunta.id, e.target.value);
                      setRespuestas(newMap);
                    }}
                  />
                </div>

                {actividad.imagen && (
                  <div className="ta-card-right">
                    <img
                      src={actividad.imagen}
                      alt={actividad.titulo}
                      className="ta-activity-image"
                    />
                  </div>
                )}
              </div>
            </div>

            <div className="ta-nav-buttons">
              <button 
                className="ta-nav-btn ta-nav-btn--prev"
                onClick={() => setCurrentIndex(prev => prev - 1)} 
                disabled={currentIndex === 0 || submitting}
              >
                Anterior
              </button>
              
              {currentIndex < totalPreguntas - 1 ? (
                <button 
                  className="ta-nav-btn ta-nav-btn--next"
                  onClick={() => setCurrentIndex(prev => prev + 1)} 
                  disabled={submitting}
                >
                  Siguiente
                </button>
              ) : (
                <button 
                  onClick={() => {
                    const sinResponder = actividad?.preguntas?.some((p: any) => !respuestas.get(p.id)?.trim());
                    if (sinResponder) {
                      alert('Debes responder a todas las preguntas antes de enviar.');
                      return;
                    }
                    setModalPhase('confirm');
                    setNotaFinal(null);
                    setPuntuacionFinal(null);
                    setFinished(true);
                  }} 
                  className="ta-nav-btn ta-nav-btn--submit" 
                  disabled={submitting}
                >
                  ¡TERMINAR!
                </button>
              )}
            </div>
          </>
        )}

        {finished && (
          <CompletionPopup 
            title={
              modalPhase === 'evaluating'
                ? 'LA IA ESTÁ EVALUANDO...'
                : modalPhase === 'result'
                  ? '¡ACTIVIDAD CORREGIDA!'
                  : '¿LISTO PARA ENVIAR?'
            }
            subtitle={
              modalPhase === 'result'
                ? `Nota: ${notaFinal ?? '-'}${puntuacionFinal !== null ? `  •  Puntos: ${puntuacionFinal}` : ''}`
                : undefined
            }
            showContinueButton={modalPhase !== 'evaluating'}
            onContinue={modalPhase === 'result' ? () => navigate(-1) : handleContinuar}
            onCancel={() => {
              if (!submitting) setFinished(false);
            }}
          />
        )}
      </main>
    </div>
  );
}