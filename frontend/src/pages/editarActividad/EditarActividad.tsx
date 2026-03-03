import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { OrdenacionForm, type OrdenacionFormInitialValues } from '../crearActividad/OrdenacionForm';
import { TeoriaForm } from '../crearActividad/TeoriaForm';
import { TestForm, type TestFormInitialValues } from '../crearActividad/TestForm';
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

type TeoriaDTO = {
  id: number;
  titulo: string;
  descripcion: string | null;
};

type GeneralTestMaestroDTO = {
  id: number;
  titulo: string;
  descripcion: string | null;
  puntuacion: number;
  imagen: string | null;
  respVisible: boolean;
  comentariosRespVisible: string | null;
  posicion: number;
  version: number;
  temaId: number;
  preguntas: {
    id: number;
    pregunta: string;
    respuestas: { id: number; respuesta: string; correcta: boolean }[];
  }[];
};

type ActivityKind = 'ordenacion' | 'test' | null;

export default function EditarActividad() {
  const { id: cursoId, actividadId } = useParams<{
    id: string;
    temaId: string;
    actividadId: string;
  }>();

  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [kind, setKind] = useState<ActivityKind>(null);
  const [ordenacion, setOrdenacion] = useState<OrdenacionDTO | null>(null);
  const [teoria, setTeoria] = useState<TeoriaDTO | null>(null);
  const [tipo, setTipo] = useState<'ordenacion' | 'teoria' | null>(null);
  const [generalTest, setGeneralTest] = useState<GeneralTestMaestroDTO | null>(null);

  useEffect(() => {
    if (!actividadId) return;

    // Try test endpoint first; if it fails, fall back to ordenacion
    apiFetch(`/api/generales/test/${actividadId}/maestro`)
      .then((r) => r.json())

      .then((data: GeneralTestMaestroDTO) => {
        setGeneralTest(data);
        setKind('test');
      })
      .catch(() => {
        // Not a test activity — try ordenacion
        return apiFetch(`/api/ordenaciones/${actividadId}/maestro`)
          .then((r) => r.json())
          .then((data: OrdenacionDTO) => {
            setOrdenacion(data);
            setKind('ordenacion');
          });
      })
      .catch((e) => {
        const msg = e instanceof Error ? e.message : 'No se pudo cargar la actividad';
        setError(msg);
      })
      .catch(() => {
        apiFetch(`/api/actividades/${actividadId}/maestro`)
          .then((r) => r.json())
          .then((data: TeoriaDTO) => {
            setTeoria(data);
            setTipo('teoria');
          })
          .catch((e) => {
            const msg = e instanceof Error ? e.message : 'No se pudo cargar la actividad';
            setError(msg);
          })
          .finally(() => setLoading(false));
      });
  }, [actividadId]);

  const actividadIdNum = actividadId ? Number.parseInt(actividadId, 10) : NaN;

  const ordenacionInitialValues: OrdenacionFormInitialValues | undefined = ordenacion
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

  const actividadIdNum = actividadId ? Number.parseInt(actividadId, 10) : NaN;

  const renderForm = () => {
    if (tipo === 'ordenacion' && ordenacion) {
      return (
        <OrdenacionForm
          mode="edit"
          ordenacionId={actividadIdNum}
          initialValues={initialValues}
        />
      );
    }

    if (tipo === 'teoria' && teoria) {
      return (
        <TeoriaForm
          mode="edit"
          actividadId={actividadIdNum}
          initialTitulo={teoria.titulo}
          initialDescripcion={teoria.descripcion ?? ''}
        />
      );
    }

    return <p className="ca-text">Edición no disponible para este tipo de actividad.</p>;
  };
  const testInitialValues: TestFormInitialValues | undefined = generalTest
    ? {
        titulo: generalTest.titulo,
        descripcion: generalTest.descripcion,
        puntuacion: generalTest.puntuacion,
        imagen: generalTest.imagen,
        respVisible: generalTest.respVisible,
        comentariosRespVisible: generalTest.comentariosRespVisible,
        posicion: generalTest.posicion,
        version: generalTest.version,
        preguntas: generalTest.preguntas ?? [],
      }
    : undefined;

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


          {!loading && !error && kind === 'ordenacion' && ordenacion && (
            <OrdenacionForm
              mode="edit"
              ordenacionId={actividadIdNum}
              initialValues={ordenacionInitialValues}
            />
          )}

          {!loading && !error && kind === 'test' && generalTest && (
            <TestForm
              mode="edit"
              generalId={actividadIdNum}
              initialValues={testInitialValues}
            />
          )}

          {!loading && !error && kind === null && (
            <p className="ca-text">Edición no disponible para este tipo de actividad.</p>
          )}
        </div>
      </main>
    </div>
  );
}