import { useEffect, useMemo, useState } from 'react';
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
import { PreguntaAbiertaForm, type PreguntaAbiertaFormInitialValues } from '../crearActividad/PreguntaAbiertaForm';

// --- TIPOS DTO ---

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
    temaId: number;
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

type PreguntaAbiertaMaestroDTO = {
    id: number;
    titulo: string;
    descripcion: string;
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

type TemaActividadMetaDTO = {
    id: number;
    tipo: string;
};

type TemaWithActividadesDTO = {
    actividades?: TemaActividadMetaDTO[];
};

type ActivityKind = 'ordenacion' | 'test' | 'teoria' | 'tablero' | 'marcarImagen' | 'clasificacion' | 'carta' | 'crucigrama' | 'preguntaAbierta' | null;

interface EditarActividadProps {
    readonly actividadIdProp?: string;
    readonly temaIdProp?: string;
    readonly cursoIdProp?: string;
    readonly embedded?: boolean;
    readonly onDone?: () => void;
}

const isCrucigramaMaestroDTO = (value: unknown): value is CrucigramaMaestroDTO => {
    if (typeof value !== 'object' || value === null) return false;
    const data = value as Record<string, unknown>;
    return (
        typeof data.titulo === 'string' &&
        typeof data.descripcion === 'string' &&
        typeof data.puntuacion === 'number' &&
        typeof data.temaId === 'number' &&
        Array.isArray(data.preguntas)
    );
};

const isTeoriaDTO = (value: unknown): value is TeoriaDTO => {
    if (typeof value !== 'object' || value === null) return false;
    const data = value as Record<string, unknown>;
    return typeof data.titulo === 'string' && !('puntuacion' in data);
};

export default function EditarActividad({ actividadIdProp, temaIdProp, cursoIdProp, embedded, onDone }: EditarActividadProps = {}) {
    const params = useParams<{ id: string; temaId: string; actividadId: string }>();
    const cursoId = cursoIdProp ?? params.id;
    const temaId = temaIdProp ?? params.temaId;
    const actividadId = actividadIdProp ?? params.actividadId;

    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [kind, setKind] = useState<ActivityKind>(null);

    // States para los DTOs
    const [ordenacion, setOrdenacion] = useState<OrdenacionDTO | null>(null);
    const [teoria, setTeoria] = useState<TeoriaDTO | null>(null);
    const [generalTest, setGeneralTest] = useState<GeneralTestMaestroDTO | null>(null);
    const [crucigrama, setCrucigrama] = useState<CrucigramaMaestroDTO | null>(null);
    const [preguntaAbierta, setPreguntaAbierta] = useState<PreguntaAbiertaMaestroDTO | null>(null);
    const [marcarImagen, setMarcarImagen] = useState<MarcarImagenDTO | null>(null);
    const [tablero, setTablero] = useState<TableroDTO | null>(null);
    const [generalCarta, setGeneralCarta] = useState<GeneralCartaMaestroDTO | null>(null);
    const [clasificacion, setClasificacion] = useState<ClasificacionMaestroDTO | null>(null);

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
            setPreguntaAbierta(null);

            let activityTypeHint: string | null = null;
            const temaIdNum = Number.parseInt(String(temaId), 10);
            const actividadIdNum = Number.parseInt(String(actividadId), 10);

            if (!Number.isNaN(temaIdNum) && !Number.isNaN(actividadIdNum)) {
                try {
                    const temaResponse = await apiFetch(`${apiBase}/api/temas/${temaIdNum}`);
                    const temaData = (await temaResponse.json()) as TemaWithActividadesDTO;
                    const actividadMeta = temaData.actividades?.find((item) => item.id === actividadIdNum);
                    activityTypeHint = actividadMeta?.tipo ?? null;
                } catch {
                    // optional hint; continue with endpoint probing
                }
            }

            try {
                const tipo = activityTypeHint?.toUpperCase();

                if (tipo === 'ABIERTA') {
                    const r = await apiFetch(`${apiBase}/api/generales/abierta/${actividadId}/maestro`);
                    if (r.ok) {
                        const data = await r.json();
                        if (cancelled) return;
                        setPreguntaAbierta(data);
                        setKind('preguntaAbierta');
                        return;
                    }
                }

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
                    const r = await apiFetch(`${apiBase}/api/generales/clasificacion/${actividadId}/maestro`);
                    const data = (await r.json()) as ClasificacionMaestroDTO;
                    if (cancelled) return;
                    setClasificacion(data);
                    setKind('clasificacion');
                    return;
                } catch {
                    // try next kind
                }

                if (activityTypeHint === 'teoria') {
                    const r = await apiFetch(`${apiBase}/api/actividades/${actividadId}/maestro`);
                    const raw = await r.json();
                    if (!isTeoriaDTO(raw)) {
                        throw new Error('No se pudo identificar la actividad de teoría');
                    }
                    if (cancelled) return;
                    setTeoria(raw);
                    setKind('teoria');
                    return;
                }

                if (activityTypeHint === 'crucigrama') {
                    const r = await apiFetch(`${apiBase}/api/generales/crucigrama/${actividadId}`);
                    const raw = await r.json();
                    if (!isCrucigramaMaestroDTO(raw)) {
                        throw new Error('No se pudo identificar la actividad de crucigrama');
                    }
                    if (cancelled) return;
                    setCrucigrama(raw);
                    setKind('crucigrama');
                    return;
                }

                try {
                    const r = await apiFetch(`${apiBase}/api/generales/crucigrama/${actividadId}`);
                    const raw = await r.json();
                    if (!isCrucigramaMaestroDTO(raw)) {
                        throw new Error('No es un crucigrama');
                    }
                    if (cancelled) return;
                    setCrucigrama(raw);
                    setKind('crucigrama');
                    return;
                } catch {
                    // try next kind
                }

                try {
                    const r = await apiFetch(`${apiBase}/api/actividades/${actividadId}/maestro`);
                    const raw = await r.json();
                    if (!isTeoriaDTO(raw)) {
                        throw new Error('No es una teoría');
                    }
                    if (cancelled) return;
                    setTeoria(raw);
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
        return () => { cancelled = true; };
    }, [actividadId, temaId]);

    // --- MAPEO DE VALORES INICIALES ---

    const preguntaAbiertaInitialValues = useMemo<PreguntaAbiertaFormInitialValues | undefined>(() => {
        if (!preguntaAbierta) return undefined;
        return {
            titulo: preguntaAbierta.titulo,
            descripcion: preguntaAbierta.descripcion,
            puntuacion: preguntaAbierta.puntuacion,
            imagen: preguntaAbierta.imagen,
            respVisible: preguntaAbierta.respVisible,
            comentariosRespVisible: preguntaAbierta.comentariosRespVisible,
            posicion: preguntaAbierta.posicion,
            version: preguntaAbierta.version,
            preguntas: (preguntaAbierta.preguntas ?? []).map((p) => ({
                id: p.id,
                pregunta: p.pregunta,
                respuestaId: p.respuestas?.[0]?.id,
                respuesta: p.respuestas?.[0]?.respuesta || '',
            })),
        };
    }, [preguntaAbierta]);

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
        } : undefined;

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
            temaId: generalTest.temaId,
            preguntas: generalTest.preguntas ?? [],
        } : undefined;

    const crucigramaInitialValues: CrucigramaFormInitialValues | undefined = crucigrama
        ? {
            titulo: crucigrama.titulo,
            descripcion: crucigrama.descripcion || '',
            puntuacion: crucigrama.puntuacion,
            respVisible: crucigrama.respVisible,
            temaId: crucigrama.temaId,
            preguntasYRespuestas: crucigrama.preguntasYRespuestas,
        } : undefined;

    const ordenacionInitialValues: OrdenacionFormInitialValues | undefined = ordenacion
        ? {
            titulo: ordenacion.titulo,
            descripcion: ordenacion.descripcion,
            puntuacion: ordenacion.puntuacion,
            imagen: ordenacion.imagen,
            respVisible: ordenacion.respVisible,
            comentariosRespVisible: ordenacion.comentariosRespVisible,
            posicion: ordenacion.posicion,
            temaId: ordenacion.temaId,
            valores: ordenacion.valores ?? [],
        } : undefined;

    const teoriaInitialValues: TeoriaFormInitialValues | undefined = teoria
        ? {
            titulo: teoria.titulo,
            descripcion: teoria.descripcion,
            imagen: teoria.imagen,
            posicion: teoria.posicion,
            temaId: teoria.temaId,
        } : undefined;

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
            temaId: generalCarta.temaId,
            preguntas: generalCarta.preguntas ?? [],
        } : undefined;

    const marcarImagenInitialValues: MarcarImagenFormInitialValues | undefined = marcarImagen
        ? {
            titulo: marcarImagen.titulo,
            descripcion: marcarImagen.descripcion,
            puntuacion: marcarImagen.puntuacion,
            respVisible: marcarImagen.respVisible,
            comentariosRespVisible: marcarImagen.comentariosRespVisible,
            temaId: marcarImagen.temaId,
            imagenAMarcar: marcarImagen.imagenAMarcar,
            puntosImagen: (marcarImagen.puntosImagen ?? []).map((p) => ({
                id: p.id,
                respuesta: p.respuesta,
                pixelX: p.pixelX,
                pixelY: p.pixelY,
            })),
        } : undefined;

    const clasificacionInitialValues: ClasificacionFormInitialValues | undefined = clasificacion
        ? {
            titulo: clasificacion.titulo,
            descripcion: clasificacion.descripcion,
            puntuacion: clasificacion.puntuacion,
            respVisible: clasificacion.respVisible,
            comentariosRespVisible: clasificacion.comentariosRespVisible,
            posicion: clasificacion.posicion,
            version: clasificacion.version,
            temaId: clasificacion.temaId,
            preguntas: clasificacion.preguntas ?? [],
        } : undefined;

    // --- RENDER ---

    const renderForm = () => {
        const idNum = Number(actividadId);
        switch (kind) {
            case 'preguntaAbierta':
                return <PreguntaAbiertaForm mode="edit" preguntaAbiertaId={idNum} initialValues={preguntaAbiertaInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'test':
                return <TestForm mode="edit" generalId={idNum} initialValues={testInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'crucigrama':
                return <CrucigramaForm mode="edit" crucigramaId={idNum} initialValues={crucigramaInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'ordenacion':
                return <OrdenacionForm mode="edit" ordenacionId={idNum} initialValues={ordenacionInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'teoria':
                return <TeoriaForm mode="edit" actividadId={idNum} initialValues={teoriaInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'tablero':
                return <TableroForm mode="edit" tableroId={tablero?.id} initialValues={tableroInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'carta':
                return <CartaForm mode="edit" generalId={idNum} initialValues={cartaInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'marcarImagen':
                return <MarcarImagenForm mode="edit" marcarImagenId={idNum} initialValues={marcarImagenInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            case 'clasificacion':
                return <ClasificacionForm mode="edit" clasificacionId={idNum} initialValues={clasificacionInitialValues} temaIdProp={temaId} cursoIdProp={cursoId} onDone={onDone} />;
            default:
                return <p className="ca-text">Tipo de actividad no reconocido.</p>;
        }
    };

    return (
        <div className={embedded ? 'ca-embedded' : 'ca-page'}>
            {!embedded && <NavbarMisCursos />}
            <main className="ca-main">
                {!embedded && (
                    <div className="ca-sidebar">
                        <button className="ca-sidebar-btn" type="button" onClick={() => navigate(`/cursos/${cursoId}`)}>
                            Volver al Mapa
                        </button>
                    </div>
                )}
                <div className="ca-contenido">
                    {loading && <p className="ca-text">Cargando datos de la actividad...</p>}
                    {!loading && error && <p className="ca-text" style={{ color: 'red' }}>{error}</p>}
                    {!loading && !error && renderForm()}
                </div>
            </main>
        </div>
    );
}