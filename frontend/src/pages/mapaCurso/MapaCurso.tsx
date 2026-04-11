import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { getCurrentUserInfo } from '../../types/curso';
import { apiFetch, fetchProgresoAlumno } from '../../utils/api';
import './MapaCurso.css';

// --- ICONOS DE ACTIVIDADES ---
import abiertaMapIcon from '../../assets/props/mapa/act_mapa_abierta.svg';
import cartaMapIcon from '../../assets/props/mapa/act_mapa_carta.svg';
import clasificacionMapIcon from '../../assets/props/mapa/act_mapa_clasificacion.svg';
import crucigramaMapIcon from '../../assets/props/mapa/act_mapa_crucigrama.svg';
import imagenMapIcon from '../../assets/props/mapa/act_mapa_imagen.svg';
import inicialMapIcon from '../../assets/props/mapa/act_mapa_inicial.svg';
import ordenMapIcon from '../../assets/props/mapa/act_mapa_orden.svg';
import tableroMapIcon from '../../assets/props/mapa/act_mapa_tablero.svg';
import teoriaMapIcon from '../../assets/props/mapa/act_mapa_teoria.svg';
import testMapIcon from '../../assets/props/mapa/act_mapa_test.svg';

import niv3 from '../../assets/props/Cerbero_despierto_niv3.png';
import niv2 from '../../assets/props/Guardian_de_la_primera_cabeza_niv2.png';
import niv1 from '../../assets/props/perritoCerberito.png';
import niv5 from '../../assets/props/Rey_de_cerebrus_niv5.png';
import niv4 from '../../assets/props/Vigia_de_las_tres_mentes_niv4.png';


type ActividadDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly posicion: number;
  readonly tipo: string;
};

type CompletionInfo = {
  done: boolean;
  terminada: boolean;
  puntuacionObtenida?: number;
};

type TemaDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly actividades: ActividadDTO[];
};

// Determina el icono según el tipo de actividad
function getActivityIconSrc(tipo: string, posicion: number): string {
  const tipoUpper = (tipo ?? '').toUpperCase();
  if (tipoUpper.includes('TEORIA')) return teoriaMapIcon;
  if (tipoUpper.includes('TEST') || tipoUpper.includes('GENERAL')) return testMapIcon;
  if (tipoUpper.includes('ORDENACION')) return ordenMapIcon;
  if (tipoUpper.includes('TABLERO')) return tableroMapIcon;
  if (tipoUpper.includes('CRUCIGRAMA')) return crucigramaMapIcon;
  if (tipoUpper.includes('CLASIFICACION')) return clasificacionMapIcon;
  if (tipoUpper.includes('IMAGEN')) return imagenMapIcon;
  if (tipoUpper.includes('CARTA')) return cartaMapIcon;
  if (tipoUpper.includes('ABIERTA')) return abiertaMapIcon;
  if (posicion === 0) return inicialMapIcon;
  return abiertaMapIcon;
}

function getMascotaEvolucionada(puntos: number): string {
  if (puntos <= 100) return niv1;
  if (puntos <= 300) return niv2;
  if (puntos <= 600) return niv3;
  if (puntos <= 1000) return niv4;
  return niv5;
}

function getNodeBgColor(index: number): string {
  return index % 2 === 0 ? '#D10057' : '#7C4DFF';
}

function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo();
  if (!info) return null;
  const infoObj = info as { id?: string | number; userId?: string | number; sub?: string | number };
  const raw = infoObj?.id ?? infoObj?.userId ?? infoObj?.sub;
  return typeof raw === 'string' ? Number(raw) : (typeof raw === 'number' ? raw : null);
}

export default function MapaCurso() {
  const { id: cursoId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [temas, setTemas] = useState<TemaDTO[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [puntosAlumno, setPuntosAlumno] = useState(0); // <-- NUEVO ESTADO PARA LOS PUNTOS

  const mapContainerRef = useRef<HTMLOListElement | null>(null);
  const [mapRowSize, setMapRowSize] = useState(6);
  const [completionMap, setCompletionMap] = useState<Map<number, CompletionInfo>>(new Map());
  const [loadingCompletion, setLoadingCompletion] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);

  // Responsividad del mapa
  useEffect(() => {
    const el = mapContainerRef.current;
    if (!el) return;
    const compute = () => {
      const width = el.getBoundingClientRect().width;
      const nextSize = width >= 980 ? 6 : width >= 820 ? 5 : width >= 640 ? 4 : 3;
      setMapRowSize(nextSize);
    };
    compute();
    const ro = new ResizeObserver(() => compute());
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  // Carga de datos iniciales (Temas + Puntos del Alumno)
  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    if (!cursoId) return;

    const cargarTodo = async () => {
      setLoading(true);
      setError('');
      const alumnoId = getCurrentUserIdFromJwt();
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // 1. Intentar traer puntos (bloque independiente)
      if (alumnoId) {
        try {
          const resAl = await apiFetch(`${apiBase}/api/alumnos/mi-puntuacion-total`);
          if (resAl.ok) {
            const dataAl = await resAl.json();
            // Asegúrate de que el backend devuelve un objeto con la propiedad 'puntos'
            setPuntosAlumno(dataAl || 0);
          }
        } catch (e) {
          // Si falla, solo avisamos por consola pero seguimos adelante
          console.warn('[MapaCurso] No se pudo cargar la evolución, usando nivel inicial.', e);
          setPuntosAlumno(0);
        }
      }

      // 2. Cargar el resto del mapa (temas y progreso)
      try {
        const prog = await fetchProgresoAlumno(Number(cursoId));
        if (prog.estado === 'TERMINADA') {
          navigate('/misCursos', { replace: true });
          return;
        }

        const resTemas = await apiFetch(`${apiBase}/api/temas/curso/${cursoId}/alumno`);
        const dataTemas = await resTemas.json();
        setTemas(Array.isArray(dataTemas) ? dataTemas : []);
        setSelectedIndex(0);

      } catch (e) {
        console.error('[MapaCurso] Error cargando datos del mapa', e);
        setError('No se pudo cargar la información del curso.');
      } finally {
        setLoading(false);
      }
    };

    cargarTodo();
  }, [cursoId, navigate]);

  // Lógica de carga de completitud de actividades (checks)
  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    const tema = temas[selectedIndex];
    const alumnoId = getCurrentUserIdFromJwt();

    if (!tema || tema.actividades.length === 0 || !alumnoId) {
      setLoadingCompletion(false);
      return;
    }

    setLoadingCompletion(true);
    Promise.all(
      tema.actividades.map((act) =>
        apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${act.id}`)
          .then(async (r) => {
            if (r.status === 404) return { id: act.id, info: { done: false, terminada: false } };
            const data = await r.json();
            return {
              id: act.id,
              info: {
                done: true,
                terminada: !!data.fechaFin && data.fechaFin !== "1970-01-01T00:00:00",
                puntuacionObtenida: data.puntuacion
              }
            };
          })
          .catch(() => ({ id: act.id, info: { done: false, terminada: false } }))
      )
    ).then((results) => {
      setCompletionMap((prev) => {
        const next = new Map(prev);
        results.forEach(({ id, info }) => next.set(id, info));
        return next;
      });
      setLoadingCompletion(false);
    });
  }, [temas, selectedIndex, refreshKey]);

  useEffect(() => {
    const handleFocus = () => {
      setCompletionMap(new Map());
      setRefreshKey((k) => k + 1);
    };
    window.addEventListener('focus', handleFocus);
    return () => window.removeEventListener('focus', handleFocus);
  }, []);

  const handleActivityClick = (act: ActividadDTO) => {
    const tipoReal = act.tipo ? act.tipo.toUpperCase() : '';
    const rutas: Record<string, string> = {
      'TEORIA': `/actividades/teoria/${act.id}`,
      'TEST': `/generales/test/${act.id}/alumno`,
      'GENERAL': `/generales/test/${act.id}/alumno`,
      'ORDENACION': `/ordenaciones/${act.id}/alumno`,
      'TABLERO': `/tableros/${act.id}/alumno`,
      'CARTA': `/generales/carta/${act.id}/alumno`,
      'MARCARIMAGEN': `/marcar-imagenes/${act.id}/alumno`,
      'CLASIFICACION': `/clasificaciones/${act.id}/alumno`,
      'CRUCIGRAMA': `/crucigrama/${act.id}/alumno`,
      'ABIERTA': `/abierta/${act.id}/alumno`,
    };
    if (rutas[tipoReal]) navigate(rutas[tipoReal]);
  };

  const selectedTema = temas[selectedIndex] ?? null;

  const sortedActividades = useMemo(
    () => (selectedTema ? [...selectedTema.actividades].sort((a, b) => a.posicion - b.posicion) : []),
    [selectedTema]
  );

  const actividadRows = useMemo(() => {
    const rows: ActividadDTO[][] = [];
    const size = Math.max(1, mapRowSize);
    for (let i = 0; i < sortedActividades.length; i += size) {
      rows.push(sortedActividades.slice(i, i + size));
    }
    return rows;
  }, [sortedActividades, mapRowSize]);

  const unlockedActivityIds = useMemo(() => {
    const ids = new Set<number>();
    if (sortedActividades.length === 0) return ids;

    const states = sortedActividades.map((act) => {
      const info = completionMap.get(act.id);
      return info?.terminada ? 'terminada' : (info?.done ? 'iniciada' : 'pendiente');
    });

    const firstNotCompletedIndex = states.findIndex((s) => s !== 'terminada');
    if (firstNotCompletedIndex === -1) {
      sortedActividades.forEach(a => ids.add(a.id));
      return ids;
    }
    for (let i = 0; i <= firstNotCompletedIndex; i++) ids.add(sortedActividades[i].id);
    return ids;
  }, [sortedActividades, completionMap]);

  const mascotaActivityId = useMemo(() => {
    if (sortedActividades.length === 0) return null;
    const states = sortedActividades.map((act) => {
      const info = completionMap.get(act.id);
      return info?.terminada ? 'terminada' : 'pendiente';
    });
    const firstNotCompletedIndex = states.findIndex((s) => s !== 'terminada');
    return firstNotCompletedIndex === -1 
      ? sortedActividades[sortedActividades.length - 1].id 
      : sortedActividades[firstNotCompletedIndex].id;
  }, [sortedActividades, completionMap]);

  return (
    <div className="mapa-page">
      <NavbarMisCursos />
      <main className="mapa-main">
        <button className="detalle-volver" onClick={() => navigate(-1)}>←</button>
        <div className="mapa-layout">
          <aside className="mapa-sidebar">
            <h2 className="mapa-sidebar-title">Temas</h2>
            {temas.map((tema, idx) => (
              <button
                key={tema.id}
                className={`mapa-tema-btn${idx === selectedIndex ? ' mapa-tema-btn--active' : ''}`}
                onClick={() => setSelectedIndex(idx)}
              >
                {tema.titulo}
              </button>
            ))}
          </aside>

          <section className="mapa-activities">
            {(loading || loadingCompletion) && <p className="mapa-feedback">Cargando actividades...</p>}
            
            {!loading && !loadingCompletion && error && (
              <p className="mapa-feedback mapa-feedback--error">{error}</p>
            )}

            {selectedTema && !loading && !loadingCompletion && !error && (
              <>
                <h2 className="mapa-activities-title">{selectedTema.titulo}</h2>
                <ol className="mapa-activities-map" ref={mapContainerRef}>
                  {actividadRows.map((row, rowIndex) => {
                    const reverse = rowIndex % 2 === 1;
                    const rowActs = reverse ? [...row].reverse() : row;
                    const firstInRowLinearIndex = rowIndex * mapRowSize;

                    return (
                      <li key={`row-${rowIndex}`} className="mapa-map-row-item">
                        <div className={`mapa-map-row${reverse ? ' mapa-map-row--reverse' : ''}`}>
                          {rowActs.map((act, localIndex) => {
                            const linearIndex = firstInRowLinearIndex + (reverse ? row.length - 1 - localIndex : localIndex);
                            const info = completionMap.get(act.id);
                            const state = info?.terminada ? 'terminada' : (info?.done ? 'iniciada' : 'pendiente');
                            const locked = !unlockedActivityIds.has(act.id);
                            const nodeBg = getNodeBgColor(linearIndex);
                            const iconSrc = getActivityIconSrc(act.tipo, act.posicion);

                            return (
                              <div key={act.id} className="mapa-map-node">
                                <div className="mapa-map-node-anchor">
                                  <button
                                    type="button"
                                    className="mapa-map-node-btn"
                                    aria-disabled={locked}
                                    data-state={state}
                                    data-locked={locked}
                                    style={{ ['--node-bg' as any]: nodeBg }}
                                    onClick={() => !locked && handleActivityClick(act)}
                                  >
                                    <img className="mapa-map-node-icon" src={iconSrc} alt="" />
                                  </button>

                                  {mascotaActivityId === act.id && (
                                    <img
                                      className="mapa-map-mascota"
                                      src={getMascotaEvolucionada(puntosAlumno)}
                                      alt="Cerbero"
                                      aria-hidden="true"
                                    />
                                  )}
                                </div>
                                <span className="mapa-map-tooltip">{act.titulo}</span>
                                
                                {localIndex < rowActs.length - 1 && (
                                  <span
                                    className="mapa-map-connector"
                                    data-locked={!unlockedActivityIds.has(rowActs[localIndex + 1]?.id)} 
                                    style={{ ['--conn-color' as any]: nodeBg }}
                                  />
                                )}
                              </div>
                            );
                          })}
                        </div>
                        {/* Conector de giro entre filas */}
                        {rowIndex < actividadRows.length - 1 && (
                          <div
                            className={`mapa-map-turn${reverse ? ' mapa-map-turn--left' : ' mapa-map-turn--right'}`}
                            data-locked={!unlockedActivityIds.has(sortedActividades[(rowIndex + 1) * mapRowSize]?.id)}
                            style={{ ['--conn-color' as any]: getNodeBgColor(firstInRowLinearIndex + row.length - 1) }}
                          />
                        )}
                      </li>
                    );
                  })}
                </ol>
              </>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}