import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { ClasificacionForm, type ClasificacionFormInitialPregunta, type ClasificacionFormInitialValues } from '../crearActividad/ClasificacionForm';
import '../crearActividad/crearActividad.css';
import { OrdenacionForm, type OrdenacionFormInitialValues } from '../crearActividad/OrdenacionForm';
import { TeoriaForm, type TeoriaFormInitialValues } from '../crearActividad/TeoriaForm';
import { MarcarImagenForm, type MarcarImagenFormInitialValues } from '../crearActividad/MarcarImagenForm';
import { TestForm, type TestFormInitialValues } from '../crearActividad/TestForm';
import { CartaForm, type CartaFormInitialValues } from '../crearActividad/CartaForm';
import { TableroForm, type TableroFormInitialValues } from '../crearActividad/TableroForm';
import { CrucigramaForm, type CrucigramaFormInitialValues } from '../crearActividad/CrucigramaForm';

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
  posicion: number;
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

type MarcarImagenPuntoDTO = {
  id: number;
  respuesta: string;
  pixelX: number;
  pixelY: number;
};

type MarcarImagenDTO = {
  id: number;
  titulo: string;
  descripcion: string | null;
  puntuacion: number;
  imagenActividad: string | null;
  respVisible: boolean;
  comentariosRespVisible: string | null;
  temaId: number;
  imagenAMarcar: string;
  puntosImagen: MarcarImagenPuntoDTO[];
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

type GeneralCartaMaestroDTO = {
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
    imagen: string | null;
    respuestas: { id: number; respuesta: string; correcta: boolean }[];
  }[];
};

type CrucigramaMaestroDTO = {
  id: number;
  titulo: string;
  descripcion: string;
  puntuacion: number;
  respVisible: boolean;
  temaId: number;
  preguntas: {
    id: number;
    pregunta: string;
    respuestas: { id: number; respuesta: string; correcta: boolean }[];
  }[];
  preguntasYRespuestas?: Record<string, string>;
};


type ActivityKind = 'ordenacion' | 'test' | 'teoria' | 'tablero' | 'marcarImagen' | 'clasificacion' | 'carta' | 'crucigrama' | null;

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
  const [marcarImagen, setMarcarImagen] = useState<MarcarImagenDTO | null>(null);
  const [tablero, setTablero] = useState<TableroDTO | null>(null);
  const [generalCarta, setGeneralCarta] = useState<GeneralCartaMaestroDTO | null>(null);
  const [clasificacion, setClasificacion] = useState<ClasificacionMaestroDTO | null>(null);
  const [crucigrama, setCrucigrama ] = useState<CrucigramaMaestroDTO | null>(null);

  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
    if (!actividadId) return;

    let cancelled = false;

    const run = async () => {
      setLoading(true);
      setError(null);
      setKind(null);
      setOrdenacion(null);
      setTeoria(null);
      setGeneralTest(null);
      setMarcarImagen(null);
      setTablero(null);
      setGeneralCarta(null);
      setClasificacion(null);
      setCrucigrama(null);

      try {
        try {
          const r = await apiFetch(`${apiBase}/api/generales/test/${actividadId}/maestro`);
          const data = (await r.json()) as GeneralTestMaestroDTO;
          if (cancelled) return;
          setGeneralTest(data);
          setKind('test');
          return;
        } catch {
          // try next kind
        }

        try {
          const r = await apiFetch(`${apiBase}/api/ordenaciones/${actividadId}/maestro`);
          const data = (await r.json()) as OrdenacionDTO;
          if (cancelled) return;
          setOrdenacion(data);
          setKind('ordenacion');
          return;
        } catch {
          // try next kind
        }

        try {
          const r = await apiFetch(`${apiBase}/api/generales/cartas/${actividadId}/maestro`);
          const data = (await r.json()) as GeneralCartaMaestroDTO;
          if (cancelled) return;
          setGeneralCarta(data);
          setKind('carta');
          return;
        } catch {
          // try next kind
        }

        try {
          const r = await apiFetch(`${apiBase}/api/marcar-imagenes/${actividadId}`);
          const data = (await r.json()) as MarcarImagenDTO;
          if (cancelled) return;
          setMarcarImagen(data);
          setKind('marcarImagen');
          return;
        } catch {
          // try next kind
        }

        try {
          const r = await apiFetch(`${apiBase}/api/tableros/${actividadId}`);
          const data = (await r.json()) as TableroDTO;
          if (cancelled) return;
          setTablero(data);
          setKind('tablero');
          return;
        } catch {
          // try next kind
        }

        try {
          const r = await apiFetch(`${apiBase}/api/generales/crucigrama/${actividadId}`);
          const data = (await r.json()) as CrucigramaMaestroDTO;
          if (cancelled) return;
          setCrucigrama(data);
          setKind('crucigrama');
          return;
        } catch {
          // try next kind
        }

        try {
          const r = await apiFetch(`${apiBase}/api/generales/clasificacion/${actividadId}/maestro`);
          const data = (await r.json()) as ClasificacionMaestroDTO;
          if (cancelled) return;
          setClasificacion(data);
          setKind('clasificacion');
          return;
        } catch {
          // try next kind
        }

        try {
          const r = await apiFetch(`${apiBase}/api/actividades/${actividadId}/maestro`);
          const data = (await r.json()) as TeoriaDTO;
          if (cancelled) return;
          setTeoria(data);
          setKind('teoria');
          return;
        } catch (e) {
          const msg = e instanceof Error ? e.message : 'No se pudo cargar la actividad';
          if (cancelled) return;
          setError(msg);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    run();

    return () => {
      cancelled = true;
    };
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
        posicion: teoria.posicion,
      }
    : undefined;

  const cartaInitialValues: CartaFormInitialValues | undefined = generalCarta
    ? {
        titulo: generalCarta.titulo,
        descripcion: generalCarta.descripcion,
        puntuacion: generalCarta.puntuacion,
        imagen: generalCarta.imagen,
        respVisible: generalCarta.respVisible,
        comentariosRespVisible: generalCarta.comentariosRespVisible,
        posicion: generalCarta.posicion,
        version: generalCarta.version,
        preguntas: generalCarta.preguntas ?? [],
      }
    : undefined;

  const marcarImagenInitialValues: MarcarImagenFormInitialValues | undefined = marcarImagen
    ? {
        titulo: marcarImagen.titulo,
        descripcion: marcarImagen.descripcion,
        puntuacion: marcarImagen.puntuacion,
        respVisible: marcarImagen.respVisible,
        comentariosRespVisible: marcarImagen.comentariosRespVisible,
        imagenAMarcar: marcarImagen.imagenAMarcar,
        puntosImagen: (marcarImagen.puntosImagen ?? []).map((p) => ({
          id: p.id,
          respuesta: p.respuesta,
          pixelX: p.pixelX,
          pixelY: p.pixelY,
        })),
      }
    : undefined;

  const actividadIdNum = actividadId ? Number.parseInt(actividadId, 10) : Number.NaN;

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

  const crucigramaInitialValues: CrucigramaFormInitialValues | undefined = crucigrama
  ? {
      titulo: crucigrama.titulo,
      descripcion: crucigrama.descripcion || '',
      puntuacion: crucigrama.puntuacion,
      respVisible: crucigrama.respVisible,
      temaId: crucigrama.temaId,
      preguntasYRespuestas: crucigrama.preguntasYRespuestas,
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
          initialValues={teoriaInitialValues}
        />
      );
    }

    if (kind === 'marcarImagen' && marcarImagen) {
      return (
        <MarcarImagenForm
          mode="edit"
          marcarImagenId={actividadIdNum}
          initialValues={marcarImagenInitialValues}
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

    if (kind === 'carta' && generalCarta) {
      return (
        <CartaForm
          mode="edit"
          generalId={actividadIdNum}
          initialValues={cartaInitialValues}
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

    if (kind === 'crucigrama' && crucigrama) {
      return (
        <CrucigramaForm
          mode="edit"
          crucigramaId={actividadIdNum}
          initialValues={crucigramaInitialValues}
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