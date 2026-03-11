import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { ClasificacionForm, type ClasificacionFormInitialPregunta, type ClasificacionFormInitialValues } from '../crearActividad/ClasificacionForm';
import '../crearActividad/crearActividad.css';
import { OrdenacionForm, type OrdenacionFormInitialValues } from '../crearActividad/OrdenacionForm';
import { TeoriaForm } from '../crearActividad/TeoriaForm';
import { TestForm, type TestFormInitialValues } from '../crearActividad/TestForm';

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

type ClasificacionMaestroDTO = {
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
  preguntas: ClasificacionFormInitialPregunta[];
};

type ActivityKind = 'ordenacion' | 'test' | 'teoria' | 'clasificacion' | null;

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
  const [clasificacion, setClasificacion] = useState<ClasificacionMaestroDTO | null>(null);

useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    if (!actividadId) return;

    const cargarActividad = async () => {
        setLoading(true);
        setError(null);

        try {
            // 1. INTENTAR CLASIFICACIÓN
            const res = await apiFetch(`${apiBase}/api/generales/clasificacion/${actividadId}/maestro`);
            if (res.ok) {
                const data: ClasificacionMaestroDTO = await res.json();
                setClasificacion(data);
                setKind('clasificacion');
                setLoading(false);
                return;
            }
        } catch (e) { console.log("Error al cargar clasificación, intentando siguiente tipo...", e); }

        try {
            // 2. INTENTAR TEST
            const res = await apiFetch(`${apiBase}/api/generales/test/${actividadId}/maestro`);
            if (res.ok) {
                const data: GeneralTestMaestroDTO = await res.json();
                setGeneralTest(data);
                setKind('test');
                setLoading(false);
                return;
            }
        } catch (e) { console.log("Error al cargar test, intentando siguiente tipo...", e); }

        try {
            // 3. INTENTAR ORDENACIÓN
            const res = await apiFetch(`${apiBase}/api/ordenaciones/${actividadId}/maestro`);
            if (res.ok) {
                const data: OrdenacionDTO = await res.json();
                setOrdenacion(data);
                setKind('ordenacion');
                setLoading(false);
                return;
            }
        } catch (e) { console.log("Error al cargar ordenación, intentando siguiente tipo...", e); }

        try {
            // 4. INTENTAR TEORÍA (Actividad genérica)
            const res = await apiFetch(`${apiBase}/api/actividades/${actividadId}/maestro`);
            if (res.ok) {
                const data: TeoriaDTO = await res.json();
                setTeoria(data);
                setKind('teoria');
                setLoading(false);
                return;
            }
        } catch (e) { console.log("Error al cargar teoría, no quedan más tipos por intentar.", e); }

        // Si llega aquí, es que ningún endpoint devolvió res.ok
        setError("No se pudo encontrar el tipo de actividad para el ID: " + actividadId);
        setLoading(false);
    };

    cargarActividad();
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

  const clasificacionInitialValues: ClasificacionFormInitialValues | undefined = clasificacion
    ? {
        titulo: clasificacion.titulo,
        descripcion: clasificacion.descripcion,
        puntuacion: clasificacion.puntuacion,
        respVisible: clasificacion.respVisible,
        comentariosRespVisible: clasificacion.comentariosRespVisible,
        posicion: clasificacion.posicion,
        version: clasificacion.version,
        preguntas: clasificacion.preguntas ?? [],
      }
    : undefined;

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
          initialTitulo={teoria.titulo}
          initialDescripcion={teoria.descripcion ?? ''}
        />
      );
    }

    if (kind === 'clasificacion' && clasificacion) {
      return (
        <ClasificacionForm
          mode="edit"
          clasificacionId={actividadIdNum}
          initialValues={clasificacionInitialValues}
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