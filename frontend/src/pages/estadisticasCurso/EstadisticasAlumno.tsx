import { useState, useEffect, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiFetch } from '../../utils/api';
import './EstadisticasAlumno.css';

// ===== Types =====

interface IntentoActividad {
  id: number;
  fechaInicio: string;
  fechaFin: string;
  puntuacion: number;
  nota: number;
  tiempoMinutos: number;
  numAbandonos: number;
}

interface ActividadEstadisticas {
  actividadId: number;
  titulo: string;
  tipo: string;
  puntuacionMaxima: number;
  completada: boolean;
  notaAlumno: number | null;
  puntuacionAlumno: number | null;
  notaMediaClase: number;
  desviacion: number | null;
  intentos: IntentoActividad[];
}

interface TemaEstadisticas {
  temaId: number;
  titulo: string;
  completado: boolean;
  actividades: ActividadEstadisticas[];
}

interface TemaResumen {
  notaActual: number | null;
  notaMin: number | null;
  notaMax: number | null;
  numActividadesCompletadas: number;
  totalActividades: number;
  tiempoTotalMinutos: number;
}

interface TemaNotaChartDatum {
  label: string;
  titulo: string;
  nota: number;
}

function tituloCortoActividad(titulo: string, maxChars = 12): string {
  const limpio = (titulo ?? '').trim();
  if (!limpio) return 'Actividad';
  if (limpio.length <= maxChars) return limpio;
  return `${limpio.slice(0, maxChars - 1)}…`;
}

interface EstadisticasAlumnoData {
  alumnoId: number;
  nombreAlumno: string;
  notaMedia: number;
  notaMin: number | null;
  notaMax: number | null;
  numActividadesCompletadas: number;
  totalActividades: number;
  tiempoTotalMinutos: number;
  temas: TemaEstadisticas[];
}

// ===== Helpers =====

const TIPO_LABELS: Record<string, string> = {
  GeneralTest: 'Test',
  Ordenacion: 'Ordenación',
  Teoria: 'Teoría',
  MarcarImagen: 'Imagen',
  Tablero: 'Tablero',
  Clasificacion: 'Clasificación',
  Carta: 'Carta',
  Crucigrama: 'Crucigrama',
  Abierta: 'Abierta',
};

function tipoLabel(tipo: string): string {
  return TIPO_LABELS[tipo] ?? tipo;
}

function formatFecha(iso: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return (
    d.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: '2-digit' }) +
    ' ' +
    d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })
  );
}

function formatTiempo(min: number): string {
  if (!min || min <= 0) return '< 1 min';
  if (min === 1) return '1 min';
  return `${min} min`;
}

function formatNota2Dec(nota: number | null | undefined): string {
  if (typeof nota !== 'number' || !Number.isFinite(nota)) return '—';
  return nota.toFixed(2);
}

function roundHalfUp(nota: number | null | undefined): number | null {
  if (typeof nota !== 'number' || !Number.isFinite(nota)) return null;
  return Math.floor(nota + 0.5);
}

function formatNotaActual(nota: number | null | undefined): string {
  const rounded = roundHalfUp(nota);
  return rounded === null ? '—' : String(rounded);
}

function formatSignedNota2Dec(nota: number | null | undefined): string {
  if (typeof nota !== 'number' || !Number.isFinite(nota)) return '—';
  return `${nota >= 0 ? '+' : ''}${nota.toFixed(2)}`;
}

function calcularNotaSobre10(puntuacion: number | null | undefined, puntuacionMaxima: number | null | undefined): number | null {
  if (typeof puntuacion !== 'number' || !Number.isFinite(puntuacion)) return null;
  if (typeof puntuacionMaxima !== 'number' || !Number.isFinite(puntuacionMaxima) || puntuacionMaxima <= 0) return null;
  return (puntuacion / puntuacionMaxima) * 10;
}

function notaBadgeClass(nota: number | null, media: number): string {
  if (nota === null) return 'ea-nota-badge ea-nota-badge--none';
  if (nota >= media + 1) return 'ea-nota-badge ea-nota-badge--above';
  if (nota <= media - 1) return 'ea-nota-badge ea-nota-badge--below';
  return 'ea-nota-badge ea-nota-badge--avg';
}

function calcularEstadoSemaforo(nota: number | null): 'riesgo' | 'atencion' | 'bien' | null {
  if (typeof nota !== 'number' || !Number.isFinite(nota)) return null;
  if (nota < 5) return 'riesgo';
  if (nota < 7) return 'atencion';
  return 'bien';
}

function labelEstadoSemaforo(estado: 'riesgo' | 'atencion' | 'bien' | null): string {
  if (estado === 'riesgo') return 'Riesgo';
  if (estado === 'atencion') return 'Atención';
  if (estado === 'bien') return 'Bien';
  return '—';
}

function obtenerResumenTema(tema: TemaEstadisticas): TemaResumen {
  // Calcular nota de cada actividad en escala 0-10, limitada al rango [0, 10]
  const notasActividades = tema.actividades
    .map(a => calcularNotaSobre10(a.puntuacionAlumno, a.puntuacionMaxima) ?? a.notaAlumno)
    .filter((n): n is number => typeof n === 'number' && Number.isFinite(n))
    .map(n => Math.max(0, Math.min(10, n)));

  const notasCompletadas = tema.actividades
    .filter(a => a.completada)
    .map(a => calcularNotaSobre10(a.puntuacionAlumno, a.puntuacionMaxima) ?? a.notaAlumno)
    .filter((n): n is number => typeof n === 'number' && Number.isFinite(n))
    .map(n => Math.max(0, Math.min(10, n)));

  const totalActividades = tema.actividades.length;
  const sumaTodasLasActividades = notasActividades.reduce((acc, n) => acc + n, 0);

  // La nota actual del tema se calcula sobre TODAS las actividades del tema.
  const notaActual = notasActividades.length > 0 ? sumaTodasLasActividades / notasActividades.length : null;
  const notaMin = notasCompletadas.length > 0 ? Math.min(...notasCompletadas) : null;
  const notaMax = notasCompletadas.length > 0 ? Math.max(...notasCompletadas) : null;

  const tiempoTotalMinutos = tema.actividades.reduce((acc, a) => {
    const minutosActividad = a.intentos.reduce((s, intento) => {
      if (typeof intento.tiempoMinutos !== 'number' || !Number.isFinite(intento.tiempoMinutos) || intento.tiempoMinutos <= 0) {
        return s;
      }
      return s + intento.tiempoMinutos;
    }, 0);
    return acc + minutosActividad;
  }, 0);

  const numActividadesCompletadas = tema.actividades.filter(a => a.completada).length;

  return {
    notaActual,
    notaMin,
    notaMax,
    numActividadesCompletadas,
    totalActividades,
    tiempoTotalMinutos,
  };
}

function TemaNotasChart({
  data,
  title,
  ariaLabel,
}: {
  data: TemaNotaChartDatum[];
  title: string;
  ariaLabel: string;
}) {
  if (data.length === 0) return null;

  const height = 220;
  const margin = { top: 20, right: 16, bottom: 62, left: 16 };
  const chartHeight = height - margin.top - margin.bottom;
  const barWidth = 30;
  const gap = 12;
  const width = Math.max(560, margin.left + margin.right + data.length * barWidth + Math.max(0, data.length - 1) * gap);
  const yScaleMax = 10;

  return (
    <div className="ea-tema-chart">
      <h4 className="ea-tema-chart-title">{title}</h4>
      <div className="ea-tema-chart-scroll" role="img" aria-label={ariaLabel}>
        <svg width={width} height={height}>
          {[0, 5, 10].map(tick => {
            const y = margin.top + (chartHeight - (tick / yScaleMax) * chartHeight);
            return (
              <g key={tick}>
                <line x1={margin.left - 4} y1={y} x2={width - margin.right + 4} y2={y} stroke="#e5e7eb" strokeWidth={1} />
                <text x={4} y={y + 4} fontSize="10" fill="#6b7280" fontFamily="'Oxygen', sans-serif">{tick}</text>
              </g>
            );
          })}

          {data.map((d, idx) => {
            const notaClamped = Math.max(0, Math.min(10, d.nota));
            const x = margin.left + idx * (barWidth + gap);
            const barH = (notaClamped / yScaleMax) * chartHeight;
            const y = margin.top + (chartHeight - barH);
            const center = x + barWidth / 2;

            return (
              <g key={`${d.label}-${idx}`}>
                <title>{`${d.titulo}: ${formatNota2Dec(notaClamped)}/10`}</title>
                <rect x={x} y={y} width={barWidth} height={barH} rx={5} fill="#86efac" stroke="#16a34a" strokeWidth={1.2} />
                <text x={center} y={y - 5} textAnchor="middle" fontSize="10" fill="#1f2937" fontFamily="'Oxygen', sans-serif">
                  {formatNota2Dec(notaClamped)}
                </text>
                <text x={center} y={height - 16} textAnchor="middle" fontSize="10" fill="#4b5563" fontFamily="'Oxygen', sans-serif" fontWeight="700">
                  {d.label}
                </text>
              </g>
            );
          })}

          <line
            x1={margin.left - 4}
            y1={margin.top + chartHeight}
            x2={width - margin.right + 4}
            y2={margin.top + chartHeight}
            stroke="#94a3b8"
            strokeWidth={1.2}
          />
        </svg>
      </div>
    </div>
  );
}

// ===== Props =====

interface EstadisticasAlumnoProps {
  readonly cursoIdProp?: string;
  readonly alumnoId?: number;
  readonly embedded?: boolean;
}

export default function EstadisticasAlumno({ cursoIdProp, alumnoId }: EstadisticasAlumnoProps = {}) {
  const params = useParams<{ id: string }>();
  const id = cursoIdProp ?? params.id;
  const navigate = useNavigate();

  const [data, setData] = useState<EstadisticasAlumnoData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedTemas, setExpandedTemas] = useState<Set<number>>(new Set());
  const [expandedActividades, setExpandedActividades] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (!id || !alumnoId) return;

    const controller = new AbortController();

    const cargar = async () => {
      setLoading(true);
      setData(null);
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
      try {
        const res = await apiFetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos/${alumnoId}`);
        if (!res.ok) throw new Error('Error al cargar estadísticas del alumno');
        const d: EstadisticasAlumnoData = await res.json();
        if (controller.signal.aborted) return;
        setData(d);
        setExpandedTemas(new Set(d.temas.map(t => t.temaId)));
        setExpandedActividades(new Set());
        setError(null);
      } catch (err) {
        if (!controller.signal.aborted) {
          setError((err as Error).message);
        }
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };

    cargar();
    return () => controller.abort();
  }, [id, alumnoId]);

  const toggleTema = (temaId: number) => {
    setExpandedTemas(prev => {
      const s = new Set(prev);
      if (s.has(temaId)) s.delete(temaId); else s.add(temaId);
      return s;
    });
  };

  const toggleActividad = (actId: number) => {
    setExpandedActividades(prev => {
      const s = new Set(prev);
      if (s.has(actId)) s.delete(actId); else s.add(actId);
      return s;
    });
  };

  const irDetalleIntento = (actividadId: number, intentoId: number) => {
    if (!id || !alumnoId) return;
    navigate(`/estadisticas/cursos/${id}/alumnos/${alumnoId}/actividades/${actividadId}/intentos/${intentoId}`);
  };

  const statCards = useMemo(() => {
    if (!data) return null;

    // Calcular resúmenes de todos los temas
    const resumenesTemas = data.temas.map(obtenerResumenTema);

    // Nota actual global: promedio de notas actuales de todos los temas
    const notaActualesTemas = resumenesTemas
      .map(resumen => resumen.notaActual)
      .filter((nota): nota is number => typeof nota === 'number' && Number.isFinite(nota));

    const notaActualGlobal = notaActualesTemas.length > 0
      ? Math.max(0, Math.min(10, notaActualesTemas.reduce((acc, n) => acc + n, 0) / notaActualesTemas.length))
      : null;

    // Nota máxima global: máximo de todas las notas máximas de cada tema
    const notasMaximasTemas = resumenesTemas
      .map(r => r.notaMax)
      .filter((nota): nota is number => typeof nota === 'number' && Number.isFinite(nota));
    const notaMaxGlobal = notasMaximasTemas.length > 0 ? Math.max(...notasMaximasTemas) : null;

    // Nota mínima global: mínimo de todas las notas mínimas de cada tema
    const notasMinimasTemas = resumenesTemas
      .map(r => r.notaMin)
      .filter((nota): nota is number => typeof nota === 'number' && Number.isFinite(nota));
    const notaMinGlobal = notasMinimasTemas.length > 0 ? Math.min(...notasMinimasTemas) : null;

    const notaActualRedondeada = typeof notaActualGlobal === 'number' ? notaActualGlobal : null;
    const estadoSemaforo = calcularEstadoSemaforo(notaActualRedondeada);

    return [
      { label: 'Nota actual', value: formatNotaActual(notaActualGlobal), mod: 'media', estado: estadoSemaforo },
      { label: 'Nota mínima', value: notaMinGlobal !== null ? formatNota2Dec(notaMinGlobal) : '—', mod: 'min' },
      { label: 'Nota máxima', value: notaMaxGlobal !== null ? formatNota2Dec(notaMaxGlobal) : '—', mod: 'max' },
      { label: 'Actividades', value: `${data.numActividadesCompletadas}/${data.totalActividades}`, mod: 'completadas' },
      { label: 'Tiempo total', value: formatTiempo(data.tiempoTotalMinutos), mod: 'tiempo' },
    ];
  }, [data]);

  if (!alumnoId) {
    return <div className="stats-placeholder">Selecciona un alumno de la lista</div>;
  }

  if (loading) return <div className="stats-info-msg">Cargando estadísticas...</div>;
  if (error) return <div className="stats-error-msg">{error}</div>;
  if (!data) return null;

  const content = (
    <>
      <div className="ea-alumno-header">
        <span className="ea-alumno-nombre">{data.nombreAlumno}</span>
        <span className="ea-alumno-subtitle">Estadísticas individuales</span>
      </div>

      {statCards && (
        <div className="stat-cards-row">
          {statCards.map(c => (
            <div key={c.mod} className={`stat-card stat-card--${c.mod}`}>
              <span className="stat-card__label">{c.label}</span>
              <span className="stat-card__value">{c.value}</span>
              {c.mod === 'media' && c.estado && (
                <span className={`analisis-semaforo-cell analisis-semaforo-cell--${c.estado}`}>
                  {labelEstadoSemaforo(c.estado)}
                </span>
              )}
            </div>
          ))}
        </div>
      )}

      {data.temas.map(tema => {
        const resumenTema = obtenerResumenTema(tema);
        const chartData: TemaNotaChartDatum[] = tema.actividades.map(act => {
          const notaActividad = calcularNotaSobre10(act.puntuacionAlumno, act.puntuacionMaxima) ?? act.notaAlumno;
          return {
            label: tituloCortoActividad(act.titulo),
            titulo: act.titulo,
            nota: typeof notaActividad === 'number' && Number.isFinite(notaActividad) ? notaActividad : 0,
          };
        });
        const temaResumenItems = [
          { label: 'Actual', value: resumenTema.notaActual !== null ? formatNotaActual(resumenTema.notaActual) : '—' },
          { label: 'Mín', value: resumenTema.notaMin !== null ? formatNota2Dec(resumenTema.notaMin) : '—' },
          { label: 'Máx', value: resumenTema.notaMax !== null ? formatNota2Dec(resumenTema.notaMax) : '—' },
          { label: 'Act', value: `${resumenTema.numActividadesCompletadas}/${resumenTema.totalActividades}` },
          { label: 'Tiempo', value: formatTiempo(resumenTema.tiempoTotalMinutos) },
        ];

        return (
        <div key={tema.temaId} className="ea-tema-section">
          <button
            type="button"
            className={`ea-tema-header${tema.completado ? ' ea-tema-header--done' : ''}`}
            onClick={() => toggleTema(tema.temaId)}
          >
            <span className="ea-tema-arrow">{expandedTemas.has(tema.temaId) ? '▾' : '▸'}</span>
            <span className="ea-tema-title">{tema.titulo}</span>
            <span className="ea-tema-summary" aria-label="Resumen del tema">
              {temaResumenItems.map(item => (
                <span key={`${tema.temaId}-${item.label}`} className="ea-tema-chip">
                  <span className="ea-tema-chip-label">{item.label}</span>
                  <span className="ea-tema-chip-value">{item.value}</span>
                </span>
              ))}
            </span>
            <span className={`ea-tema-badge${tema.completado ? ' ea-tema-badge--done' : ' ea-tema-badge--pending'}`}>
              {tema.completado ? '✓ Completado' : 'En progreso'}
            </span>
          </button>

          {expandedTemas.has(tema.temaId) && (
            <div className="ea-actividades-list">
              {tema.actividades.length === 0 ? (
                <p className="ea-empty">Sin actividades</p>
              ) : (
                tema.actividades.map(act => {
                  const notaAlumnoCalculada = calcularNotaSobre10(act.puntuacionAlumno, act.puntuacionMaxima);
                  const notaAlumnoMostrada = notaAlumnoCalculada ?? act.notaAlumno;

                  return (
                    <div key={act.actividadId} className="ea-actividad-block">
                      <button
                        type="button"
                        className={`ea-actividad-row${act.completada ? ' ea-actividad-row--done' : ' ea-actividad-row--pending'}`}
                        onClick={() => toggleActividad(act.actividadId)}
                      >
                        <span className="ea-act-tipo-badge">{tipoLabel(act.tipo)}</span>
                        <span className="ea-act-titulo">{act.titulo}</span>
                        <span className="ea-act-stats">
                          {act.completada && notaAlumnoMostrada !== null ? (
                            <>
                              <span className={notaBadgeClass(notaAlumnoMostrada, act.notaMediaClase)}>
                                {formatNota2Dec(notaAlumnoMostrada)}/10
                              </span>
                              <span className="ea-act-media">Media: {formatNota2Dec(act.notaMediaClase)}</span>
                              {act.desviacion !== null && (
                                <span className={`ea-act-desv${act.desviacion >= 0 ? ' ea-act-desv--pos' : ' ea-act-desv--neg'}`}>
                                  {formatSignedNota2Dec(act.desviacion)}
                                </span>
                              )}
                            </>
                          ) : (
                            <span className="ea-act-no-completada">Sin completar</span>
                          )}
                          {act.intentos.length > 0 && (
                            <span className="ea-act-intentos-badge">
                              {act.intentos.length} {act.intentos.length === 1 ? 'intento' : 'intentos'}
                            </span>
                          )}
                          <span className="ea-act-expand-arrow">
                            {expandedActividades.has(act.actividadId) ? '▴' : '▾'}
                          </span>
                        </span>
                      </button>

                      {expandedActividades.has(act.actividadId) && (
                        <div className="ea-intentos-list">
                          {act.intentos.length === 0 ? (
                            <p className="ea-empty">Sin intentos registrados</p>
                          ) : (
                            act.intentos.map((intento, idx) => {
                              const notaIntentoCalculada = calcularNotaSobre10(intento.puntuacion, act.puntuacionMaxima);
                              const notaIntentoMostrada = notaIntentoCalculada ?? intento.nota;

                              return (
                                <button
                                  key={intento.id}
                                  type="button"
                                  className="ea-intento-row ea-intento-row--clickable"
                                  onClick={() => irDetalleIntento(act.actividadId, intento.id)}
                                >
                                  <span className="ea-intento-num">#{idx + 1}</span>
                                  <span className="ea-intento-fecha">
                                    {formatFecha(intento.fechaFin || intento.fechaInicio)}
                                  </span>
                                  <span className="ea-intento-nota">
                                    {notaIntentoMostrada !== null ? `${formatNota2Dec(notaIntentoMostrada)}/10` : '—'}
                                  </span>
                                  <span className="ea-intento-puntos">
                                    {intento.puntuacion !== null ? `${intento.puntuacion} pts` : '—'}
                                  </span>
                                  <span className="ea-intento-tiempo">
                                    {formatTiempo(intento.tiempoMinutos)}
                                  </span>
                                  {intento.numAbandonos > 0 && (
                                    <span className="ea-intento-abandonos">
                                      {intento.numAbandonos} abandono{intento.numAbandonos !== 1 ? 's' : ''}
                                    </span>
                                  )}
                                </button>
                              );
                            })
                          )}
                        </div>
                      )}
                    </div>
                  );
                })
              )}

              {tema.actividades.length > 0 && (
                <TemaNotasChart
                  data={chartData}
                  title="Notas del alumno por actividad"
                  ariaLabel="Gráfica de notas del alumno por actividad del tema"
                />
              )}
            </div>
          )}
        </div>
      );})}

      <TemaNotasChart
        data={data.temas.map(tema => {
          const resumenTema = obtenerResumenTema(tema);
          return {
            label: tituloCortoActividad(tema.titulo, 14),
            titulo: tema.titulo,
            nota: typeof resumenTema.notaActual === 'number' && Number.isFinite(resumenTema.notaActual)
              ? resumenTema.notaActual
              : 0,
          };
        })}
        title="Notas del alumno por tema"
        ariaLabel="Gráfica de notas del alumno por tema"
      />
    </>
  );

  return <div className="estadisticas-yellow-card ea-root">{content}</div>;
}