import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso'; // Asegúrate de importar esto
import './MapaCurso.css';

import abiertaMapIcon from '../../assets/props/mapa/act_mapa_abierta.svg';
import cartaMapIcon from '../../assets/props/mapa/act_mapa_carta.svg';
import clasificacionMapIcon from '../../assets/props/mapa/act_mapa_clasificacion.svg';
import crucigramaMapIcon from '../../assets/props/mapa/act_mapa_crucigrama.svg';
import imagenMapIcon from '../../assets/props/mapa/act_mapa_imagen.svg';
import tableroMapIcon from '../../assets/props/mapa/act_mapa_tablero.svg';
import teoriaMapIcon from '../../assets/props/mapa/act_mapa_teoria.svg';
import testMapIcon from '../../assets/props/mapa/act_mapa_test.svg';
import mascotaMapIcon from '../../assets/props/mapa/mapa_mascota.svg';

type ActividadDTO = {
  readonly id: number;
  readonly titulo: string;
  readonly descripcion: string | null;
  readonly puntuacion: number;
  readonly posicion: number;
  readonly tipo: string;
};

// 1. Definimos bien el tipo de la información de progreso
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

function getActivityIconSrc(tipo: string): string {
  const tipoUpper = (tipo ?? '').toUpperCase();
  if (tipoUpper.includes('TEORIA')) return teoriaMapIcon;
  if (tipoUpper.includes('TEST') || tipoUpper.includes('GENERAL')) return testMapIcon;

  if (tipoUpper.includes('ORDENACION')) return tableroMapIcon;
  if (tipoUpper.includes('TABLERO')) return tableroMapIcon;
  if (tipoUpper.includes('CRUCIGRAMA')) return crucigramaMapIcon;
  if (tipoUpper.includes('CLASIFICACION')) return clasificacionMapIcon;
  if (tipoUpper.includes('IMAGEN')) return imagenMapIcon;
  if (tipoUpper.includes('CARTA')) return cartaMapIcon;
  if (tipoUpper.includes('ABIERTA')) return abiertaMapIcon;

  if (tipoUpper === 'TEORIA') return teoriaMapIcon;
  if (tipoUpper === 'TEST' || tipoUpper === 'GENERAL') return testMapIcon;
  if (tipoUpper === 'ORDENACION') return tableroMapIcon;
  return testMapIcon;
}

function getNodeBgColor(index: number): string {
  return index % 2 === 0 ? '#D10057' : 'rgba(124, 77, 255, 0.5)';
}

// Helper para el ID del alumno
function getCurrentUserIdFromJwt(): number | null {
  const info = getCurrentUserInfo();
  if (!info) return null;
  const raw = (info as any)?.id ?? (info as any)?.userId ?? (info as any)?.sub;
  return typeof raw === 'string' ? Number(raw) : raw;
}

export default function MapaCurso() {
  const { id: cursoId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [temas, setTemas] = useState<TemaDTO[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const mapContainerRef = useRef<HTMLOListElement | null>(null);
  const [mapRowSize, setMapRowSize] = useState(6);

  void loading;
  void error;

  // 2. CORRECCIÓN: El Map ahora guarda objetos de tipo CompletionInfo
  const [completionMap, setCompletionMap] = useState<Map<number, CompletionInfo>>(new Map());

  useEffect(() => {
    const el = mapContainerRef.current;
    if (!el) return;

    const win = globalThis as unknown as Window;

    const compute = () => {
      const width = el.getBoundingClientRect().width;
      // Simple responsive breakpoints for how many nodes fit per row.
      // Keep it deterministic and easy to tweak.
      const nextSize = width >= 980 ? 6 : width >= 820 ? 5 : width >= 640 ? 4 : 3;
      setMapRowSize((prev) => (prev === nextSize ? prev : nextSize));
    };

    compute();

    const ResizeObserverCtor = (globalThis as any).ResizeObserver as (typeof ResizeObserver) | undefined;
    if (typeof ResizeObserverCtor !== 'undefined') {
      const ro = new ResizeObserverCtor(() => compute());
      ro.observe(el);
      return () => ro.disconnect();
    }

    win.addEventListener('resize', compute);
    return () => win.removeEventListener('resize', compute);
  }, []);

  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    if (!cursoId) return;
    setLoading(true);
    apiFetch(`${apiBase}/api/temas/curso/${cursoId}/alumno`)
      .then((r) => r.json())
      .then((data: TemaDTO[]) => {
        setTemas(Array.isArray(data) ? data : []);
        setSelectedIndex(0);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [cursoId]);

  useEffect(() => {
    const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
    const tema = temas[selectedIndex];
    if (!tema || tema.actividades.length === 0) return;

    const unchecked = tema.actividades.filter((act) => !completionMap.has(act.id));
    if (unchecked.length === 0) return;

    const alumnoId = getCurrentUserIdFromJwt();
    if (!alumnoId) return;

    Promise.all(
      unchecked.map((act) =>
        apiFetch(`${apiBase}/api/actividades-alumno/alumno/${alumnoId}/actividad/${act.id}`)
          .then(async (r) => {
            if (r.status === 404) return { id: act.id, info: { done: false, terminada: false } };
            const data = await r.json();
            return {
              id: act.id,
              info: {
                done: true,
                terminada: !!data.acabada && data.acabada !== "1970-01-01T00:00:00",
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
    });
  }, [temas, selectedIndex]);

  const handleActivityClick = (act: ActividadDTO) => {
    const tipoReal = act.tipo ? act.tipo.toUpperCase() : '';
    if (tipoReal === 'TEORIA') navigate(`/actividades/teoria/${act.id}`);
    else if (tipoReal === 'TEST' || tipoReal === 'GENERAL') navigate(`/generales/test/${act.id}/alumno`);
    else if (tipoReal === 'ORDENACION') navigate(`/ordenaciones/${act.id}/alumno`);
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

  return (
    <div className="mapa-page">
      <NavbarMisCursos />
      <main className="mapa-main">
        <button className="detalle-volver" onClick={() => navigate(-1)}>← Volver</button>
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
            {selectedTema && (
              <>
                <h2 className="mapa-activities-title">{selectedTema.titulo}</h2>
                <ol className="mapa-activities-map" ref={mapContainerRef}>
                  {actividadRows.map((row, rowIndex) => {
                    const reverse = rowIndex % 2 === 1;
                    const rowActs = reverse ? [...row].reverse() : row;

                    const firstInRowLinearIndex = rowIndex * mapRowSize;
                    const renderedRow = (
                      <li key={`row-${rowIndex}`} className="mapa-map-row-item">
                        <div className={`mapa-map-row${reverse ? ' mapa-map-row--reverse' : ''}`}>
                          {rowActs.map((act, localIndex) => {
                            const linearIndex = firstInRowLinearIndex + (reverse ? row.length - 1 - localIndex : localIndex);

                            const info = completionMap.get(act.id);
                            const done = info?.done ?? false;
                            const terminada = info?.terminada ?? false;
                            const state = terminada ? 'terminada' : done ? 'iniciada' : 'pendiente';
                            const locked = state === 'pendiente';

                            const tipo = (act.tipo ?? '').toUpperCase();
                            const navigableType = ['TEST', 'GENERAL', 'ORDENACION', 'TEORIA'].includes(tipo);

                            const iconSrc = getActivityIconSrc(tipo);
                            const nodeBg = getNodeBgColor(linearIndex);

                            const isLastInRow = localIndex === rowActs.length - 1;
                            const nextAct = !isLastInRow ? rowActs[localIndex + 1] : null;
                            const nextInfo = nextAct ? completionMap.get(nextAct.id) : undefined;
                            const nextDone = nextInfo?.done ?? false;
                            const nextTerminada = nextInfo?.terminada ?? false;
                            const nextState = nextAct
                              ? nextTerminada
                                ? 'terminada'
                                : nextDone
                                  ? 'iniciada'
                                  : 'pendiente'
                              : null;
                            const connectorLocked = nextState === 'pendiente';

                            return (
                              <div key={act.id} className="mapa-map-node">
                                <div className="mapa-map-node-anchor">
                                  <button
                                    type="button"
                                    className={`mapa-map-node-btn${navigableType ? '' : ' mapa-map-node-btn--disabled'}`}
                                    aria-disabled={!navigableType || locked}
                                    data-state={state}
                                    data-locked={locked ? 'true' : 'false'}
                                    style={{ ['--node-bg' as any]: nodeBg }}
                                    onClick={() => navigableType && !locked && handleActivityClick(act)}
                                  >
                                    <img
                                      className="mapa-map-node-icon"
                                      src={iconSrc}
                                      alt=""
                                      aria-hidden="true"
                                    />
                                  </button>

                                  {state === 'iniciada' && (
                                    <img
                                      className="mapa-map-mascota"
                                      src={mascotaMapIcon}
                                      alt=""
                                      aria-hidden="true"
                                    />
                                  )}
                                </div>

                                <span className="mapa-map-tooltip" role="tooltip">
                                  {act.titulo}
                                </span>

                                {!isLastInRow && (
                                  <span
                                    className="mapa-map-connector"
                                    aria-hidden="true"
                                    data-locked={connectorLocked ? 'true' : 'false'}
                                    style={{ ['--conn-color' as any]: nodeBg }}
                                  />
                                )}
                              </div>
                            );
                          })}
                        </div>

                        {rowIndex < actividadRows.length - 1 && rowActs.length > 0 && (
                          (() => {
                            const nextRowFirst = sortedActividades[(rowIndex + 1) * mapRowSize];
                            const nextRowFirstInfo = nextRowFirst ? completionMap.get(nextRowFirst.id) : undefined;
                            const nextRowFirstDone = nextRowFirstInfo?.done ?? false;
                            const nextRowFirstTerminada = nextRowFirstInfo?.terminada ?? false;
                            const nextRowFirstState = nextRowFirst
                              ? nextRowFirstTerminada
                                ? 'terminada'
                                : nextRowFirstDone
                                  ? 'iniciada'
                                  : 'pendiente'
                              : null;
                            const turnLocked = nextRowFirstState === 'pendiente';

                            return (
                          <div
                            className={`mapa-map-turn${reverse ? ' mapa-map-turn--left' : ' mapa-map-turn--right'}`}
                            aria-hidden="true"
                            data-locked={turnLocked ? 'true' : 'false'}
                            style={{ ['--conn-color' as any]: getNodeBgColor(firstInRowLinearIndex + row.length - 1) }}
                          />
                            );
                          })()
                        )}
                      </li>
                    );

                    return renderedRow;
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