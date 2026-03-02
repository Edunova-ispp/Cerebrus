import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { OrdenacionForm, type OrdenacionFormInitialValues } from '../crearActividad/OrdenacionForm';
import '../crearActividad/crearActividad.css';

type OrdenacionDTO = {
  id: number;
  titulo: string;
  descripcion: string | null;
  puntuacion: number;
  imagen: string | null;
  respVisible: boolean;
  comentariosRespVisible: string | null;
  posicion: number;
  temaId: number;
  valores: string[];
};

export default function EditarActividad() {
  const { id: cursoId, temaId, actividadId } = useParams<{
    id: string;
    temaId: string;
    actividadId: string;
  }>();

  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [ordenacion, setOrdenacion] = useState<OrdenacionDTO | null>(null);

  useEffect(() => {
    if (!actividadId) return;

    setLoading(true);
    setError(null);

    apiFetch(`/api/ordenaciones/${actividadId}/maestro`)
      .then((r) => r.json())
      .then((data: OrdenacionDTO) => setOrdenacion(data))
      .catch((e) => {
        const msg = e instanceof Error ? e.message : 'No se pudo cargar la actividad';
        setError(msg);
      })
      .finally(() => setLoading(false));
  }, [actividadId]);

  const initialValues: OrdenacionFormInitialValues | undefined = ordenacion
    ? {
        titulo: ordenacion.titulo,
        descripcion: ordenacion.descripcion,
        puntuacion: ordenacion.puntuacion,
        imagen: ordenacion.imagen,
        respVisible: ordenacion.respVisible,
        comentariosRespVisible: ordenacion.comentariosRespVisible,
        posicion: ordenacion.posicion,
        valores: ordenacion.valores ?? [],
      }
    : undefined;

  const ordenacionIdNum = actividadId ? Number.parseInt(actividadId, 10) : NaN;

  return (
    <div className="ca-page">
      <NavbarMisCursos />
      <main className="ca-main">
        <div className="ca-sidebar">
          <button
            className="ca-sidebar-btn"
            type="button"
            onClick={() => navigate(`/cursos/${cursoId}/temas`)}
          >
            Volver al Mapa
          </button>
        </div>

        <div className="ca-contenido">
          {loading && <p className="ca-text">Cargando actividad...</p>}

          {!loading && error && <p className="ca-text">{error}</p>}

          {!loading && !error && ordenacion && (
            <OrdenacionForm mode="edit" ordenacionId={ordenacionIdNum} initialValues={initialValues} />
          )}

          {!loading && !error && !ordenacion && (
            <p className="ca-text">Edición no disponible para este tipo de actividad.</p>
          )}
        </div>
      </main>
    </div>
  );
}
