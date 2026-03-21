import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import './EstadisticasCurso.css';

interface Tema {
  id: number;
  titulo: string;
}

interface EstadisticasTemaDTO {
  temaCompletadoPorTodos: boolean | null;
  notaMediaTema: number | null;
  tiempoMedioTema: number | null;
  notaMaximaTema: number | null;
  notaMinimaTema: number | null;
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

export default function GraficasTemas({ cursoIdProp, embedded }: { cursoIdProp?: string; embedded?: boolean } = {}) {
  const params = useParams<{ cursoId: string }>();
  const cursoId = cursoIdProp ?? params.cursoId;
  const navigate = useNavigate();

  const [temas, setTemas] = useState<Tema[]>([]);
  const [mapaEstadisticas, setMapaEstadisticas] = useState<Map<number, EstadisticasTemaDTO>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [chartMode, setChartMode] = useState<'nota' | 'tiempo'>('nota');

  useEffect(() => {
    cargarTodo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cursoId]);

  const cargarTodo = async () => {
    setLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

      const [resTemas, resStats] = await Promise.all([
        fetch(`${apiBase}/api/temas/curso/${cursoId}/maestro`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        fetch(`${apiBase}/api/estadisticas/cursos/${cursoId}/estadisticas-temas`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);

      if (!resTemas.ok) throw new Error('Error al obtener la estructura del curso');
      if (!resStats.ok) throw new Error('Error al obtener estadísticas de temas');

      const temasData = await resTemas.json();
      const temasNormalizados: Tema[] = (temasData as any[]).map((t) => ({
        id: t.id,
        titulo: t.titulo,
      }));

      const statsData: Record<string, EstadisticasTemaDTO> = await resStats.json();
      const nuevoMapa = new Map<number, EstadisticasTemaDTO>();
      Object.entries(statsData).forEach(([temaId, stats]) => {
        nuevoMapa.set(Number(temaId), stats);
      });

      setTemas(temasNormalizados);
      setMapaEstadisticas(nuevoMapa);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
      setTemas([]);
      setMapaEstadisticas(new Map());
    } finally {
      setLoading(false);
    }
  };

  const datosNotaMedia = useMemo<BarDatum[]>(() => {
    return temas
      .slice(0, MAX_BARS)
      .map((t, idx) => {
        const stats = mapaEstadisticas.get(t.id);
        const v = stats?.notaMediaTema;
        return {
          label: String(idx + 1),
          value: typeof v === 'number' && Number.isFinite(v) ? v : 0,
        };
      });
  }, [temas, mapaEstadisticas]);

  const datosTiempoMedio = useMemo<BarDatum[]>(() => {
    return temas
      .slice(0, MAX_BARS)
      .map((t, idx) => {
        const stats = mapaEstadisticas.get(t.id);
        const v = stats?.tiempoMedioTema;
        return {
          label: String(idx + 1),
          value: typeof v === 'number' && Number.isFinite(v) ? v : 0,
        };
      });
  }, [temas, mapaEstadisticas]);

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
          <h2 className="chart-main-title">Gráficas de Temas</h2>
        </div>

        {loading && <p className="msg-placeholder">Cargando datos...</p>}
        {error && (
          <p className="msg-placeholder" style={{ color: 'red' }}>
            {error}
          </p>
        )}

        {!loading && !error && (
          <section className="panel-actividades" style={{ width: '95%', maxWidth: '1100px' }}>
            {temas.length === 0 ? (
              <p className="msg-vacio">Este curso aun no tiene temas</p>
            ) : (
              <div className="charts-grid">
                <div className="chart-header">
                  <span></span>
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
                </div>
                {chartMode === 'nota' ? (
                  <BarChart
                    titulo="Nota media por tema"
                    data={datosNotaMedia}
                    barColor="#D10057"
                    legendBarLabel="Barras: nota media"
                  />
                ) : (
                  <BarChart
                    titulo="Tiempo medio por tema"
                    data={datosTiempoMedio}
                    unidad="mins"
                    barColor="#7C4DFF"
                    legendBarLabel="Barras: tiempo medio"
                  />
                )}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}
