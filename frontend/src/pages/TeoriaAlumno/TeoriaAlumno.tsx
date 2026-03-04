import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso'; // Asegúrate de importar esto
import maguitoImg from '../../assets/props/maguito.png';
import espadaImg from '../../assets/props/espada.png';
import './TeoriaAlumno.css';

type TeoriaDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
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

  useEffect(() => {
    if (!actividadId) return;
    const id = Number.parseInt(actividadId, 10);

    const run = async () => {
      try {
        setLoading(true);
        // 1. Cargar datos de la teoría
        const res = await apiFetch(`/api/actividades/${id}/alumno`);
        if (!res.ok) throw new Error(); 
    const data = await res.json();
    setTeoria(data);

        // 2. Registrar el inicio de la actividad
        const user = getCurrentUserInfo() as any;
        const alumnoId = user?.id || user?.userId || user?.sub;

        if (alumnoId) {
          const createRes = await apiFetch(`/api/actividades-alumno`, {
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
  }, [actividadId]);

  // Función para finalizar la teoría y sumar el punto/completado
  const handleFinalizar = async () => {
    if (actividadAlumnoId) {
      try {
        // Marcamos como acabada en el servidor
        await apiFetch(`/api/actividades-alumno/corregir-automaticamente/${actividadAlumnoId}`, {
          method: 'PUT',
          body: JSON.stringify([]), // Lista vacía porque no hay respuestas que corregir
        });
      } catch (e) {
        console.error("No se pudo marcar como finalizada");
      }
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
            <div className="ta-top">
              <button className="ta-exit-btn" type="button" onClick={() => navigate(-1)}>
                <img src={espadaImg} alt="" className="ta-exit-icon" />
                Salir del curso
              </button>

              <div className="ta-title-banner">
                <h1 className="ta-title">{teoria.titulo}</h1>
                {teoria.descripcion && (
                  <p className="ta-subtitle">{teoria.descripcion.split('\n')[0]}</p>
                )}
              </div>
            </div>

            <div className="ta-content-box">
              <div className="ta-text-area">
                {teoria.descripcion
                  ? teoria.descripcion.split('\n').map((line, i) => (
                      <p key={i} className="ta-paragraph">
                        {line}
                      </p>
                    ))
                  : <p className="ta-paragraph">Sin contenido.</p>
                }
              </div>

              <div className="ta-maguito-wrapper">
                <img src={maguitoImg} alt="Maguito" className="ta-maguito-img" />
              </div>
            </div>

            <div className="ta-bottom">
              <button className="ca-btn-guardar" type="button" onClick={handleFinalizar}>
                He terminado de leer
              </button>
            </div>
          </>
        )}

        {!teoria && !error && <p className="ca-text">No se encontró la lección.</p>}
      </main>
    </div>
  );
}