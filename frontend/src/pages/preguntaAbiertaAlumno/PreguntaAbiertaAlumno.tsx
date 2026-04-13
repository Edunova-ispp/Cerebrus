import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import ActivityResultScreen, { type ActivityResultConfig } from '../../components/ActivityResultScreen/ActivityResultScreen';
import AnswerViewModal from '../../components/AnswerViewModal/AnswerViewModal';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';

import './PreguntaAbiertaAlumno.css';
import '../testAlumno/TestAlumno.css';
import dragonImg from '../../assets/props/dragon.png';
import caballeroImg from '../../assets/props/caballero.png';

type EvaluacionAbiertaResponse = {
  readonly notaFinal?: number;
  readonly puntuacionFinal?: number;
};

type PreguntaAbiertaDTO = {
  readonly id: number;
  readonly pregunta: string;
  readonly imagen?: string | null;
};

type AbiertaAlumnoDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion?: string | null;
  readonly puntuacion: number;
  readonly imagen?: string | null;
  readonly preguntas: PreguntaAbiertaDTO[];
  readonly permitirReintento?: boolean;
  readonly mostrarPuntuacion?: boolean;
  readonly encontrarRespuestaMaestro?: boolean;
  readonly encontrarRespuestaAlumno?: boolean;
};

type ActividadAlumnoDTO = {
  readonly id: number;
  readonly puntuacion?: number | null;
  readonly nota?: number | null;
  readonly fechaFin?: string | null;
};

type RespAlumnoGeneralResumenDTO = {
  readonly preguntaId: number | null;
  readonly respuesta: string;
  readonly respuestaCorrecta?: string | null;
};

function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo() as Record<string, unknown> | null;
  if (!info) return null;
  const raw = info.id ?? info.userId ?? info.sub;
  const id = typeof raw === 'string' ? Number(raw) : raw;
  return typeof id === 'number' && Number.isFinite(id) ? id : null;
}

function isCompletedAttempt(fechaFin?: string | null): boolean {
  if (!fechaFin) return false;
  const parsed = new Date(fechaFin);
  if (Number.isNaN(parsed.getTime())) return false;
  return parsed.getFullYear() !== 1970;
}

export default function PreguntaAbiertaAlumno() {
  const { actividadId } = useParams<{ actividadId: string }>();
  const navigate = useNavigate();
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  const [actividad, setActividad] = useState<AbiertaAlumnoDTO | null>(null);
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);

  const [respuestas, setRespuestas] = useState<Map<number, string>>(new Map());
  const [submittedAnswersByQuestion, setSubmittedAnswersByQuestion] = useState<Map<number, string>>(new Map());
  const [correctAnswersByQuestion, setCorrectAnswersByQuestion] = useState<Map<number, string>>(new Map());

  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [lastAttemptGrade, setLastAttemptGrade] = useState<number | null>(null);
  const [lastAttemptScore, setLastAttemptScore] = useState<number | null>(null);
  const [activityConfig, setActivityConfig] = useState<ActivityResultConfig | null>(null);
  const [showAnswerModal, setShowAnswerModal] = useState(false);
  const [answerModalMode, setAnswerModalMode] = useState<'student' | 'correct'>('student');

  const hydrateAnswersFromHistory = async (actAlumnoId: number) => {
    try {
      const histRes = await apiFetch(`${apiBase}/api/respuestas-alumno-general/actividad-alumno/${actAlumnoId}`);
      const histData = (await histRes.json()) as RespAlumnoGeneralResumenDTO[];
      const studentMap = new Map<number, string>();
      const correctMap = new Map<number, string>();

      for (const item of histData) {
        if (typeof item.preguntaId !== 'number') continue;
        studentMap.set(item.preguntaId, item.respuesta ?? '');
        if (typeof item.respuestaCorrecta === 'string' && item.respuestaCorrecta.trim().length > 0) {
          correctMap.set(item.preguntaId, item.respuestaCorrecta);
        }
      }

      setSubmittedAnswersByQuestion(studentMap);
      setCorrectAnswersByQuestion(correctMap);
    } catch {
      setSubmittedAnswersByQuestion(new Map());
      setCorrectAnswersByQuestion(new Map());
    }
  };

  useEffect(() => {
    if (!actividadId) return;

    const loadData = async () => {
      try {
        setLoading(true);
        const res = await apiFetch(`${apiBase}/api/generales/abierta/${actividadId}`);
        const data = (await res.json()) as AbiertaAlumnoDTO;
        setActividad(data);

        setActivityConfig({
          showScore: data.mostrarPuntuacion ?? true,
          allowRetry: data.permitirReintento ?? false,
          showCorrectAnswer: data.encontrarRespuestaMaestro ?? true,
          showStudentAnswer: data.encontrarRespuestaAlumno ?? true,
        });

        const initialMap = new Map<number, string>();
        data.preguntas.forEach((p) => initialMap.set(p.id, ''));
        setRespuestas(initialMap);

        const alumnoId = getCurrentUserIdFromJwt();
        if (!alumnoId) throw new Error('No se pudo identificar al alumno.');

        let hasExisting = false;
        try {
          const getAA = await apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${data.id}`);
          const aaData = (await getAA.json()) as ActividadAlumnoDTO;
          if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
            hasExisting = true;
            setActividadAlumnoId(aaData.id);
            if (isCompletedAttempt(aaData.fechaFin)) {
              setSubmitted(true);
              setLastAttemptScore(aaData.puntuacion ?? 0);
              setLastAttemptGrade(aaData.nota ?? null);
              await hydrateAnswersFromHistory(aaData.id);
            }
          }
        } catch {
          hasExisting = false;
        }

        if (!hasExisting) {
          const createRes = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: data.id }),
          });
          const aaData = (await createRes.json()) as ActividadAlumnoDTO;
          setActividadAlumnoId(aaData.id);
        }
      } catch (e) {
        console.error('Error al cargar la actividad:', e);
      } finally {
        setLoading(false);
      }
    };

    void loadData();
  }, [actividadId, apiBase]);

  const handleSubmit = async () => {
    if (!actividadAlumnoId || !actividad) return;

    const sinResponder = actividad.preguntas.some((p) => !respuestas.get(p.id)?.trim());
    if (sinResponder) {
      alert('Debes responder a todas las preguntas antes de enviar.');
      return;
    }

    setSubmitting(true);
    try {
      const respuestasAlumnoObj: Record<string, string> = {};
      respuestas.forEach((valor, clave) => {
        respuestasAlumnoObj[clave.toString()] = valor;
      });

      const payload = {
        actividadAlumnoId: Number(actividadAlumnoId),
        respuestasAlumno: respuestasAlumnoObj,
      };

      const res = await apiFetch(`${apiBase}/api/respuestas-alumno-general/abierta`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json();
        const backendMessage = errorData?.mensaje || errorData?.message || 'Error al procesar la corrección';
        throw new Error(backendMessage);
      }

      const resultado = (await res.json()) as EvaluacionAbiertaResponse;
      setLastAttemptGrade(typeof resultado?.notaFinal === 'number' ? resultado.notaFinal : null);
      setLastAttemptScore(typeof resultado?.puntuacionFinal === 'number' ? resultado.puntuacionFinal : null);
      setSubmitted(true);

      await hydrateAnswersFromHistory(actividadAlumnoId);
    } catch (error) {
      console.error('Error en el envío final:', error);
      const msg = error instanceof Error ? error.message : '';
      if (msg.includes('cuota') || msg.includes('límite') || msg.includes('429') || msg.toLowerCase().includes('reintenta')) {
        alert(msg || 'La IA ha alcanzado su límite de peticiones. Inténtalo de nuevo más tarde.');
      } else {
        alert('No se pudo procesar la entrega. Verifica tu conexión.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleRetry = async () => {
    if (!actividad) return;

    try {
      const alumnoId = getCurrentUserIdFromJwt();
      if (!alumnoId) throw new Error('No se pudo identificar al alumno.');

      const createRes = await apiFetch(`${apiBase}/api/actividades-alumno`, {
        method: 'POST',
        body: JSON.stringify({ alumnoId, actividadId: actividad.id }),
      });
      const aaData = (await createRes.json()) as ActividadAlumnoDTO;
      if (typeof aaData?.id === 'number' && Number.isFinite(aaData.id)) {
        setActividadAlumnoId(aaData.id);
      }

      const resetMap = new Map<number, string>();
      actividad.preguntas.forEach((p) => resetMap.set(p.id, ''));
      setRespuestas(resetMap);
      setSubmittedAnswersByQuestion(new Map());
      setCorrectAnswersByQuestion(new Map());
      setCurrentIndex(0);
      setLastAttemptScore(null);
      setLastAttemptGrade(null);
      setSubmitted(false);
    } catch {
      alert('No se pudo crear un nuevo intento.');
    }
  };

  const handleViewStudentAnswers = () => {
    setAnswerModalMode('student');
    setShowAnswerModal(true);
  };

  const handleViewCorrectAnswers = () => {
    setAnswerModalMode('correct');
    setShowAnswerModal(true);
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
            <ActivityHeader title={actividad.titulo} guideType="pregunta-abierta" guideRole="alumno" />
            
            <div className="ta-battle-bar">
              <img src={caballeroImg} className="ta-knight-img" alt="Knight" />
              <div className="ta-progress-track">
                <div className="ta-progress-fill" style={{ width: `${((currentIndex + 1) / totalPreguntas) * 100}%` }} />
              </div>
              <img src={dragonImg} className="ta-dragon-img" alt="Dragon" />
            </div>

            <div className="ta-question-card">
              <h3 className="ta-question-counter">
                Pregunta {currentIndex + 1} de {totalPreguntas}
              </h3>

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
                    disabled={submitted || submitting}
                  />
                </div>

                {actividad.imagen && (
                  <div className="ta-card-right">
                    <img src={actividad.imagen} alt={actividad.titulo} className="ta-activity-image" />
                  </div>
                )}
              </div>
            </div>

            <div className="ta-nav-buttons">
              <button
                className="ta-nav-btn ta-nav-btn--prev"
                onClick={() => setCurrentIndex((prev) => prev - 1)}
                disabled={currentIndex === 0 || submitting || submitted}
              >
                Anterior
              </button>

              {currentIndex < totalPreguntas - 1 ? (
                <button
                  className="ta-nav-btn ta-nav-btn--next"
                  onClick={() => setCurrentIndex((prev) => prev + 1)}
                  disabled={submitting || submitted}
                >
                  Siguiente
                </button>
              ) : (
                <button onClick={handleSubmit} className="ta-nav-btn ta-nav-btn--submit" disabled={submitting || submitted}>
                  {submitting ? 'Corrigiendo...' : '¡TERMINAR!'}
                </button>
              )}
            </div>
          </>
        )}

        {submitted && actividad && activityConfig && (
          <ActivityResultScreen
            title="¡ACTIVIDAD CORREGIDA!"
            score={lastAttemptScore ?? 0}
            maxScore={actividad.puntuacion}
            grade={lastAttemptGrade ?? undefined}
            config={activityConfig}
            onContinue={() => navigate(-1)}
            onRetry={handleRetry}
            onViewStudentAnswer={handleViewStudentAnswers}
            onViewCorrectAnswer={handleViewCorrectAnswers}
            onCancel={() => navigate(-1)}
          />
        )}
      </main>

      {showAnswerModal && (
        <AnswerViewModal
          title={answerModalMode === 'student' ? 'Mi respuesta' : 'Respuesta correcta'}
          answers={
            actividad
              ? actividad.preguntas.map((pregunta) => ({
                  question: pregunta.pregunta,
                  studentAnswer:
                    submittedAnswersByQuestion.get(pregunta.id) || respuestas.get(pregunta.id) || '(No respondida)',
                  correctAnswer: correctAnswersByQuestion.get(pregunta.id) || '(No disponible)',
                  isCorrect: undefined,
                }))
              : []
          }
          onClose={() => setShowAnswerModal(false)}
          mode={answerModalMode}
        />
      )}
    </div>
  );
}
