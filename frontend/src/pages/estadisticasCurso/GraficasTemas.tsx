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

function limitarLabel(texto: string, maxLen: number) {
  const limpio = (texto ?? '').trim();
  if (limpio.length <= maxLen) return limpio;
  return `${limpio.slice(0, Math.max(0, maxLen - 1))}…`;
}

function BarChart({
  titulo,
  data,
  unidad,
}: {
  titulo: string;
  data: BarDatum[];
  unidad?: string;
}) {
  const height = 300;
  const margin = { top: 22, right: 18, bottom: 78, left: 18 };
  const chartHeight = height - margin.top - margin.bottom;

  const maxValue = Math.max(0, ...data.map((d) => (Number.isFinite(d.value) ? d.value : 0)));
  const safeMax = maxValue <= 0 ? 1 : maxValue;

  const barWidth = 46;
  const gap = 14;
  const width = Math.max(640, margin.left + margin.right + data.length * barWidth + Math.max(0, data.length - 1) * gap);

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
                  fill="#FCEF91"
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
                  fontSize="12"
                  fontFamily="'Pixelify Sans', sans-serif"
                  fill="#000"
                  transform={`rotate(-35 ${labelX} ${height - 14})`}
                >
                  {d.label}
                </text>
              </g>
            );
          })}

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
      {data.length >= MAX_BARS && (
        <p className="stats-info-msg">Mostrando {MAX_BARS} elementos para que la gráfica no se desborde.</p>
      )}
    </div>
  );
}

export default function GraficasTemas() {
  const { cursoId } = useParams<{ cursoId: string }>();
  const navigate = useNavigate();

  const [temas, setTemas] = useState<Tema[]>([]);
  const [mapaEstadisticas, setMapaEstadisticas] = useState<Map<number, EstadisticasTemaDTO>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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
          label: limitarLabel(`${idx + 1}. ${t.titulo}`, 18),
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
          label: limitarLabel(`${idx + 1}. ${t.titulo}`, 18),
          value: typeof v === 'number' && Number.isFinite(v) ? v : 0,
        };
      });
  }, [temas, mapaEstadisticas]);

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
          ←
        </button>
        <h1 className="estadisticas-titulo-curso">Gráficas de Temas</h1>

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
                <BarChart titulo="Nota media por tema" data={datosNotaMedia} />
                <BarChart titulo="Tiempo medio por tema" data={datosTiempoMedio} unidad="mins" />
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}
