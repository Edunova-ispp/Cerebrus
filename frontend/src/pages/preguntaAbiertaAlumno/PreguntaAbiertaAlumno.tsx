import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import { apiFetch } from '../../utils/api'; 
import { getCurrentUserInfo } from '../../types/curso';

import './PreguntaAbiertaAlumno.css'; 
import dragonImg from '../../assets/props/dragon.png';
import caballeroImg from '../../assets/props/caballero.png';

export default function PreguntaAbiertaAlumno() {
  const { actividadId } = useParams<{ actividadId: string }>();
  const navigate = useNavigate();
  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

  const [actividad, setActividad] = useState<any>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  
  // Guardamos las respuestas en un Map <ID_Pregunta, Texto>
  const [respuestas, setRespuestas] = useState<Map<number, string>>(new Map());
  
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [finished, setFinished] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!actividadId) return;
    
    const loadData = async () => {
      try {
        setLoading(true);
        // 1. Obtener la actividad
        const res = await apiFetch(`${apiBase}/api/generales/abierta/${actividadId}`);
        const data = await res.json();
        setActividad(data);

        // 2. Registrar el inicio de la actividad para el alumno
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
    try {
      /**
       * CONVERSIÓN AL TIPO LinkedHashMap<Long, String>
       * Creamos un objeto plano donde las llaves son los IDs de las preguntas.
       * Jackson en Java transformará estas llaves String a Long automáticamente.
       */
      const respuestasAlumnoObj: Record<string, string> = {};
      respuestas.forEach((valor, clave) => {
        respuestasAlumnoObj[clave.toString()] = valor;
      });

      // El payload debe coincidir con EvaluacionActividadAbiertaRequest.java
      const payload = {
        actividadAlumnoId: Number(actividadAlumnoId),
        respuestasAlumno: respuestasAlumnoObj
      };

      console.log("Enviando evaluación...", payload);

      // IMPORTANTE: Asegúrate de que esta URL sea la que acepta el método POST en tu Controller
      const res = await apiFetch(`${apiBase}/api/respuestas-alumno-general/abierta`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.message || "Error al procesar la corrección");
      }

      const dataFinal = await res.json();
      console.log("Resultado de la IA:", dataFinal);

      // Redirigir tras el éxito
      navigate('/mis-cursos');

    } catch (error) {
      console.error("Error en el envío final:", error);
      alert("No se pudo procesar la entrega. Verifica tu conexión.");
    } finally {
      setSubmitting(false);
      setFinished(false);
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

            <div className="ta-nav-buttons">
              <button 
                onClick={() => setCurrentIndex(prev => prev - 1)} 
                disabled={currentIndex === 0 || submitting}
              >
                Anterior
              </button>
              
              {currentIndex < totalPreguntas - 1 ? (
                <button 
                  onClick={() => setCurrentIndex(prev => prev + 1)} 
                  disabled={submitting}
                >
                  Siguiente
                </button>
              ) : (
                <button 
                  onClick={() => setFinished(true)} 
                  className="ta-nav-btn--submit" 
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
            title={submitting ? "LA IA ESTÁ EVALUANDO..." : "¿LISTO PARA ENVIAR?"} 
            onContinue={handleContinuar} 
            onCancel={() => !submitting && setFinished(false)}
          />
        )}
      </main>
    </div>
  );
}