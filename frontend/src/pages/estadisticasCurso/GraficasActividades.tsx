import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface Actividad {
  id: number;
  titulo: string;
}

interface Tema {
  id: number;
  titulo: string;
  actividades: Actividad[];
}

interface EstadisticasActividadDTO {
  notaMediaActividad: number | null;
  notaMaximaActividad: number | null;
  notaMinimaActividad: number | null;
  tiempoMedioActividad: number | null;
  actividadCompletadaPorTodos: boolean | null;
}

type BarDatum = {
  label: string;
  value: number;
};

const MAX_BARS = 12;

function BarChart({
  titulo,
  data,
  unidad,
  barColor,
  legendBarLabel,
}: {
  titulo: string;
  data: BarDatum[];
  unidad?: string;
  barColor: string;
  legendBarLabel: string;
}) {
  const height = 300;
  const margin = { top: 22, right: 18, bottom: 78, left: 18 };
  const chartHeight = height - margin.top - margin.bottom;

  const maxValue = Math.max(0, ...data.map((d) => (Number.isFinite(d.value) ? d.value : 0)));
  const safeMax = maxValue <= 0 ? 1 : maxValue;

  const barWidth = 46;
  const gap = 14;
  const width = Math.max(640, margin.left + margin.right + data.length * barWidth + Math.max(0, data.length - 1) * gap);

  const media = data.length === 0 ? 0 : data.reduce((acc, d) => acc + (Number.isFinite(d.value) ? d.value : 0), 0) / data.length;
  const mediaClamped = Number.isFinite(media) ? Math.max(0, media) : 0;
  const yMedia = margin.top + (chartHeight - (mediaClamped / safeMax) * chartHeight);

  const progresoPoints = data
    .map((d, idx) => {
      const x = margin.left + idx * (barWidth + gap);
      const v = Number.isFinite(d.value) ? Math.max(0, d.value) : 0;
      const barH = (v / safeMax) * chartHeight;
      const y = margin.top + (chartHeight - barH);
      const xCenter = x + barWidth / 2;
      return `${xCenter},${y}`;
    })
    .join(' ');

  return (
    <div className="chart-block">
      <h3 className="chart-title">{titulo}</h3>
      <div className="chart-scroll-container" role="img" aria-label={titulo}>
        <svg width={width} height={height}>
          {data.map((d, idx) => {
            const x = margin.left + idx * (barWidth + gap);
            const v = Number.isFinite(d.value) ? Math.max(0, d.value) : 0;
            const barH = (v / safeMax) * chartHeight;
            const y = margin.top + (chartHeight - barH);
            const labelX = x + barWidth / 2;
            const valueText = `${Math.round(v * 100) / 100}${unidad ? ` ${unidad}` : ''}`;

            return (
              <g key={`${d.label}-${idx}`}>
                <rect
                  x={x}
                  y={y}
                  width={barWidth}
                  height={barH}
                  fill={barColor}
                  stroke="#000"
                  strokeWidth={2}
                  rx={6}
                />
                <text
                  x={labelX}
                  y={y - 6}
                  textAnchor="middle"
                  fontSize="12"
                  fontFamily="'Pixelify Sans', sans-serif"
                  fill="#000"
                >
                  {valueText}
                </text>
                <text
                  x={labelX}
                  y={height - 14}
                  textAnchor="end"
                  fontSize="20"
                  fontFamily="'Pixelify Sans', sans-serif"
                  fill="#000"
                >
                  {d.label}
                </text>
              </g>
            );
          })}

          {data.length > 1 && (
            <polyline
              points={progresoPoints}
              fill="none"
              stroke="#000"
              strokeWidth={2}
            />
          )}

          {data.length > 0 && (
            <line
              x1={margin.left - 6}
              y1={yMedia}
              x2={width - margin.right + 6}
              y2={yMedia}
              stroke="#FF0000"
              strokeWidth={4}
            />
          )}

          <line
            x1={margin.left - 6}
            y1={margin.top + chartHeight}
            x2={width - margin.right + 6}
            y2={margin.top + chartHeight}
            stroke="#000"
            strokeWidth={2}
          />
        </svg>
      </div>

      <div className="chart-legend" aria-label="Leyenda">
        <span className="legend-item">
          <span className="legend-swatch" style={{ backgroundColor: barColor }} />
          {legendBarLabel}
        </span>
        <span className="legend-item">
          <span className="legend-line" style={{ borderTopColor: '#000', borderTopWidth: 2 }} />
          Línea negra: progreso (tope de cada barra)
        </span>
        <span className="legend-item">
          <span className="legend-line" style={{ borderTopColor: '#FF0000', borderTopWidth: 4 }} />
          Línea roja: media
        </span>
      </div>

      {data.length >= MAX_BARS && (
        <p className="stats-info-msg">Mostrando {MAX_BARS} elementos para que la gráfica no se desborde.</p>
      )}
    </div>
  );
}

export default function GraficasActividades({ cursoIdProp, embedded, temaIdSeleccionado }: { cursoIdProp?: string; embedded?: boolean; temaIdSeleccionado?: number } = {}) {
  const params = useParams<{ cursoId: string }>();
  const cursoId = cursoIdProp ?? params.cursoId;
  const navigate = useNavigate();

  const [temas, setTemas] = useState<Tema[]>([]);
  const [temaSeleccionado, setTemaSeleccionado] = useState<Tema | null>(null);
  const [mapaEstadisticas, setMapaEstadisticas] = useState<Map<number, EstadisticasActividadDTO>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [chartMode, setChartMode] = useState<'nota' | 'tiempo'>('nota');

  useEffect(() => {
    cargarEstructuraYSeleccion();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cursoId]);

  useEffect(() => {
    if (temaIdSeleccionado != null && temas.length > 0) {
      const tema = temas.find(t => t.id === temaIdSeleccionado);
      if (tema && tema.id !== temaSeleccionado?.id) {
        seleccionarTema(tema);
      }
    }
  }, [temaIdSeleccionado, temas]);

  const cargarEstructuraYSeleccion = async () => {
    setLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

      const resEstructura = await fetch(`${apiBase}/api/temas/curso/${cursoId}/maestro`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!resEstructura.ok) throw new Error('Error al obtener la estructura del curso');

      const temasData: Tema[] = await resEstructura.json();
      setTemas(temasData);

      if (temasData.length > 0) {
        const temaInicial = temasData[0];
        setTemaSeleccionado(temaInicial);
        await cargarEstadisticasTema(temaInicial.id);
      } else {
        setTemaSeleccionado(null);
        setMapaEstadisticas(new Map());
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
      setTemas([]);
      setTemaSeleccionado(null);
      setMapaEstadisticas(new Map());
    } finally {
      setLoading(false);
    }
  };

  const cargarEstadisticasTema = async (temaId: number) => {
    const token = localStorage.getItem('token');
    const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

    const res = await fetch(`${apiBase}/api/estadisticas/cursos/${cursoId}/temas/${temaId}/estadisticas-actividades`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    if (!res.ok) throw new Error('Error al obtener estadísticas de actividades');

    const data = await res.json();
    const nuevoMapa = new Map<number, EstadisticasActividadDTO>();
    Object.entries(data as Record<string, EstadisticasActividadDTO>).forEach(([actividadId, stats]) => {
      nuevoMapa.set(Number(actividadId), stats);
    });
    setMapaEstadisticas(nuevoMapa);
  };

  const seleccionarTema = async (tema: Tema) => {
    setTemaSeleccionado(tema);
    setError('');
    try {
      await cargarEstadisticasTema(tema.id);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
      setMapaEstadisticas(new Map());
    }
  };

  const datosNotaMedia = useMemo<BarDatum[]>(() => {
    if (!temaSeleccionado) return [];
    return temaSeleccionado.actividades
      .slice(0, MAX_BARS)
      .map((a, idx) => {
        const stats = mapaEstadisticas.get(a.id);
        const v = stats?.notaMediaActividad;
        return {
          label: String(idx + 1),
          value: typeof v === 'number' && Number.isFinite(v) ? v : 0,
        };
      });
  }, [temaSeleccionado, mapaEstadisticas]);

  const datosTiempoMedio = useMemo<BarDatum[]>(() => {
    if (!temaSeleccionado) return [];
    return temaSeleccionado.actividades
      .slice(0, MAX_BARS)
      .map((a, idx) => {
        const stats = mapaEstadisticas.get(a.id);
        const v = stats?.tiempoMedioActividad;
        return {
          label: String(idx + 1),
          value: typeof v === 'number' && Number.isFinite(v) ? v : 0,
        };
      });
  }, [temaSeleccionado, mapaEstadisticas]);

  return (
    <div className={embedded ? 'graficas-embedded' : 'estadisticas-page'}>
      {!embedded && <NavbarMisCursos />}
      <main className={embedded ? 'graficas-embedded-main' : 'estadisticas-main'}>
        {!embedded && (
          <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
            ← Volver
          </button>
        )}
        <div className="chart-header">
          <h2 className="chart-main-title">Gráficas de Actividades</h2>
        </div>

        {loading && <p className="msg-placeholder">Cargando datos...</p>}
        {error && (
          <p className="msg-placeholder" style={{ color: 'red' }}>
            {error}
          </p>
        )}

        {!loading && !error && (
          <div className="layout-estadisticas">
            {temaIdSeleccionado == null && (
              <aside className="panel-temas">
                <h3>Temas</h3>
                {temas.length === 0 ? (
                  <p className="msg-vacio">Este curso aun no tiene temas</p>
                ) : (
                  <ul className="lista-temas-scroll">
                    {temas.map((tema) => (
                      <li
                        key={tema.id}
                        className={`btn-medias-pixel ${temaSeleccionado?.id === tema.id ? 'active' : ''}`}
                        onClick={() => seleccionarTema(tema)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter' || e.key === ' ') seleccionarTema(tema);
                        }}
                        role="button"
                        tabIndex={0}
                      >
                        {tema.titulo}
                      </li>
                    ))}
                  </ul>
                )}
              </aside>
            )}

            <section className="panel-actividades">
              {temaSeleccionado ? (
                <div className="charts-grid">
                  <div className="chart-header">
                    <h3 className="chart-subtitle">{temaSeleccionado.titulo}</h3>
                    {temaSeleccionado.actividades.length > 0 && (
                      <div className="chart-toggle">
                        <button
                          className={`chart-toggle-btn${chartMode === 'nota' ? ' chart-toggle-btn--active' : ''}`}
                          onClick={() => setChartMode('nota')}
                        >
                          Nota
                        </button>
                        <button
                          className={`chart-toggle-btn${chartMode === 'tiempo' ? ' chart-toggle-btn--active' : ''}`}
                          onClick={() => setChartMode('tiempo')}
                        >
                          Tiempo
                        </button>
                      </div>
                    )}
                  </div>
                  {temaSeleccionado.actividades.length === 0 ? (
                    <p className="msg-vacio">Este tema aun no tiene actividades</p>
                  ) : (
                    <>
                      {chartMode === 'nota' ? (
                        <BarChart
                          titulo="Nota media por actividad"
                          data={datosNotaMedia}
                          barColor="#D10057"
                          legendBarLabel="Barras: nota media"
                        />
                      ) : (
                        <BarChart
                          titulo="Tiempo medio por actividad"
                          data={datosTiempoMedio}
                          unidad="mins"
                          barColor="#7C4DFF"
                          legendBarLabel="Barras: tiempo medio"
                        />
                      )}
                    </>
                  )}
                </div>
              ) : (
                <p className="msg-placeholder">Selecciona un tema</p>
              )}
            </section>
          </div>
        )}
      </main>
    </div>
  );
}
