import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { OrdenacionForm, type OrdenacionFormInitialValues } from '../crearActividad/OrdenacionForm';
import { TeoriaForm, type TeoriaFormInitialValues } from '../crearActividad/TeoriaForm';
import { TestForm, type TestFormInitialValues } from '../crearActividad/TestForm';
import { TableroForm, type TableroFormInitialValues } from '../crearActividad/TableroForm';
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
  descripcion: string;
  imagen: string;
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

type TableroDTO = {
  id: number;
  titulo: string;
  descripcion: string | null;
  puntuacion: number;
  tamano: boolean;
  posicion: number;
  temaId: number;
  respVisible: boolean;
  preguntas: {
    id: number;
    pregunta: string;
    respuestas: { id: number; respuesta: string; correcta: boolean }[];
  }[];
};

type ActivityKind = 'ordenacion' | 'test' | 'teoria' | 'tablero' | null;

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
  const [generalTest, setGeneralTest] = useState<GeneralTestMaestroDTO | null>(null);
  const [tablero, setTablero] = useState<TableroDTO | null>(null);

  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    if (!actividadId) return;

    setLoading(true);
    setError(null);

    // 1. Intentar test
    apiFetch(`${apiBase}/api/generales/test/${actividadId}/maestro`)
      .then((r) => r.json())
      .then((data: GeneralTestMaestroDTO) => {
        setGeneralTest(data);
        setKind('test');
        setLoading(false);
      })
      .catch(() => {
        // 2. Intentar ordenación
        apiFetch(`${apiBase}/api/ordenaciones/${actividadId}/maestro`)
          .then((r) => r.json())
          .then((data: OrdenacionDTO) => {
            setOrdenacion(data);
            setKind('ordenacion');
            setLoading(false);
          })
          .catch(() => {
            // 3. Intentar tablero
            apiFetch(`${apiBase}/api/tableros/${actividadId}`)
              .then((r) => r.json())
              .then((data: TableroDTO) => {
                setTablero(data);
                setKind('tablero');
                setLoading(false);
              })
              .catch(() => {
                // 4. Intentar teoría
                apiFetch(`${apiBase}/api/actividades/${actividadId}/maestro`)
                  .then((r) => r.json())
                  .then((data: TeoriaDTO) => {
                    setTeoria(data);
                    setKind('teoria');
                    setLoading(false);
                  })
                  .catch((e) => {
                    const msg = e instanceof Error ? e.message : 'No se pudo cargar la actividad';
                    setError(msg);
                    setLoading(false);
                  });
              });
          });
      });
  }, [actividadId]);

  const tableroInitialValues: TableroFormInitialValues | undefined = tablero
    ? {
        titulo: tablero.titulo,
        descripcion: tablero.descripcion,
        puntuacion: tablero.puntuacion,
        respVisible: tablero.respVisible,
        tamano: tablero.tamano,
        temaId: tablero.temaId,
        preguntas: tablero.preguntas.map((p) => ({
          pregunta: p.pregunta,
          respuesta: p.respuestas[0]?.respuesta ?? '',
        })),
      }
    : undefined;

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

  const teoriaInitialValues: TeoriaFormInitialValues | undefined = teoria
    ? {
        titulo: teoria.titulo,
        descripcion: teoria.descripcion,
        imagen: teoria.imagen,
      }
    : undefined;

  const actividadIdNum = actividadId ? Number.parseInt(actividadId, 10) : NaN;

  const renderForm = () => {
    if (kind === 'test' && generalTest) {
      return (
        <TestForm
          mode="edit"
          generalId={actividadIdNum}
          initialValues={testInitialValues}
        />
      );
    }

    if (kind === 'ordenacion' && ordenacion) {
      return (
        <OrdenacionForm
          mode="edit"
          ordenacionId={actividadIdNum}
          initialValues={ordenacionInitialValues}
        />
      );
    }

    if (kind === 'teoria' && teoria) {
      return (
        <TeoriaForm
          mode="edit"
          actividadId={actividadIdNum}
          initialValues={teoriaInitialValues}
        />
      );
    }

    if (kind === 'tablero' && tablero) {
      return (
        <TableroForm
          mode="edit"
          tableroId={tableroInitialValues ? tablero.id : undefined}
          initialValues={tableroInitialValues}
        />
      );
    }

    return <p className="ca-text">Edición no disponible para este tipo de actividad.</p>;
  };

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
          {!loading && !error && renderForm()}
        </div>
      </main>
    </div>
  );
}