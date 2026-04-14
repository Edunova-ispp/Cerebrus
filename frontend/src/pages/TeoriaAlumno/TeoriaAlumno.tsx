import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import maguitoImg from '../../assets/props/maguito.png';
import ActivityHeader from '../../components/ActivityHeader/ActivityHeader';
import CompletionPopup from '../../components/CompletionPopup/CompletionPopup';
import './TeoriaAlumno.css';

type TeoriaDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly imagen: string | null;
  readonly posicion: number;
  readonly temaId: number | null;
};

export default function TeoriaAlumno() {
  const { actividadId } = useParams<{ actividadId: string }>();
  const navigate = useNavigate();

  const [teoria, setTeoria] = useState<TeoriaDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [actividadAlumnoId, setActividadAlumnoId] = useState<number | null>(null);
  const [isFlipped, setIsFlipped] = useState(false); 

  const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
  const teoriaImageSrc = teoria?.imagen
    ? (/^https?:\/\//i.test(teoria.imagen) || teoria.imagen.startsWith('data:')
        ? teoria.imagen
        : `${apiBase}${teoria.imagen.startsWith('/') ? '' : '/'}${teoria.imagen}`)
    : null;

  useEffect(() => {
    if (!actividadId) return;
    const id = Number.parseInt(actividadId, 10);

    const run = async () => {
      try {
        setLoading(true);
        // 1. Cargar datos de la teoría
        const res = await apiFetch(`${apiBase}/api/actividades/${id}/alumno`);
        if (!res.ok) throw new Error();
        const data = await res.json();
        setTeoria(data);

        // 2. Registrar el inicio de la actividad
        const user = getCurrentUserInfo() as any;
        const alumnoId = user?.id || user?.userId || user?.sub;

        if (alumnoId) {
          const createRes = await apiFetch(`${apiBase}/api/actividades-alumno`, {
            method: 'POST',
            body: JSON.stringify({ alumnoId, actividadId: id }),
          });
          const aaData = await createRes.json();
          setActividadAlumnoId(aaData.id);
        }
      } catch (e) {
        setError('Error cargando la lección');
      } finally {
        setLoading(false);
      }
    };

    run();
  }, [actividadId, apiBase]);

  const [finished, setFinished] = useState(false);

  // Muestra el popup inmediatamente; el registro al backend ocurre al hacer clic en Continuar
  const handleFinalizar = () => {
    setFinished(true);
  };

  const handleContinuar = async () => {
    console.log('[TeoriaAlumno] handleContinuar — actividadAlumnoId:', actividadAlumnoId);
    if (actividadAlumnoId) {
      try {
        const res = await apiFetch(`${apiBase}/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`, {
          method: 'PUT',
          body: JSON.stringify([]),
        });
        const data = await res.json();
        console.log('[TeoriaAlumno] corregir-automaticamente response:', data);
      } catch (e) {
        console.error('[TeoriaAlumno] error registrando completión:', e);
      }
    } else {
      console.warn('[TeoriaAlumno] actividadAlumnoId es null — no se puede registrar la completión');
    }
    navigate(-1);
  };

  if (loading) {
    return (
      <div className="teoria-alumno-page">
        <NavbarMisCursos />
        <main className="teoria-alumno-main">
          <p className="ca-text">Cargando...</p>
        </main>
      </div>
    );
  }

  return (
    <div className="teoria-alumno-page">
      <NavbarMisCursos />

      <main className="teoria-alumno-main">
        {error && <p className="ca-text">{error}</p>}

        {teoria && (
          <>
            <ActivityHeader title={teoria.titulo} guideType="teoria" guideRole="alumno" />

            <div 
              className={`ta-flashcard ${isFlipped ? 'flipped' : ''}`} 
              onClick={() => setIsFlipped(!isFlipped)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  setIsFlipped(!isFlipped);
                }
              }}
              role="button"
              tabIndex={0}
              aria-label="Girar tarjeta de teoria"
            >
              <div className="ta-card-inner">
                
                {/* PARTE DELANTERA: Título e Imagen */}
                <div className="ta-card-front">
                  <div className="ta-card-content">
                    <h1 className="ta-title">{teoria.titulo}</h1>
                    {teoriaImageSrc ? (
                      <img src={teoriaImageSrc} alt="Ilustración" className="ta-main-img" />
                    ) : (
                      <img src={maguitoImg} alt="Maguito" className="ta-maguito-placeholder" />
                    )}
                    <p className="teoria-hint">PULSA PARA LEER LA LECCIÓN</p>
                  </div>
                </div>

                {/* PARTE TRASERA: Descripción */}
                <div className="ta-card-back">
                  <div className="ta-card-content">
                    <h2 className="ta-back-title">{teoria.titulo}</h2>
                    <div className="ta-scroll-area">
                      {teoria.descripcion ? (
                        (() => {
                          const lineOccurrences = new Map<string, number>();
                          return teoria.descripcion.split('\n').map((line) => {
                            const currentCount = (lineOccurrences.get(line) ?? 0) + 1;
                            lineOccurrences.set(line, currentCount);
                            return (
                              <p key={`${line || 'empty-line'}-${currentCount}`} className="ta-paragraph">
                                {line}
                              </p>
                            );
                          });
                        })()
                      ) : (
                        <p className="ta-paragraph">Sin contenido disponible.</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Botón de finalizar */}
            <div className="ta-bottom">
              <button className="ca-btn-guardar" type="button" onClick={handleFinalizar}>
                He terminado de leer
              </button>
            </div>
          </>
        )}

        {!teoria && !error && <p className="ca-text">No se encontró la lección.</p>}

        {finished && <CompletionPopup title="¡LECCIÓN COMPLETADA!" onContinue={handleContinuar} />}
      </main>
    </div>
  );
}