import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
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

  useEffect(() => {
    if (!actividadId) return;

    const id = Number.parseInt(actividadId, 10);
    if (Number.isNaN(id)) {
      setError('ID de actividad inválido');
      setLoading(false);
      return;
    }

    apiFetch(`/api/actividades/${id}/maestro`)
      .then((r) => r.json())
      .then((data: TeoriaDTO) => setTeoria(data))
      .catch((e) => {
        const msg = e instanceof Error ? e.message : 'Error cargando la lección';
        setError(msg);
      })
      .finally(() => setLoading(false));
  }, [actividadId]);

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
            {/* Fila superior: botón salir + banner título */}
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

            {/* Caja de contenido con maguito */}
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

            {/* Botón continuar */}
            <div className="ta-bottom">
              <button className="ca-btn-guardar" type="button" onClick={() => navigate(-1)}>
                Continuar
              </button>
            </div>
          </>
        )}

        {!teoria && !error && <p className="ca-text">No se encontró la lección.</p>}
      </main>
    </div>
  );
}
