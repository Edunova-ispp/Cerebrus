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

export default function GraficasActividades() {
  const { cursoId } = useParams<{ cursoId: string }>();
  const navigate = useNavigate();

  const [temas, setTemas] = useState<Tema[]>([]);
  const [temaSeleccionado, setTemaSeleccionado] = useState<Tema | null>(null);
  const [mapaEstadisticas, setMapaEstadisticas] = useState<Map<number, EstadisticasActividadDTO>>(new Map());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cargarEstructuraYSeleccion();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cursoId]);

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
          label: limitarLabel(`${idx + 1}. ${a.titulo}`, 18),
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
          label: limitarLabel(`${idx + 1}. ${a.titulo}`, 18),
          value: typeof v === 'number' && Number.isFinite(v) ? v : 0,
        };
      });
  }, [temaSeleccionado, mapaEstadisticas]);

  return (
    <div className="estadisticas-page">
      <NavbarMisCursos />
      <main className="estadisticas-main">
        <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
          ←
        </button>
        <h1 className="estadisticas-titulo-curso">Gráficas de Actividades</h1>

        {loading && <p className="msg-placeholder">Cargando datos...</p>}
        {error && (
          <p className="msg-placeholder" style={{ color: 'red' }}>
            {error}
          </p>
        )}

        {!loading && !error && (
          <div className="layout-estadisticas">
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

            <section className="panel-actividades">
              {temaSeleccionado ? (
                <div className="charts-grid">
                  <h3>{temaSeleccionado.titulo}</h3>
                  {temaSeleccionado.actividades.length === 0 ? (
                    <p className="msg-vacio">Este tema aun no tiene actividades</p>
                  ) : (
                    <>
                      <BarChart titulo="Nota media por actividad" data={datosNotaMedia} />
                      <BarChart titulo="Tiempo medio por actividad" data={datosTiempoMedio} unidad="mins" />
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
