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
    respVisible: boolean;
    posicion: number;
    version: number;
    temaId: number;
    preguntas: {
        id: number;
        pregunta: string;
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

    useEffect(() => {
        const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
        if (!actividadId) return;

        let cancelled = false;

        const run = async () => {
            setLoading(true);
            setError(null);
            setKind(null);

            let activityTypeHint: string | null = null;
            const temaIdNum = Number.parseInt(String(temaId), 10);
            const actividadIdNum = Number.parseInt(String(actividadId), 10);

            // 1. Obtener el tipo real desde el Tema para evitar pruebas ciegas
            try {
                const temaResponse = await apiFetch(`${apiBase}/api/temas/${temaIdNum}`);
                if (temaResponse.ok) {
                    const temaData = (await temaResponse.json()) as TemaWithActividadesDTO;
                    const meta = temaData.actividades?.find(a => a.id === actividadIdNum);
                    activityTypeHint = meta?.tipo ?? null;
                }
            } catch (err) {
                console.error("Error obteniendo metadatos del tema", err);
            }

            try {
                const tipo = activityTypeHint?.toUpperCase();
                console.log("el tipo es", tipo)

                // 2. Carga Condicional basada en tu controlador Java
                if (tipo === 'ABIERTA') {
                    const r = await apiFetch(`${apiBase}/api/generales/abierta/${actividadId}/maestro`);
                    console.log("paso por aqui?")
                    if (r.ok) {
                        const data = await r.json();
                        if (cancelled) return;
                        setPreguntaAbierta(data);
                        setKind('preguntaAbierta');
                        setLoading(false);
                        return;
                    }
                } 
                
                

                if (!cancelled) setError("No se pudo identificar o cargar la actividad correctamente.");

            } catch (err: any) {
                if (!cancelled) setError("Error de conexión al cargar la actividad.");
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        run();
        return () => { cancelled = true; };
    }, [actividadId, temaId]);

    // --- MAPEO DE VALORES INICIALES ---

    // ✅ FIX: useMemo evita que se cree un objeto nuevo en cada render,
    // lo que hacía que el useEffect de PreguntaAbiertaForm no se disparara
    // correctamente y las preguntas aparecieran vacías al editar.
    const preguntaAbiertaInitialValues = useMemo<PreguntaAbiertaFormInitialValues | undefined>(() => {
        if (!preguntaAbierta) return undefined;
        return {
            titulo: preguntaAbierta.titulo,
            descripcion: preguntaAbierta.descripcion,
            puntuacion: preguntaAbierta.puntuacion,
            respVisible: preguntaAbierta.respVisible,
            posicion: preguntaAbierta.posicion,
            version: preguntaAbierta.version,
            preguntas: (preguntaAbierta.preguntas ?? []).map((p) => ({
                id: p.id,
                pregunta: p.pregunta,
                respuestaId: p.respuestas?.[0]?.id,
                respuesta: p.respuestas?.[0]?.respuesta || '',
            })),
        };
    }, [preguntaAbierta]); // Solo se recrea cuando cambia el dato real del backend

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
            valores: ordenacion.valores ?? [],
        } : undefined;

    const teoriaInitialValues: TeoriaFormInitialValues | undefined = teoria
        ? {
            titulo: teoria.titulo,
            descripcion: teoria.descripcion,
            imagen: teoria.imagen,
            posicion: teoria.posicion,
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