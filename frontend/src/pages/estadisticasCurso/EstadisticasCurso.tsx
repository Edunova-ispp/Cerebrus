import React, { useEffect, useMemo, useState, useCallback, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import MediasCurso from './MediasCurso';
import EstadisticasActividad from './EstadisticasActividad';
import EstadisticasAlumno from './EstadisticasAlumno';
import EstadisticasActividades from './EstadisticasActividades';
import EstadisticasTemas from './EstadisticasTemas';
import GraficasActividades from './GraficasActividades';
import GraficasTemas from './GraficasTemas';
import './EstadisticasCurso.css';

interface EstadisticaAlumno {
  nombre: string;
  idAlumno?: number; 
  puntos: number;
  actividadesRealizadas: number;
  tiempoTotal: number;
}

interface OpcionItem {
  id: number;
  nombre: string;
  temaId?: number;
}

interface TemaEstructura {
  id: number;
  titulo: string;
  actividades: Array<{ id: number; titulo: string }>;
}

interface TemaStats {
  temaCompletadoPorTodos: boolean | null;
  notaMediaTema: number | null;
  tiempoMedioTema: number | null;
  notaMaximaTema: number | null;
  notaMinimaTema: number | null;
}

interface ActividadStats {
  notaMediaActividad: number | null;
  notaMaximaActividad: number | null;
  notaMinimaActividad: number | null;
  tiempoMedioActividad: number | null;
  actividadCompletadaPorTodos: boolean | null;
}

interface RepeticionesActividadDTO {
  repeticionesMedia: number | null;
  repeticionesMinima: number | null;
  repeticionesMaxima: number | null;
}

type AlumnoSemaforo = {
  alumno: string;
  notaActual: number;
  estado: 'riesgo' | 'atencion' | 'bien';
};

type AnalisisTablaOrden = 'elemento' | 'tipo' | 'notaMedia' | 'notaMaxima' | 'notaMinima' | 'tiempoMedio' | 'completado';
type ResumenTablaOrden = 'alumno' | 'puntos' | 'actividades' | 'tiempo' | 'notaActual';

type EvolucionSemana = { label: string; media: number };

function stdDev(values: number[]): number {
  if (values.length === 0) return 0;
  const mean = values.reduce((a, b) => a + b, 0) / values.length;
  const variance = values.reduce((acc, v) => acc + (v - mean) ** 2, 0) / values.length;
  return Math.sqrt(variance);
}

function weekKeyFromISO(iso: string | null | undefined): string | null {
  if (!iso) return null;
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return null;
  const date = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
  const dayNum = date.getUTCDay() || 7;
  date.setUTCDate(date.getUTCDate() + 4 - dayNum);
  const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
  const weekNum = Math.ceil((((date.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  return `${date.getUTCFullYear()}-W${String(weekNum).padStart(2, '0')}`;
}

type StatsView =
  | { mode: "resumen" }
  | { mode: "analisis" }
  | { mode: "analisisTablas" }
  | { mode: "analisisGraficas" }
  | { mode: "medias"; temaId?: number }
  | { mode: "tiemposTema"; temaId?: number }
  | { mode: "tiemposActividad"; actividadId?: number }
  | { mode: "alumnos" }
  | { mode: "alumnoDetalle"; alumnoId: number; alumnoNombre: string }
  | { mode: "desgloseActividades"; temaId?: number }
  | { mode: "desgloseTemas" }
  | { mode: "graficasActividades"; temaId?: number }
  | { mode: "graficasTemas" }
  | { mode: "tendencias" };

interface EstadisticasCursoDTO {
  cursoCompletadoPorTodos: boolean | null;
  notaMediaCurso: number | null;
  tiempoMedioCurso: number | null;
  notaMaximaCurso: number | null;
  notaMinimaCurso: number | null;
}

interface EstadisticasCursoProps {
  readonly cursoId?: string;
  readonly embedded?: boolean;
}

type HistDatum = { label: string; value: number };
type HeatmapRow = { alumno: string; notas: Array<number | null> };

function quantile(sorted: number[], q: number): number {
  if (sorted.length === 0) return 0;
  const pos = (sorted.length - 1) * q;
  const base = Math.floor(pos);
  const rest = pos - base;
  if (sorted[base + 1] !== undefined) {
    return sorted[base] + rest * (sorted[base + 1] - sorted[base]);
  }
  return sorted[base];
}

function HistogramChart({ title, data }: { title: string; data: HistDatum[] }) {
  const height = 240;
  const margin = { top: 18, right: 16, bottom: 74, left: 18 };
  const chartHeight = height - margin.top - margin.bottom;
  const barWidth = 54;
  const gap = 18;
  const width = Math.max(560, margin.left + margin.right + data.length * barWidth + Math.max(0, data.length - 1) * gap);
  const maxValue = Math.max(1, ...data.map(d => d.value));

  const splitLabelLines = (label: string): string[] => {
    const idxParentesis = label.indexOf(' (');
    if (idxParentesis > 0) {
      return [label.slice(0, idxParentesis), label.slice(idxParentesis + 1)];
    }

    if (label.length <= 12) return [label];

    const partes = label.split(' ');
    if (partes.length < 2) return [label];
    const mitad = Math.ceil(partes.length / 2);
    return [partes.slice(0, mitad).join(' '), partes.slice(mitad).join(' ')];
  };

  return (
    <div className="chart-block">
      <h3 className="chart-title">{title}</h3>
      <div className="chart-scroll-container" role="img" aria-label={title}>
        <svg width={width} height={height}>
          {data.map((d, idx) => {
            const x = margin.left + idx * (barWidth + gap);
            const barH = (d.value / maxValue) * chartHeight;
            const y = margin.top + (chartHeight - barH);
            const center = x + barWidth / 2;

            return (
              <g key={`${d.label}-${idx}`}>
                <rect x={x} y={y} width={barWidth} height={barH} rx={6} fill="#93c5fd" stroke="#2563eb" strokeWidth={1.2} />
                <text x={center} y={y - 6} textAnchor="middle" fontSize="11" fill="#1e293b" fontFamily="'Oxygen', sans-serif">
                  {d.value}
                </text>
                <text x={center} y={height - 36} textAnchor="middle" fontSize="10" fill="#475569" fontWeight="700" fontFamily="'Oxygen', sans-serif">
                  {splitLabelLines(d.label).map((line, lineIdx) => (
                    <tspan key={`${d.label}-${lineIdx}`} x={center} dy={lineIdx === 0 ? 0 : 12}>
                      {line}
                    </tspan>
                  ))}
                </text>
              </g>
            );
          })}
          <line x1={margin.left - 4} y1={margin.top + chartHeight} x2={width - margin.right + 4} y2={margin.top + chartHeight} stroke="#94a3b8" strokeWidth={1.2} />
        </svg>
      </div>
    </div>
  );
}

function ScatterTiempoPuntosChart({ data }: { data: EstadisticaAlumno[] }) {
  const chartData = data.filter(d => d.tiempoTotal > 0 || d.puntos > 0);
  if (chartData.length === 0) {
    return <p className="stats-info-msg">No hay datos suficientes para la dispersión tiempo vs puntos.</p>;
  }

  const width = 720;
  const height = 300;
  const margin = { top: 18, right: 20, bottom: 44, left: 36 };
  const plotW = width - margin.left - margin.right;
  const plotH = height - margin.top - margin.bottom;

  const maxTiempo = Math.max(1, ...chartData.map(d => d.tiempoTotal));
  const maxPuntos = Math.max(1, ...chartData.map(d => d.puntos));

  return (
    <div className="chart-block">
      <h3 className="chart-title">Dispersión: tiempo vs puntos</h3>
      <div className="chart-scroll-container" role="img" aria-label="Dispersión de tiempo total y puntos por alumno">
        <svg width={width} height={height}>
          <line x1={margin.left} y1={margin.top + plotH} x2={width - margin.right} y2={margin.top + plotH} stroke="#94a3b8" strokeWidth={1.2} />
          <line x1={margin.left} y1={margin.top} x2={margin.left} y2={margin.top + plotH} stroke="#94a3b8" strokeWidth={1.2} />

          {chartData.map((d, idx) => {
            const x = margin.left + (d.tiempoTotal / maxTiempo) * plotW;
            const y = margin.top + (plotH - (d.puntos / maxPuntos) * plotH);
            return (
              <g key={`${d.nombre}-${idx}`}>
                <title>{`${d.nombre}: ${d.tiempoTotal} min, ${d.puntos} pts`}</title>
                <circle cx={x} cy={y} r={5} fill="#16a34a" opacity={0.85} />
              </g>
            );
          })}

          <text x={width / 2} y={height - 8} textAnchor="middle" fontSize="11" fill="#475569" fontFamily="'Oxygen', sans-serif">Tiempo total (min)</text>
          <text x={12} y={height / 2} textAnchor="middle" fontSize="11" fill="#475569" transform={`rotate(-90 12 ${height / 2})`} fontFamily="'Oxygen', sans-serif">Puntos</text>
        </svg>
      </div>
    </div>
  );
}

function formatearTiempo(minutosTotales: number): string {
  if (!minutosTotales || minutosTotales === 0) return '0 mins';
  const redondeado = Math.round(minutosTotales);
  if (redondeado === 0) return '< 1 min';
  if (redondeado === 1) return '1 min';
  return `${redondeado} mins`;
}

function formatearNumero2Dec(valor: number | null | undefined): string {
  if (typeof valor !== 'number' || !Number.isFinite(valor)) return '0.00';
  return valor.toFixed(2);
}

function limitarNotaSobreDiez(valor: number | null | undefined): number | null {
  if (typeof valor !== 'number' || !Number.isFinite(valor)) return null;
  return Math.max(0, Math.min(10, valor));
}

function formatearTiempoCurso(minutos: number | null | undefined): string {
  if (typeof minutos !== 'number' || !Number.isFinite(minutos) || minutos <= 0) return '0 mins';
  const redondeado = Math.round(minutos);
  return formatearTiempo(redondeado);
}

export default function EstadisticasCurso({ cursoId, embedded }: EstadisticasCursoProps = {}) {
  const params = useParams<{ id: string }>();
  const id = cursoId ?? params.id;
  const navigate = useNavigate();
  const [estadisticas, setEstadisticas] = useState<EstadisticaAlumno[]>([]);
  const [estadisticasCurso, setEstadisticasCurso] = useState<EstadisticasCursoDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [statsView, setStatsView] = useState<StatsView>({ mode: "resumen" });
  const [temasList, setTemasList] = useState<OpcionItem[]>([]);
  const [actividadesList, setActividadesList] = useState<OpcionItem[]>([]);
  const [alumnosList, setAlumnosList] = useState<{ id: number; nombre: string; notaActual: number | null }[]>([]);
  const [cargandoLista, setCargandoLista] = useState(false);
  const [alumnoSearch, setAlumnoSearch] = useState('');
  const [notaFiltro, setNotaFiltro] = useState('');
  const [operadorNotaFiltro, setOperadorNotaFiltro] = useState<'lt' | 'gt'>('lt');
  const alumnoSearchRef = useRef<HTMLInputElement>(null);
  const [tendenciasAvanzadas, setTendenciasAvanzadas] = useState<{ temas: string[]; rows: HeatmapRow[]; series: number[][] } | null>(null);
  const [loadingTendencias, setLoadingTendencias] = useState(false);
  const [errorTendencias, setErrorTendencias] = useState('');
  const [analisisTemaId, setAnalisisTemaId] = useState<number | undefined>(undefined);
  const [analisisActividadId, setAnalisisActividadId] = useState<number | undefined>(undefined);
  const [analisisTree, setAnalisisTree] = useState<TemaEstructura[]>([]);
  const [analisisTemaStats, setAnalisisTemaStats] = useState<Map<number, TemaStats>>(new Map());
  const [analisisActividadStats, setAnalisisActividadStats] = useState<Map<number, Map<number, ActividadStats>>>(new Map());
  const [analisisRepeticionesStats, setAnalisisRepeticionesStats] = useState<Map<number, Map<number, RepeticionesActividadDTO>>>(new Map());
  const [analisisExpandedTemas, setAnalisisExpandedTemas] = useState<Set<number>>(new Set());
  const [analisisLoading, setAnalisisLoading] = useState(false);
  const [analisisError, setAnalisisError] = useState('');
  const [analisisGrafTema, setAnalisisGrafTema] = useState<number | 'all'>('all');
  const [analisisAlumnoResumes, setAnalisisAlumnoResumes] = useState<any[]>([]);
  const [analisisKpiTema, setAnalisisKpiTema] = useState<number | 'all'>('all');
  const [analisisMetricOrden, setAnalisisMetricOrden] = useState<'suspensos' | 'desviacion' | 'abandono' | 'repeticiones'>('suspensos');
  const [analisisMetricSentido, setAnalisisMetricSentido] = useState<'desc' | 'asc'>('desc');
  const [analisisSemaforoFiltro, setAnalisisSemaforoFiltro] = useState<'all' | 'riesgo' | 'atencion' | 'bien'>('all');
  const [analisisSemaforoOrden, setAnalisisSemaforoOrden] = useState<'alumno' | 'notaActual' | 'estado'>('notaActual');
  const [analisisSemaforoSentido, setAnalisisSemaforoSentido] = useState<'asc' | 'desc'>('desc');
  const [analisisTablaOrden, setAnalisisTablaOrden] = useState<AnalisisTablaOrden>('notaMedia');
  const [analisisTablaSentido, setAnalisisTablaSentido] = useState<'asc' | 'desc'>('desc');
  const [resumenTablaOrden, setResumenTablaOrden] = useState<ResumenTablaOrden>('puntos');
  const [resumenTablaSentido, setResumenTablaSentido] = useState<'asc' | 'desc'>('desc');

  const actividadesAnalisis = useMemo(() => {
    if (analisisTemaId == null) return actividadesList;
    return actividadesList.filter(a => a.temaId === analisisTemaId);
  }, [actividadesList, analisisTemaId]);

  useEffect(() => {
    if (actividadesAnalisis.length === 0) {
      setAnalisisActividadId(undefined);
      return;
    }
    const existe = actividadesAnalisis.some(a => a.id === analisisActividadId);
    if (!existe) {
      setAnalisisActividadId(actividadesAnalisis[0].id);
    }
  }, [actividadesAnalisis, analisisActividadId]);

  const calcularNotaActividadSobre10 = (puntuacion: number | null | undefined, puntuacionMaxima: number | null | undefined, notaAlumno: number | null | undefined): number => {
    if (typeof puntuacion === 'number' && Number.isFinite(puntuacion)
      && typeof puntuacionMaxima === 'number' && Number.isFinite(puntuacionMaxima) && puntuacionMaxima > 0) {
      return (puntuacion / puntuacionMaxima) * 10;
    }
    if (typeof notaAlumno === 'number' && Number.isFinite(notaAlumno)) {
      return notaAlumno;
    }
    return 0;
  };

  const calcularNotaActualDesdeResumen = (resumen: any): number | null => {
    const temas = Array.isArray(resumen?.temas) ? resumen.temas : [];
    if (temas.length === 0) return null;

    const notasTema = temas.map((tema: any) => {
      const actividades = Array.isArray(tema?.actividades) ? tema.actividades : [];
      if (actividades.length === 0) return null;

      const suma = actividades.reduce((acc: number, act: any) => {
        const notaActividad = calcularNotaActividadSobre10(act?.puntuacionAlumno, act?.puntuacionMaxima, act?.notaAlumno);
        return acc + notaActividad;
      }, 0);

      return suma / actividades.length;
    }).filter((n: number | null): n is number => typeof n === 'number' && Number.isFinite(n));

    if (notasTema.length === 0) return null;
    return notasTema.reduce((acc: number, n: number) => acc + n, 0) / notasTema.length;
  };

  const calcularNotasTemaDesdeResumen = (resumen: any): Map<number, { titulo: string; nota: number | null }> => {
    const out = new Map<number, { titulo: string; nota: number | null }>();
    const temas = Array.isArray(resumen?.temas) ? resumen.temas : [];

    temas.forEach((tema: any) => {
      const temaId = typeof tema?.temaId === 'number' ? tema.temaId : undefined;
      if (temaId === undefined) return;
      const titulo = typeof tema?.titulo === 'string' ? tema.titulo : `Tema ${temaId}`;
      const actividades = Array.isArray(tema?.actividades) ? tema.actividades : [];
      if (actividades.length === 0) {
        out.set(temaId, { titulo, nota: null });
        return;
      }

      const suma = actividades.reduce((acc: number, act: any) => {
        const notaActividad = calcularNotaActividadSobre10(act?.puntuacionAlumno, act?.puntuacionMaxima, act?.notaAlumno);
        return acc + notaActividad;
      }, 0);

      out.set(temaId, { titulo, nota: suma / actividades.length });
    });

    return out;
  };

  const cargarEstadisticas = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

      // Hacemos 3 llamadas: Puntos, Actividades y la lista completa de tiempos (con límite 1000 para que vengan todos)
      const [puntosRes, actividadesRes, tiemposRes, cursoRes] = await Promise.all([
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/puntos`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/actividades-completadas`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos-rapidos-lentos?limite=1000`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/estadisiticas-curso`, { headers: { 'Authorization': `Bearer ${token}` } }),
      ]);

      if (!puntosRes.ok || !actividadesRes.ok) throw new Error('Error al cargar datos principales');

      const puntosData = await puntosRes.json();
      const actividadesData = await actividadesRes.json();
      const tiemposData = tiemposRes.ok ? await tiemposRes.json() : null;
      const cursoData: EstadisticasCursoDTO | null = cursoRes.ok ? await cursoRes.json() : null;

      const alumnosMap = new Map<string, EstadisticaAlumno>();

      // 1. Procesar puntos
      Object.entries(puntosData).forEach(([nombre, puntos]) => {
        alumnosMap.set(nombre, { nombre, puntos: puntos as number, actividadesRealizadas: 0, tiempoTotal: 0 });
      });

      // 2. Procesar actividades completadas
      Object.entries(actividadesData).forEach(([nombre, acts]) => {
        if (!alumnosMap.has(nombre)) {
          alumnosMap.set(nombre, { nombre, puntos: 0, actividadesRealizadas: 0, tiempoTotal: 0 });
        }
        alumnosMap.get(nombre)!.actividadesRealizadas = acts as number;
      });

      // 3. Procesar tiempos desde el endpoint de tu compañero
      if (tiemposData && tiemposData.masRapidos) {
        tiemposData.masRapidos.forEach((t: any) => {
          if (!alumnosMap.has(t.nombreAlumno)) {
            alumnosMap.set(t.nombreAlumno, { nombre: t.nombreAlumno, puntos: 0, actividadesRealizadas: 0, tiempoTotal: 0 });
          }
          // El backend de tu compañero nos da el tiempo exacto en minutos
          alumnosMap.get(t.nombreAlumno)!.tiempoTotal = t.tiempoMinutos || 0;
          alumnosMap.get(t.nombreAlumno)!.idAlumno = t.alumnoId;
        });
      }

      setEstadisticas(Array.from(alumnosMap.values()));
      console.log('[cargarEstadisticas] alumnosMap:', Array.from(alumnosMap.entries()));
      setEstadisticasCurso(cursoData);

    } catch (err) {
      setError((err as Error).message || 'Error cargando las estadísticas');
      setEstadisticasCurso(null);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void cargarEstadisticas();
  }, [cargarEstadisticas]);

  useEffect(() => {
    if (statsView.mode !== 'tendencias') return;
    if (estadisticas.length === 0) return;

    const cargarTendenciasAvanzadas = async () => {
      const alumnosConId = estadisticas.filter(e => typeof e.idAlumno === 'number') as Array<EstadisticaAlumno & { idAlumno: number }>;
      if (alumnosConId.length === 0) {
        setTendenciasAvanzadas(null);
        return;
      }

      setLoadingTendencias(true);
      setErrorTendencias('');
      try {
        const token = localStorage.getItem('token');
        const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

        const respuestas = await Promise.all(alumnosConId.map(async (a) => {
          const res = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos/${a.idAlumno}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          if (!res.ok) return { alumno: a.nombre, temas: new Map<number, { titulo: string; nota: number | null }>() };
          const resumen = await res.json();
          return { alumno: a.nombre, temas: calcularNotasTemaDesdeResumen(resumen) };
        }));

        const temasMap = new Map<number, string>();
        respuestas.forEach(r => {
          r.temas.forEach((val, temaId) => {
            if (!temasMap.has(temaId)) temasMap.set(temaId, val.titulo);
          });
        });

        const temasOrdenados = Array.from(temasMap.entries())
          .sort((a, b) => a[0] - b[0])
          .map(([idTema, titulo]) => ({ idTema, titulo }));

        const rows: HeatmapRow[] = respuestas.map(r => ({
          alumno: r.alumno,
          notas: temasOrdenados.map(t => r.temas.get(t.idTema)?.nota ?? null),
        }));

        const series = temasOrdenados.map((_, idx) =>
          rows
            .map(row => row.notas[idx])
            .filter((n): n is number => typeof n === 'number' && Number.isFinite(n))
        );

        setTendenciasAvanzadas({ temas: temasOrdenados.map(t => t.titulo), rows, series });
      } catch {
        setErrorTendencias('No se pudieron cargar las tendencias avanzadas.');
      } finally {
        setLoadingTendencias(false);
      }
    };

    void cargarTendenciasAvanzadas();
  }, [statsView.mode, estadisticas, id]);

  const metricasTiempo = useMemo(() => {
    if (estadisticas.length === 0) return null;
    const alumnosConTiempo = estadisticas.filter(est => est.tiempoTotal > 0);
    if (alumnosConTiempo.length === 0) return null;

    let totalMinutos = 0;
    let masRapido = alumnosConTiempo[0];
    let masLento = alumnosConTiempo[0];

    alumnosConTiempo.forEach(est => {
      totalMinutos += est.tiempoTotal;
      if (est.tiempoTotal < masRapido.tiempoTotal) masRapido = est;
      if (est.tiempoTotal > masLento.tiempoTotal) masLento = est;
    });

    return { media: totalMinutos / alumnosConTiempo.length, masRapido, masLento };
  }, [estadisticas]);

  const alertasResumen = useMemo(() => {
    if (estadisticas.length === 0) return null;

    const sinActividad = estadisticas.filter(e => e.actividadesRealizadas === 0).length;
    const mediaActividades = estadisticas.reduce((acc, e) => acc + e.actividadesRealizadas, 0) / Math.max(estadisticas.length, 1);
    const bajoRitmo = estadisticas.filter(e => e.actividadesRealizadas > 0 && e.actividadesRealizadas < mediaActividades).length;

    const ordenPuntos = [...estadisticas].sort((a, b) => b.puntos - a.puntos);
    const top = ordenPuntos[0] ?? null;

    const tiempoConDatos = estadisticas.filter(e => e.tiempoTotal > 0);
    const mediaTiempo = tiempoConDatos.length > 0
      ? tiempoConDatos.reduce((acc, e) => acc + e.tiempoTotal, 0) / tiempoConDatos.length
      : 0;
    const tiempoAlto = tiempoConDatos.filter(e => e.tiempoTotal > mediaTiempo * 1.5).length;

    return {
      sinActividad,
      bajoRitmo,
      top,
      tiempoAlto,
    };
  }, [estadisticas]);

  const resumenGeneral = useMemo(() => {
    const totalAlumnos = estadisticas.length;
    if (totalAlumnos === 0) {
      return {
        totalAlumnos: 0,
        activos: 0,
        participacionPct: 0,
        promedioPuntos: 0,
        promedioActividades: 0,
      };
    }

    const activos = estadisticas.filter(e => e.actividadesRealizadas > 0).length;
    const participacionPct = (activos / totalAlumnos) * 100;
    const promedioPuntos = estadisticas.reduce((acc, e) => acc + e.puntos, 0) / totalAlumnos;
    const promedioActividades = estadisticas.reduce((acc, e) => acc + e.actividadesRealizadas, 0) / totalAlumnos;

    return {
      totalAlumnos,
      activos,
      participacionPct,
      promedioPuntos,
      promedioActividades,
    };
  }, [estadisticas]);

  const resumenActividadNiveles = useMemo(() => {
    const totalActividadesCurso = analisisTree.reduce((acc, tema) => acc + tema.actividades.length, 0);
    const denominador = totalActividadesCurso > 0
      ? totalActividadesCurso
      : Math.max(1, ...estadisticas.map(e => e.actividadesRealizadas));

    const ratioCompletado = (e: EstadisticaAlumno): number => {
      const raw = (e.actividadesRealizadas / denominador) * 100;
      return Math.max(0, Math.min(100, raw));
    };

    const levels = [
      { label: 'Sin avance (0%)', match: (e: EstadisticaAlumno) => e.actividadesRealizadas === 0 },
      { label: 'Inicio (1-25%)', match: (e: EstadisticaAlumno) => {
        const pct = ratioCompletado(e);
        return pct > 0 && pct <= 25;
      } },
      { label: 'Progreso (26-60%)', match: (e: EstadisticaAlumno) => {
        const pct = ratioCompletado(e);
        return pct > 25 && pct <= 60;
      } },
      { label: 'Avance alto (>60%)', match: (e: EstadisticaAlumno) => ratioCompletado(e) > 60 },
    ];

    return levels.map((level) => {
      const alumnos = estadisticas.filter(level.match);
      const count = alumnos.length;
      const avgPuntos = count > 0
        ? alumnos.reduce((acc, a) => acc + a.puntos, 0) / count
        : 0;

      return {
        label: level.label,
        count,
        avgPuntos,
      };
    });
  }, [estadisticas, analisisTree]);

  const tendenciasData = useMemo(() => {
    const bins = [
      { label: '0', value: 0 },
      { label: '1-2', value: 0 },
      { label: '3-4', value: 0 },
      { label: '5+', value: 0 },
    ];

    estadisticas.forEach(e => {
      const a = e.actividadesRealizadas;
      if (a <= 0) bins[0].value += 1;
      else if (a <= 2) bins[1].value += 1;
      else if (a <= 4) bins[2].value += 1;
      else bins[3].value += 1;
    });

    return bins;
  }, [estadisticas]);

  const analisisInsights = useMemo(() => {
    if (analisisAlumnoResumes.length === 0) {
      return {
        suspensosTema: [] as Array<{ temaId: number; nombre: string; pct: number }>,
        suspensosActividad: [] as Array<{ actividadId: number; temaId: number; nombre: string; pct: number }>,
        desvioTema: [] as Array<{ temaId: number; nombre: string; sd: number }>,
        desvioActividad: [] as Array<{ actividadId: number; temaId: number; nombre: string; sd: number }>,
        evolucionSemanal: [] as EvolucionSemana[],
        repAbandono: [] as Array<{ actividadId: number; temaId: number; nombre: string; repMedia: number; abandonoPct: number }>,
        semaforo: [] as AlumnoSemaforo[],
        semaforoCount: { riesgo: 0, atencion: 0, bien: 0 },
      };
    }

    const totalAlumnos = analisisAlumnoResumes.length;
    const temasMeta = new Map<number, string>();
    const notasTemaPorAlumno = new Map<number, number[]>();
    const notasActividadPorAlumno = new Map<number, number[]>();
    const actividadNombre = new Map<number, string>();
    const temaNombrePorActividad = new Map<number, string>();
    const temaIdPorActividad = new Map<number, number>();
    const semanaNotas = new Map<string, number[]>();
    const actividadIntentos = new Map<number, { intentos: number; abandonos: number }>();
    const semaforo: AlumnoSemaforo[] = [];

    const notaAct = (act: any, intento?: any): number => {
      const pMax = act?.puntuacionMaxima;
      const p = intento ? intento?.puntuacion : act?.puntuacionAlumno;
      if (typeof p === 'number' && Number.isFinite(p) && typeof pMax === 'number' && Number.isFinite(pMax) && pMax > 0) {
        return (p / pMax) * 10;
      }
      const n = intento ? intento?.nota : act?.notaAlumno;
      if (typeof n === 'number' && Number.isFinite(n)) return n;
      return 0;
    };

    analisisAlumnoResumes.forEach((resumen: any) => {
      const temas = Array.isArray(resumen?.temas) ? resumen.temas : [];
      const notasTemaAlumno: number[] = [];

      temas.forEach((tema: any) => {
        const temaId = Number(tema?.temaId);
        if (!Number.isFinite(temaId)) return;
        const tNombre = String(tema?.titulo ?? `Tema ${temaId}`);
        temasMeta.set(temaId, tNombre);

        const actividades = Array.isArray(tema?.actividades) ? tema.actividades : [];
        if (actividades.length === 0) return;

        const notasActTema = actividades.map((act: any) => {
          const actId = Number(act?.actividadId);
          if (Number.isFinite(actId)) {
            actividadNombre.set(actId, String(act?.titulo ?? `Actividad ${actId}`));
            temaNombrePorActividad.set(actId, tNombre);
            temaIdPorActividad.set(actId, temaId);
          }
          const n = notaAct(act);

          if (Number.isFinite(actId)) {
            if (!notasActividadPorAlumno.has(actId)) notasActividadPorAlumno.set(actId, []);
            notasActividadPorAlumno.get(actId)!.push(n);

            const intentos = Array.isArray(act?.intentos) ? act.intentos : [];
            const current = actividadIntentos.get(actId) ?? { intentos: 0, abandonos: 0 };
            current.intentos += intentos.length;
            current.abandonos += intentos.reduce((acc: number, it: any) => acc + (typeof it?.numAbandonos === 'number' ? it.numAbandonos : 0), 0);
            actividadIntentos.set(actId, current);

            intentos.forEach((it: any) => {
              const key = weekKeyFromISO(it?.fechaFin || it?.fechaInicio);
              if (!key) return;
              const ni = notaAct(act, it);
              if (!semanaNotas.has(key)) semanaNotas.set(key, []);
              semanaNotas.get(key)!.push(ni);
            });
          }

          return n;
        });

        const notaTemaAlumno = notasActTema.reduce((a: number, b: number) => a + b, 0) / actividades.length;
        notasTemaAlumno.push(notaTemaAlumno);
        if (!notasTemaPorAlumno.has(temaId)) notasTemaPorAlumno.set(temaId, []);
        notasTemaPorAlumno.get(temaId)!.push(notaTemaAlumno);
      });

      const notaActual = notasTemaAlumno.length > 0 ? notasTemaAlumno.reduce((a, b) => a + b, 0) / notasTemaAlumno.length : 0;
      const estado: AlumnoSemaforo['estado'] = notaActual < 5 ? 'riesgo' : (notaActual < 7 ? 'atencion' : 'bien');
      semaforo.push({ alumno: String(resumen?.nombreAlumno ?? 'Alumno'), notaActual, estado });
    });

    const suspensosTema = Array.from(temasMeta.entries()).map(([temaId, nombre]) => {
      const notas = notasTemaPorAlumno.get(temaId) ?? [];
      const susp = notas.filter(n => n < 5).length;
      return { temaId, nombre, pct: totalAlumnos > 0 ? (susp / totalAlumnos) * 100 : 0 };
    }).sort((a, b) => b.pct - a.pct);

    const desvioTema = Array.from(temasMeta.entries()).map(([temaId, nombre]) => {
      const notas = notasTemaPorAlumno.get(temaId) ?? [];
      return { temaId, nombre, sd: stdDev(notas) };
    }).sort((a, b) => b.sd - a.sd);

    const suspensosActividad = Array.from(notasActividadPorAlumno.entries()).map(([actId, notas]) => {
      const susp = notas.filter(n => n < 5).length;
      return {
        actividadId: actId,
        temaId: temaIdPorActividad.get(actId) ?? -1,
        nombre: `${temaNombrePorActividad.get(actId) ?? ''} / ${actividadNombre.get(actId) ?? `Actividad ${actId}`}`,
        pct: totalAlumnos > 0 ? (susp / totalAlumnos) * 100 : 0,
      };
    }).sort((a, b) => b.pct - a.pct);

    const desvioActividad = Array.from(notasActividadPorAlumno.entries()).map(([actId, notas]) => ({
      actividadId: actId,
      temaId: temaIdPorActividad.get(actId) ?? -1,
      nombre: `${temaNombrePorActividad.get(actId) ?? ''} / ${actividadNombre.get(actId) ?? `Actividad ${actId}`}`,
      sd: stdDev(notas),
    })).sort((a, b) => b.sd - a.sd);

    const repAbandono = Array.from(actividadIntentos.entries()).map(([actId, agg]) => ({
      actividadId: actId,
      temaId: temaIdPorActividad.get(actId) ?? -1,
      nombre: `${temaNombrePorActividad.get(actId) ?? ''} / ${actividadNombre.get(actId) ?? `Actividad ${actId}`}`,
      repMedia: totalAlumnos > 0 ? agg.intentos / totalAlumnos : 0,
      abandonoPct: agg.intentos > 0 ? (agg.abandonos / agg.intentos) * 100 : 0,
    })).sort((a, b) => (b.abandonoPct + b.repMedia) - (a.abandonoPct + a.repMedia));

    const evolucionSemanal = Array.from(semanaNotas.entries())
      .map(([label, notas]) => ({ label, media: notas.reduce((a, b) => a + b, 0) / Math.max(notas.length, 1) }))
      .sort((a, b) => a.label.localeCompare(b.label));

    const semaforoCount = semaforo.reduce((acc, s) => {
      acc[s.estado] += 1;
      return acc;
    }, { riesgo: 0, atencion: 0, bien: 0 });

    return {
      suspensosTema,
      suspensosActividad,
      desvioTema,
      desvioActividad,
      evolucionSemanal,
      repAbandono,
      semaforo: semaforo.sort((a, b) => a.notaActual - b.notaActual),
      semaforoCount,
    };
  }, [analisisAlumnoResumes]);

  const analisisTreeOrdenado = useMemo(() => {
    const getTemaSortValue = (tema: TemaEstructura): string | number => {
      const temaStats = analisisTemaStats.get(tema.id);
      switch (analisisTablaOrden) {
        case 'elemento':
          return tema.titulo.toLocaleLowerCase('es');
        case 'tipo':
          return 'tema';
        case 'notaMedia':
          return temaStats?.notaMediaTema ?? Number.NEGATIVE_INFINITY;
        case 'notaMaxima':
          return temaStats?.notaMaximaTema ?? Number.NEGATIVE_INFINITY;
        case 'notaMinima':
          return temaStats?.notaMinimaTema ?? Number.NEGATIVE_INFINITY;
        case 'tiempoMedio':
          return temaStats?.tiempoMedioTema ?? Number.NEGATIVE_INFINITY;
        case 'completado':
          return temaStats?.temaCompletadoPorTodos == null ? 2 : (temaStats.temaCompletadoPorTodos ? 1 : 0);
        default:
          return tema.titulo.toLocaleLowerCase('es');
      }
    };

    const getActividadSortValue = (act: OpcionItem, st?: ActividadStats): string | number => {
      switch (analisisTablaOrden) {
        case 'elemento':
          return String(act.nombre ?? '').toLocaleLowerCase('es');
        case 'tipo':
          return 'actividad';
        case 'notaMedia':
          return st?.notaMediaActividad ?? Number.NEGATIVE_INFINITY;
        case 'notaMaxima':
          return st?.notaMaximaActividad ?? Number.NEGATIVE_INFINITY;
        case 'notaMinima':
          return st?.notaMinimaActividad ?? Number.NEGATIVE_INFINITY;
        case 'tiempoMedio':
          return st?.tiempoMedioActividad ?? Number.NEGATIVE_INFINITY;
        case 'completado':
          return st?.actividadCompletadaPorTodos == null ? 2 : (st.actividadCompletadaPorTodos ? 1 : 0);
        default:
          return String(act.nombre ?? '').toLocaleLowerCase('es');
      }
    };

    const comparar = (a: string | number, b: string | number) => {
      const resultado = typeof a === 'string' && typeof b === 'string'
        ? a.localeCompare(b, 'es', { sensitivity: 'base' })
        : (a as number) - (b as number);
      return analisisTablaSentido === 'asc' ? resultado : -resultado;
    };

    return [...analisisTree]
      .sort((a, b) => comparar(getTemaSortValue(a), getTemaSortValue(b)))
      .map((tema) => {
        const statsPorActividad = analisisActividadStats.get(tema.id) ?? new Map<number, ActividadStats>();
        const actividadesOrdenadas = [...tema.actividades].sort((a, b) => comparar(
          getActividadSortValue(a, statsPorActividad.get(a.id)),
          getActividadSortValue(b, statsPorActividad.get(b.id))
        ));
        return { ...tema, actividades: actividadesOrdenadas };
      });
  }, [analisisTree, analisisActividadStats, analisisTemaStats, analisisTablaOrden, analisisTablaSentido]);

  const handleAnalisisTablaSort = (orden: AnalisisTablaOrden) => {
    if (analisisTablaOrden === orden) {
      setAnalisisTablaSentido(prev => (prev === 'asc' ? 'desc' : 'asc'));
      return;
    }
    setAnalisisTablaOrden(orden);
    setAnalisisTablaSentido(orden === 'elemento' ? 'asc' : 'desc');
  };

  const cargarListaTemas = useCallback(async (): Promise<OpcionItem[]> => {
    setCargandoLista(true);
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      const res = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!res.ok) throw new Error('No se pudo obtener la lista de temas');
      const temasData = await res.json();
      const nuevos: OpcionItem[] = temasData.map((t: any) => {
        const temaObj = t.tema || t;
        return { id: temaObj.id, nombre: temaObj.titulo || temaObj.nombre || `Tema ${temaObj.id}` };
      });
      setTemasList(nuevos);
      return nuevos;
    } catch (err) {
      console.error(err);
      return [];
    } finally {
      setCargandoLista(false);
    }
  }, [id]);

  const cargarListaActividades = useCallback(async (): Promise<OpcionItem[]> => {
    setCargandoLista(true);
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");
      const res = await fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!res.ok) throw new Error('No se pudo obtener la lista');
      const temasData = await res.json();
      const acts: OpcionItem[] = [];
      temasData.forEach((t: any) => {
        const temaObj = t.tema || t;
        const listaActs = t.actividades || t.actividadesDTO || [];
        listaActs.forEach((a: any) => {
          const actObj = a.actividad || a;
          const tipoAct = (actObj.tipo || "").toUpperCase();
          const tituloAct = (actObj.titulo || actObj.nombre || "").toLowerCase();
          const esTeoria = tipoAct === 'TEORIA' || tituloAct.includes('teoría') || tituloAct.includes('teoria');
          if (!esTeoria) {
            acts.push({ id: actObj.id, nombre: actObj.titulo || actObj.nombre || `Actividad ${actObj.id}`, temaId: temaObj.id });
          }
        });
      });
      setActividadesList(acts);
      return acts;
    } catch (err) {
      console.error(err);
      return [];
    } finally {
      setCargandoLista(false);
    }
  }, [id]);

  const handleMedias = async () => {
    const temas = await cargarListaTemas();
    setStatsView({ mode: "medias", temaId: temas[0]?.id });
  };

  const handleDesgloseActividades = async () => {
    const temas = await cargarListaTemas();
    setStatsView({ mode: "desgloseActividades", temaId: temas[0]?.id });
  };

  const handleGraficasActividades = async () => {
    const temas = await cargarListaTemas();
    setStatsView({ mode: "graficasActividades", temaId: temas[0]?.id });
  };

  const handleTiemposActividad = async () => {
    const acts = await cargarListaActividades();
    setStatsView({ mode: "tiemposActividad", actividadId: acts[0]?.id });
  };

  const cargarAnalisisData = useCallback(async () => {
    setAnalisisLoading(true);
    setAnalisisError('');
    try {
      const token = localStorage.getItem('token');
      const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

      const [resTemas, resTemasStats] = await Promise.all([
        fetch(`${apiBase}/api/temas/curso/${id}/maestro`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
        fetch(`${apiBase}/api/estadisticas/cursos/${id}/estadisticas-temas`, {
          headers: { Authorization: `Bearer ${token}` },
        }),
      ]);

      if (!resTemas.ok) throw new Error('No se pudo cargar la estructura de temas.');
      if (!resTemasStats.ok) throw new Error('No se pudieron cargar estadísticas de temas.');

      const temasRaw = await resTemas.json();
      const temas: TemaEstructura[] = (temasRaw as any[]).map((t) => ({
        id: t.id,
        titulo: t.titulo,
        actividades: Array.isArray(t.actividades)
          ? t.actividades.map((a: any) => ({ id: a.id, titulo: a.titulo }))
          : [],
      }));

      const temasStatsRaw: Record<string, TemaStats> = await resTemasStats.json();
      const temaStatsMap = new Map<number, TemaStats>();
      Object.entries(temasStatsRaw).forEach(([k, v]) => temaStatsMap.set(Number(k), v));

      const actividadStatsEntries = await Promise.all(
        temas.map(async (tema) => {
          const res = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/temas/${tema.id}/estadisticas-actividades`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          if (!res.ok) return [tema.id, new Map<number, ActividadStats>()] as const;
          const raw: Record<string, ActividadStats> = await res.json();
          const map = new Map<number, ActividadStats>();
          Object.entries(raw).forEach(([k, v]) => map.set(Number(k), v));
          return [tema.id, map] as const;
        })
      );

      const repeticionesEntries = await Promise.all(
        temas.map(async (tema) => {
          const res = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/temas/${tema.id}/repeticiones-actividades`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          if (!res.ok) return [tema.id, new Map<number, RepeticionesActividadDTO>()] as const;
          const raw: Record<string, RepeticionesActividadDTO> = await res.json();
          const map = new Map<number, RepeticionesActividadDTO>();
          Object.entries(raw).forEach(([k, v]) => map.set(Number(k), v));
          return [tema.id, map] as const;
        })
      );

      const actividadStatsMap = new Map<number, Map<number, ActividadStats>>(actividadStatsEntries);
      const repeticionesMap = new Map<number, Map<number, RepeticionesActividadDTO>>(repeticionesEntries);

      const resAlumnos = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos-rapidos-lentos?limite=1000`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      let alumnosResumes: any[] = [];
      if (resAlumnos.ok) {
        const alumnosData = await resAlumnos.json();
        const seen = new Set<number>();
        const ids: number[] = [];
        [...(alumnosData.masRapidos || []), ...(alumnosData.masLentos || [])].forEach((a: any) => {
          if (typeof a.alumnoId === 'number' && !seen.has(a.alumnoId)) {
            seen.add(a.alumnoId);
            ids.push(a.alumnoId);
          }
        });

        const alumnoReqs = await Promise.all(ids.map(async (alumnoId) => {
          try {
            const rr = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos/${alumnoId}`, {
              headers: { Authorization: `Bearer ${token}` },
            });
            if (!rr.ok) return null;
            return await rr.json();
          } catch {
            return null;
          }
        }));
        alumnosResumes = alumnoReqs.filter(Boolean);
      }

      setAnalisisTree(temas);
      setAnalisisTemaStats(temaStatsMap);
      setAnalisisActividadStats(actividadStatsMap);
      setAnalisisRepeticionesStats(repeticionesMap);
      setAnalisisExpandedTemas(new Set(temas.map(t => t.id)));
      setAnalisisGrafTema('all');
      setAnalisisKpiTema('all');
      setAnalisisAlumnoResumes(alumnosResumes);
    } catch (e) {
      setAnalisisError(e instanceof Error ? e.message : 'Error cargando datos de análisis.');
      setAnalisisTree([]);
      setAnalisisTemaStats(new Map());
      setAnalisisActividadStats(new Map());
      setAnalisisRepeticionesStats(new Map());
      setAnalisisAlumnoResumes([]);
    } finally {
      setAnalisisLoading(false);
    }
  }, [id]);

  const handleAnalisis = useCallback(async () => {
    const [temas, acts] = await Promise.all([cargarListaTemas(), cargarListaActividades()]);
    setAnalisisTemaId(prev => (prev === undefined ? undefined : (temas.some(t => t.id === prev) ? prev : undefined)));
    setAnalisisActividadId(prev => prev ?? acts[0]?.id);
    await cargarAnalisisData();
    setStatsView({ mode: 'analisis' });
  }, [cargarListaTemas, cargarListaActividades, cargarAnalisisData]);

  useEffect(() => {
    void cargarAnalisisData();
  }, [cargarAnalisisData]);

  const cargarAlumnosConNota = useCallback(async (): Promise<{ id: number; nombre: string; notaActual: number | null }[]> => {
    const token = localStorage.getItem('token');
    const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
    const res = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos-rapidos-lentos?limite=1000`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!res.ok) throw new Error('No se pudo cargar la lista de alumnos.');

    const data = await res.json();
    console.log('[cargarAlumnosConNota] respuesta alumnos-rapidos-lentos:', JSON.stringify(data));
    const seen = new Set<number>();
    const alumnosBase: { id: number; nombre: string }[] = [];
    [...(data.masRapidos || []), ...(data.masLentos || [])].forEach((a: { alumnoId: number; nombreAlumno: string }) => {
      if (!seen.has(a.alumnoId)) {
        seen.add(a.alumnoId);
        alumnosBase.push({ id: a.alumnoId, nombre: a.nombreAlumno });
      }
    });

    return await Promise.all(alumnosBase.map(async (a) => {
      try {
        const resumenRes = await fetch(`${apiBase}/api/estadisticas/cursos/${id}/alumnos/${a.id}`, {
          headers: { 'Authorization': `Bearer ${token}` },
        });
        if (!resumenRes.ok) {
          return { ...a, notaActual: null };
        }
        const resumenData = await resumenRes.json();
        return { ...a, notaActual: calcularNotaActualDesdeResumen(resumenData) };
      } catch {
        return { ...a, notaActual: null };
      }
    }));
  }, [id]);

  const handleAlumnos = useCallback(async () => {
    setCargandoLista(true);
    try {
      const alumnos = await cargarAlumnosConNota();
      setAlumnosList(alumnos);
      console.log('[handleAlumnos] alumnos construidos:', alumnos.length, alumnos);
      if (alumnos.length > 0) {
        setStatsView({ mode: "alumnoDetalle", alumnoId: alumnos[0].id, alumnoNombre: alumnos[0].nombre });
      } else {
        setStatsView({ mode: "alumnos" });
      }
      setAlumnoSearch('');
      setNotaFiltro('');
      setOperadorNotaFiltro('lt');
      setTimeout(() => alumnoSearchRef.current?.focus(), 100);
    } catch (err) {
      console.error(err);
    } finally {
      setCargandoLista(false);
    }
  }, [cargarAlumnosConNota]);

  // Recalcular máximos y mínimos globales del curso basándose en las actividades reales
  const valoresGlobalesCurso = useMemo(() => {
    // Solo calcular si tenemos datos cargados
    if (analisisTree.length === 0 || analisisActividadStats.size === 0) {
      return {
        notaMaxima: null,
        notaMinima: null,
      };
    }

    // Recorrer todos los temas y sus actividades para encontrar máximo y mínimo global
    const notasMaximas: number[] = [];
    const notasMinimas: number[] = [];

    analisisTree.forEach(tema => {
      const actividadStatsMap = analisisActividadStats.get(tema.id);
      if (actividadStatsMap) {
        actividadStatsMap.forEach(actStats => {
          if (typeof actStats.notaMaximaActividad === 'number' && Number.isFinite(actStats.notaMaximaActividad)) {
            notasMaximas.push(Math.max(0, Math.min(10, actStats.notaMaximaActividad)));
          }
          if (typeof actStats.notaMinimaActividad === 'number' && Number.isFinite(actStats.notaMinimaActividad)) {
            notasMinimas.push(Math.max(0, Math.min(10, actStats.notaMinimaActividad)));
          }
        });
      }
    });

    return {
      notaMaxima: notasMaximas.length > 0 ? Math.max(...notasMaximas) : null,
      notaMinima: notasMinimas.length > 0 ? Math.min(...notasMinimas) : null,
    };
  }, [analisisTree, analisisActividadStats]);

  const cursoIndicadoresContent = !loading && !error ? (
    <div className="curso-indicadores" style={{ marginBottom: '16px' }}>
      <div className="curso-indicador">
        <div className="curso-indicador-label">Nota media</div>
        <div className="curso-indicador-value">{Math.round(limitarNotaSobreDiez(estadisticasCurso?.notaMediaCurso) ?? 0)}</div>
      </div>
      <div className="curso-indicador">
        <div className="curso-indicador-label">Nota máx.</div>
        <div className="curso-indicador-value">{valoresGlobalesCurso.notaMaxima ?? 0}</div>
      </div>
      <div className="curso-indicador">
        <div className="curso-indicador-label">Nota mín.</div>
        <div className="curso-indicador-value">{valoresGlobalesCurso.notaMinima ?? 0}</div>
      </div>
    </div>
  ) : null;

  const notasActualesMap = useMemo(() => {
    const map = new Map<string, number>();
    analisisInsights.semaforo.forEach((s) => {
      map.set(s.alumno.trim().toLowerCase(), s.notaActual);
    });
    return map;
  }, [analisisInsights.semaforo]);

  const filasResumenOrdenadas = useMemo(() => {
    const filas = estadisticas.map((stat) => ({
      ...stat,
      notaActual: notasActualesMap.get(stat.nombre.trim().toLowerCase()) ?? null,
    }));

    const getValue = (row: typeof filas[number], column: ResumenTablaOrden): string | number | null => {
      switch (column) {
        case 'alumno':
          return row.nombre.toLowerCase();
        case 'puntos':
          return row.puntos;
        case 'actividades':
          return row.actividadesRealizadas;
        case 'tiempo':
          return row.tiempoTotal;
        case 'notaActual':
          return row.notaActual;
        default:
          return null;
      }
    };

    return [...filas].sort((a, b) => {
      const va = getValue(a, resumenTablaOrden);
      const vb = getValue(b, resumenTablaOrden);

      if (va === null && vb === null) return 0;
      if (va === null) return 1;
      if (vb === null) return -1;

      let resultado = 0;
      if (typeof va === 'string' && typeof vb === 'string') {
        resultado = va.localeCompare(vb, 'es', { sensitivity: 'base' });
      } else {
        resultado = (va as number) - (vb as number);
      }

      return resumenTablaSentido === 'asc' ? resultado : -resultado;
    });
  }, [estadisticas, notasActualesMap, resumenTablaOrden, resumenTablaSentido]);

  const handleResumenTablaSort = (orden: ResumenTablaOrden) => {
    if (resumenTablaOrden === orden) {
      setResumenTablaSentido(prev => (prev === 'asc' ? 'desc' : 'asc'));
      return;
    }
    setResumenTablaOrden(orden);
    setResumenTablaSentido(orden === 'alumno' ? 'asc' : 'desc');
  };

  let estadisticasContent: React.ReactNode;
  if (loading) {
    estadisticasContent = <div className="stats-info-msg">Cargando...</div>;
  } else if (error) {
    estadisticasContent = <div className="stats-error-msg">{error}</div>;
  } else {
    estadisticasContent = (
      <>
        {metricasTiempo && (
          <div className="stat-cards-row">
            <div className="stat-card stat-card--tiempo">
              <span className="stat-card__label">Tiempo medio</span>
              <span className="stat-card__value">{formatearTiempo(metricasTiempo.media)}</span>
            </div>
            <div className="stat-card stat-card--rapido">
              <span className="stat-card__label">Alumno más rápido</span>
              <span className="stat-card__value">{formatearTiempo(metricasTiempo.masRapido.tiempoTotal)}</span>
              <span className="stat-card__name">{metricasTiempo.masRapido.nombre}</span>
            </div>
            <div className="stat-card stat-card--lento">
              <span className="stat-card__label">Alumno más lento</span>
              <span className="stat-card__value">{formatearTiempo(metricasTiempo.masLento.tiempoTotal)}</span>
              <span className="stat-card__name">{metricasTiempo.masLento.nombre}</span>
            </div>
          </div>
        )}

        <section className="resumen-overview-panel">
          <div className="resumen-overview-header">
            <h3 className="resumen-overview-title">Visión general del curso</h3>
          </div>
          <div className="resumen-overview-grid">
            <div className="resumen-overview-item">
              <span className="resumen-overview-item__label">Alumnos del curso</span>
              <span className="resumen-overview-item__value">{resumenGeneral.totalAlumnos}</span>
            </div>
            <div className="resumen-overview-item">
              <span className="resumen-overview-item__label">Participación activa</span>
              <span className="resumen-overview-item__value">{resumenGeneral.participacionPct.toFixed(1)}%</span>
              <span className="resumen-overview-item__meta">{resumenGeneral.activos} con actividad registrada</span>
            </div>
            <div className="resumen-overview-item">
              <span className="resumen-overview-item__label">Promedio de actividades</span>
              <span className="resumen-overview-item__value">{resumenGeneral.promedioActividades.toFixed(2)}</span>
            </div>
            <div className="resumen-overview-item">
              <span className="resumen-overview-item__label">Promedio de puntos</span>
              <span className="resumen-overview-item__value">{resumenGeneral.promedioPuntos.toFixed(1)}</span>
            </div>
          </div>
        </section>

        {alertasResumen && (
          <div className="stats-alerts-row">
            <div className="stats-alert-card stats-alert-card--warn">
              <span className="stats-alert-card__label">Alumnos sin actividad</span>
              <span className="stats-alert-card__value">{alertasResumen.sinActividad}</span>
              <span className="stats-alert-card__meta">Todavía no registran actividades completadas.</span>
            </div>
            <div className="stats-alert-card stats-alert-card--info">
              <span className="stats-alert-card__label">Ritmo por debajo de la media</span>
              <span className="stats-alert-card__value">{alertasResumen.bajoRitmo}</span>
              <span className="stats-alert-card__meta">Participan, pero avanzan por debajo del promedio del grupo.</span>
            </div>
            <div className="stats-alert-card stats-alert-card--good">
              <span className="stats-alert-card__label">Puntuación más alta</span>
              <span className="stats-alert-card__value">{alertasResumen.top ? `${alertasResumen.top.nombre} (${alertasResumen.top.puntos} pts)` : '—'}</span>
              <span className="stats-alert-card__meta">Alumno con mayor puntuación acumulada en el curso.</span>
            </div>
            <div className="stats-alert-card stats-alert-card--warn">
              <span className="stats-alert-card__label">Tiempo significativamente alto</span>
              <span className="stats-alert-card__value">{alertasResumen.tiempoAlto}</span>
              <span className="stats-alert-card__meta">Alumnos con dedicación muy superior a la media de tiempo.</span>
            </div>
          </div>
        )}

        <p className="analisis-panel-help">El nivel de participación se calcula como porcentaje de actividades del curso completadas por cada alumno.</p>
        <div className="charts-grid">
          <HistogramChart
            title="Distribución del grupo por nivel de participación"
            data={resumenActividadNiveles.map((x) => ({ label: x.label, value: x.count }))}
          />
        </div>

        <div className="table-scroll-container">
          <table className="pixel-table">
            <thead>
              <tr>
                <th>
                  <button type="button" className={`analisis-table-sort-btn${resumenTablaOrden === 'alumno' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleResumenTablaSort('alumno')}>
                    Alumno{resumenTablaOrden === 'alumno' ? (resumenTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                  </button>
                </th>
                <th className="text-center">
                  <button type="button" className={`analisis-table-sort-btn${resumenTablaOrden === 'puntos' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleResumenTablaSort('puntos')}>
                    Puntos{resumenTablaOrden === 'puntos' ? (resumenTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                  </button>
                </th>
                <th className="text-center">
                  <button type="button" className={`analisis-table-sort-btn${resumenTablaOrden === 'actividades' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleResumenTablaSort('actividades')}>
                    Nº actividades{resumenTablaOrden === 'actividades' ? (resumenTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                  </button>
                </th>
                <th className="text-center">
                  <button type="button" className={`analisis-table-sort-btn${resumenTablaOrden === 'tiempo' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleResumenTablaSort('tiempo')}>
                    Tiempo total{resumenTablaOrden === 'tiempo' ? (resumenTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                  </button>
                </th>
                <th className="text-center">
                  <button type="button" className={`analisis-table-sort-btn${resumenTablaOrden === 'notaActual' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleResumenTablaSort('notaActual')}>
                    Nota actual{resumenTablaOrden === 'notaActual' ? (resumenTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                  </button>
                </th>
              </tr>
            </thead>
            <tbody>
              {filasResumenOrdenadas.map((stat, index) => (
                <tr key={`${stat.idAlumno ?? 'sin-id'}-${stat.nombre}-${index}`}>
                  <td>{stat.nombre}</td>
                  <td className="text-center">{stat.puntos}</td>
                  <td className="text-center">{stat.actividadesRealizadas}</td>
                  <td className="text-center">{formatearTiempo(stat.tiempoTotal)}</td>
                  <td className="text-center">{stat.notaActual === null ? '—' : stat.notaActual.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </>
    );
  }

  const renderSidebar = () => {
    const isAnalisisMode =
      statsView.mode === 'analisis' ||
      statsView.mode === 'analisisTablas' ||
      statsView.mode === 'analisisGraficas' ||
      statsView.mode === 'medias' ||
      statsView.mode === 'desgloseTemas' ||
      statsView.mode === 'graficasTemas' ||
      statsView.mode === 'desgloseActividades' ||
      statsView.mode === 'graficasActividades' ||
      statsView.mode === 'tiemposActividad';

    return (
      <>
        <button
          className={`stats-sidebar-btn${statsView.mode === 'resumen' ? ' stats-sidebar-btn--active' : ''}`}
          onClick={() => setStatsView({ mode: "resumen" })}
        >
          Resumen general
        </button>
        <button
          className={`stats-sidebar-btn${statsView.mode === 'alumnos' || statsView.mode === 'alumnoDetalle' ? ' stats-sidebar-btn--active' : ''}`}
          onClick={handleAlumnos}
        >
          Alumnos
        </button>
        <button
          className={`stats-sidebar-btn${isAnalisisMode ? ' stats-sidebar-btn--active' : ''}`}
          onClick={() => void handleAnalisis()}
        >
          Análisis
        </button>
        <button
          className={`stats-sidebar-btn${statsView.mode === 'tendencias' ? ' stats-sidebar-btn--active' : ''}`}
          onClick={() => setStatsView({ mode: "tendencias" })}
        >
          Tendencias
        </button>
      </>
    );
  };

  const renderAlumnosContent = () => {
    const normalizedSearch = alumnoSearch.trim().toLowerCase();
    const notaUmbral = notaFiltro.trim() === '' ? null : Number(notaFiltro);
    const notaFiltroValida = notaUmbral !== null && Number.isFinite(notaUmbral);
    const filteredAlumnos = alumnosList
      .filter(a => (normalizedSearch ? a.nombre.toLowerCase().includes(normalizedSearch) : true))
      .filter(a => {
        if (!notaFiltroValida) return true;
        if (typeof a.notaActual !== 'number' || !Number.isFinite(a.notaActual)) return false;
        return operadorNotaFiltro === 'gt' ? a.notaActual > (notaUmbral as number) : a.notaActual < (notaUmbral as number);
      });

    const alumnoSeleccionado = statsView.mode === 'alumnoDetalle'
      ? filteredAlumnos.find(a => a.id === statsView.alumnoId) ?? alumnosList.find(a => a.id === statsView.alumnoId) ?? null
      : null;

    return (
      <div className="alumnos-layout">
        <section className="analisis-panel analisis-panel--full">
          <h3 className="analisis-panel-title">Listado de alumnos</h3>
          <p className="analisis-panel-help">Busca y filtra alumnos desde el contenido principal. La barra lateral queda reservada para la navegación.</p>
          <div className="alumnos-toolbar">
            <input
              ref={alumnoSearchRef}
              type="text"
              className="stats-sidebar-search alumnos-search"
              placeholder="Buscar alumno..."
              value={alumnoSearch}
              onChange={e => setAlumnoSearch(e.target.value)}
            />

            <div className="stats-sidebar-filter-row alumnos-filter-row">
              <select
                className="stats-sidebar-filter-operator"
                value={operadorNotaFiltro}
                onChange={(e) => setOperadorNotaFiltro(e.target.value as 'lt' | 'gt')}
                aria-label="Operador de filtro de nota"
              >
                <option value="lt">&lt;</option>
                <option value="gt">&gt;</option>
              </select>
              <input
                type="number"
                min="0"
                max="10"
                step="0.01"
                className="stats-sidebar-filter-note"
                placeholder="Nota"
                value={notaFiltro}
                onChange={(e) => setNotaFiltro(e.target.value)}
                aria-label="Nota para filtrar alumnos"
              />
            </div>
          </div>

          {cargandoLista ? (
            <p className="stats-info-msg">Cargando alumnos...</p>
          ) : filteredAlumnos.length === 0 ? (
            <p className="stats-info-msg">{alumnoSearch.trim() || notaFiltro.trim() ? 'Sin resultados' : 'Sin alumnos inscritos'}</p>
          ) : (
            <div className="alumnos-list-grid">
              {filteredAlumnos.map(a => (
                <button
                  key={a.id}
                  className={`stats-sidebar-btn alumnos-list-item${typeof a.notaActual === 'number' && a.notaActual < 5 ? ' stats-sidebar-btn--failed' : ''}${statsView.mode === 'alumnoDetalle' && statsView.alumnoId === a.id ? ' stats-sidebar-btn--active' : ''}`}
                  onClick={() => setStatsView({ mode: 'alumnoDetalle', alumnoId: a.id, alumnoNombre: a.nombre })}
                >
                  <div>{a.nombre}</div>
                  <div className={`stats-sidebar-alumno-nota${typeof a.notaActual === 'number' && a.notaActual < 5 ? ' stats-sidebar-alumno-nota--failed' : ''}`}>
                    Nota actual: {typeof a.notaActual === 'number' && Number.isFinite(a.notaActual) ? a.notaActual.toFixed(2) : '—'}
                  </div>
                </button>
              ))}
            </div>
          )}
        </section>

        <section className="analisis-panel analisis-panel--full">
          <h3 className="analisis-panel-title">Detalle del alumno</h3>
          {statsView.mode === 'alumnoDetalle' && alumnoSeleccionado ? (
            <EstadisticasAlumno
              cursoIdProp={id}
              alumnoId={alumnoSeleccionado.id}
              embedded
            />
          ) : (
            <div className="stats-placeholder">Selecciona un alumno para ver su detalle.</div>
          )}
        </section>
      </div>
    );
  };

  const isAnalisisMode =
    statsView.mode === 'analisis' ||
    statsView.mode === 'analisisTablas' ||
    statsView.mode === 'analisisGraficas' ||
    statsView.mode === 'medias' ||
    statsView.mode === 'desgloseTemas' ||
    statsView.mode === 'graficasTemas' ||
    statsView.mode === 'desgloseActividades' ||
    statsView.mode === 'graficasActividades' ||
    statsView.mode === 'tiemposActividad';

  const isAlumnosMode = statsView.mode === 'alumnos' || statsView.mode === 'alumnoDetalle';

  const renderStatsContent = () => {
    switch (statsView.mode) {
      case "analisis": {
        const mostrarTemas = analisisKpiTema === 'all';

        const filtradoSuspensosTema = mostrarTemas ? analisisInsights.suspensosTema : [];
        const filtradoDesvioTema = mostrarTemas ? analisisInsights.desvioTema : [];
        const filtradoSuspensosActividad = !mostrarTemas ? analisisInsights.suspensosActividad.filter(x => x.temaId === analisisKpiTema) : [];
        const filtradoDesvioActividad = !mostrarTemas ? analisisInsights.desvioActividad.filter(x => x.temaId === analisisKpiTema) : [];
        const filtradoRepAbandono = !mostrarTemas ? analisisInsights.repAbandono.filter(x => x.temaId === analisisKpiTema) : [];
        const ordenMetricas = mostrarTemas && (analisisMetricOrden === 'abandono' || analisisMetricOrden === 'repeticiones')
          ? 'suspensos'
          : analisisMetricOrden;

        const topSuspensos = mostrarTemas
          ? (filtradoSuspensosTema[0] ?? null)
          : (filtradoSuspensosActividad[0] ?? null);
        const topDesviacion = mostrarTemas
          ? (filtradoDesvioTema[0] ?? null)
          : (filtradoDesvioActividad[0] ?? null);
        const topAbandono = mostrarTemas
          ? null
          : (filtradoRepAbandono[0] ?? null);
        const topRepeticiones = mostrarTemas
          ? null
          : (filtradoRepAbandono[0] ?? null);

        const semaforoFiltrado = analisisInsights.semaforo.filter((s) => (
          analisisSemaforoFiltro === 'all' ? true : s.estado === analisisSemaforoFiltro
        ));
        const semaforoEstadoOrden: Record<AlumnoSemaforo['estado'], number> = { riesgo: 0, atencion: 1, bien: 2 };
        const semaforoOrdenado = [...semaforoFiltrado].sort((a, b) => {
          let comparacion = 0;
          if (analisisSemaforoOrden === 'alumno') {
            comparacion = a.alumno.localeCompare(b.alumno, 'es', { sensitivity: 'base' });
          } else if (analisisSemaforoOrden === 'notaActual') {
            comparacion = a.notaActual - b.notaActual;
          } else {
            comparacion = semaforoEstadoOrden[a.estado] - semaforoEstadoOrden[b.estado];
          }
          return analisisSemaforoSentido === 'asc' ? comparacion : -comparacion;
        });

        const handleSemaforoSort = (orden: 'alumno' | 'notaActual' | 'estado') => {
          if (analisisSemaforoOrden === orden) {
            setAnalisisSemaforoSentido(prev => (prev === 'asc' ? 'desc' : 'asc'));
            return;
          }
          setAnalisisSemaforoOrden(orden);
          setAnalisisSemaforoSentido(orden === 'alumno' ? 'asc' : 'desc');
        };

        const resumenPrimario = mostrarTemas ? (filtradoSuspensosTema[0] ?? null) : (filtradoSuspensosActividad[0] ?? null);
        const resumenSecundario = mostrarTemas ? (filtradoDesvioTema[0] ?? null) : (filtradoDesvioActividad[0] ?? null);
        const resumenTercero = mostrarTemas ? (filtradoSuspensosTema[1] ?? null) : (filtradoRepAbandono[0] ?? null);

        const metricItems = (mostrarTemas
          ? filtradoSuspensosTema.slice(0, 5).map((x) => {
            const desviacion = filtradoDesvioTema.find(item => item.temaId === x.temaId);
            return {
              kind: 'tema' as const,
              id: x.temaId,
              nombre: x.nombre,
              pct: x.pct,
              sd: desviacion ? desviacion.sd : 0,
              abandonoPct: null as number | null,
              repMedia: null as number | null,
            };
          })
          : filtradoSuspensosActividad.slice(0, 5).map((x) => {
            const desviacion = filtradoDesvioActividad.find(item => item.actividadId === x.actividadId);
            const abandono = filtradoRepAbandono.find(item => item.actividadId === x.actividadId);
            return {
              kind: 'actividad' as const,
              id: x.actividadId,
              nombre: x.nombre,
              pct: x.pct,
              sd: desviacion ? desviacion.sd : 0,
              abandonoPct: abandono ? abandono.abandonoPct : null,
              repMedia: abandono ? abandono.repMedia : null,
            };
          }))
          .sort((a, b) => {
            const valueA = ordenMetricas === 'suspensos'
              ? a.pct
              : ordenMetricas === 'desviacion'
                ? a.sd
                : ordenMetricas === 'abandono'
                  ? (a.abandonoPct ?? Number.NEGATIVE_INFINITY)
                  : (a.repMedia ?? Number.NEGATIVE_INFINITY);
            const valueB = ordenMetricas === 'suspensos'
              ? b.pct
              : ordenMetricas === 'desviacion'
                ? b.sd
                : ordenMetricas === 'abandono'
                  ? (b.abandonoPct ?? Number.NEGATIVE_INFINITY)
                  : (b.repMedia ?? Number.NEGATIVE_INFINITY);
            return analisisMetricSentido === 'desc' ? valueB - valueA : valueA - valueB;
          });

        return (
          <div className="analisis-layout">
            <div className="analisis-header">
              <h2 className="analisis-title">Análisis del curso</h2>
              <p className="analisis-subtitle">Vista general con tabla de temas y actividades, gráficas y semáforo de alumnos.</p>
            </div>

            <section className="analisis-panel analisis-panel--full">
              <h3 className="analisis-panel-title">Tabla de rendimiento por tema y actividad</h3>
              <p className="analisis-panel-help">Consulta primero los temas y despliega cada uno para ver sus actividades.</p>
              {analisisLoading ? (
                <div className="stats-info-msg">Cargando tabla de análisis...</div>
              ) : analisisError ? (
                <div className="stats-error-msg">{analisisError}</div>
              ) : (
                <div className="table-scroll-container">
                  <table className="pixel-table">
                    <thead>
                      <tr>
                        <th>
                          <button type="button" className={`analisis-table-sort-btn${analisisTablaOrden === 'elemento' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleAnalisisTablaSort('elemento')}>
                            Elemento{analisisTablaOrden === 'elemento' ? (analisisTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-table-sort-btn${analisisTablaOrden === 'tipo' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleAnalisisTablaSort('tipo')}>
                            Tipo{analisisTablaOrden === 'tipo' ? (analisisTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-table-sort-btn${analisisTablaOrden === 'notaMedia' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleAnalisisTablaSort('notaMedia')}>
                            Nota media{analisisTablaOrden === 'notaMedia' ? (analisisTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-table-sort-btn${analisisTablaOrden === 'notaMaxima' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleAnalisisTablaSort('notaMaxima')}>
                            Nota máx.{analisisTablaOrden === 'notaMaxima' ? (analisisTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-table-sort-btn${analisisTablaOrden === 'notaMinima' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleAnalisisTablaSort('notaMinima')}>
                            Nota mín.{analisisTablaOrden === 'notaMinima' ? (analisisTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-table-sort-btn${analisisTablaOrden === 'tiempoMedio' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleAnalisisTablaSort('tiempoMedio')}>
                            Tiempo medio{analisisTablaOrden === 'tiempoMedio' ? (analisisTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-table-sort-btn${analisisTablaOrden === 'completado' ? ' analisis-table-sort-btn--active' : ''}`} onClick={() => handleAnalisisTablaSort('completado')}>
                            Completado por todos{analisisTablaOrden === 'completado' ? (analisisTablaSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {analisisTreeOrdenado.map((tema) => {
                        const temaStats = analisisTemaStats.get(tema.id);
                        const expanded = analisisExpandedTemas.has(tema.id);
                        const actividadesStats = analisisActividadStats.get(tema.id) ?? new Map<number, ActividadStats>();

                        return (
                          <React.Fragment key={tema.id}>
                            <tr
                              className="analisis-theme-row"
                              onClick={() => {
                                setAnalisisExpandedTemas(prev => {
                                  const next = new Set(prev);
                                  if (next.has(tema.id)) next.delete(tema.id); else next.add(tema.id);
                                  return next;
                                });
                              }}
                            >
                              <td><span className="analisis-toggle">{expanded ? '▾' : '▸'}</span> {tema.titulo}</td>
                              <td><span className="analisis-pill">Tema</span></td>
                              <td className="text-center font-bold">{formatearNumero2Dec(limitarNotaSobreDiez(temaStats?.notaMediaTema))}</td>
                              <td className="text-center font-bold">{formatearNumero2Dec(limitarNotaSobreDiez(temaStats?.notaMaximaTema))}</td>
                              <td className="text-center font-bold">{formatearNumero2Dec(limitarNotaSobreDiez(temaStats?.notaMinimaTema))}</td>
                              <td className="text-center font-bold">{formatearTiempoCurso(temaStats?.tiempoMedioTema)}</td>
                              <td className="text-center font-bold">{temaStats?.temaCompletadoPorTodos == null ? 'N/A' : (temaStats.temaCompletadoPorTodos ? 'Sí' : 'No')}</td>
                            </tr>

                            {expanded && tema.actividades.map((act) => {
                              const st = actividadesStats.get(act.id);
                              return (
                                <tr key={act.id} className="analisis-activity-row">
                                  <td className="analisis-activity-cell">{act.titulo}</td>
                                  <td><span className="analisis-pill analisis-pill--activity">Actividad</span></td>
                                  <td className="text-center font-bold">{formatearNumero2Dec(limitarNotaSobreDiez(st?.notaMediaActividad))}</td>
                                  <td className="text-center font-bold">{formatearNumero2Dec(limitarNotaSobreDiez(st?.notaMaximaActividad))}</td>
                                  <td className="text-center font-bold">{formatearNumero2Dec(limitarNotaSobreDiez(st?.notaMinimaActividad))}</td>
                                  <td className="text-center font-bold">{formatearTiempoCurso(st?.tiempoMedioActividad)}</td>
                                  <td className="text-center font-bold">{st?.actividadCompletadaPorTodos == null ? 'N/A' : (st.actividadCompletadaPorTodos ? 'Sí' : 'No')}</td>
                                </tr>
                              );
                            })}
                          </React.Fragment>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </section>

            <section className="analisis-panel analisis-panel--full">
              <h3 className="analisis-panel-title">Gráfica de temas y actividades</h3>
              <div className="analisis-graph-filter">
                <label htmlFor="analisis-graf-tema">Tema:</label>
                <select
                  id="analisis-graf-tema"
                  className="stats-sidebar-select"
                  value={analisisGrafTema === 'all' ? '__all__' : String(analisisGrafTema)}
                  onChange={(e) => {
                    const v = e.target.value;
                    setAnalisisGrafTema(v === '__all__' ? 'all' : Number(v));
                  }}
                >
                  <option value="__all__">Todos los temas</option>
                  {analisisTree.map(t => (
                    <option key={t.id} value={t.id}>{t.titulo}</option>
                  ))}
                </select>
              </div>

              {analisisGrafTema === 'all' ? (
                <GraficasTemas cursoIdProp={id} embedded />
              ) : (
                <GraficasActividades cursoIdProp={id} embedded temaIdSeleccionado={analisisGrafTema} />
              )}
            </section>

            <div className="analisis-grid-two">
              <section className="analisis-panel">
                <h3 className="analisis-panel-title">Indicadores de riesgo</h3>
                <p className="analisis-panel-help">Resume suspensos, dispersión, abandono y repeticiones para identificar los puntos que necesitan revisión.</p>
                <div className="analisis-kpi-block">
                  <h4 className="analisis-kpi-title">Resumen rápido</h4>
                  <div className="analisis-quick-grid">
                    <div className="analisis-quick-card analisis-quick-card--danger">
                      <span className="analisis-quick-card__label">{mostrarTemas ? 'Tema con más suspensos' : 'Actividad con más suspensos'}</span>
                      <span className="analisis-quick-card__value">{topSuspensos ? topSuspensos.nombre : 'Sin datos'}</span>
                      <span className="analisis-quick-card__meta">{topSuspensos ? `${topSuspensos.pct.toFixed(1)}% susp.` : 'No hay datos disponibles'}</span>
                    </div>
                    <div className="analisis-quick-card analisis-quick-card--info">
                      <span className="analisis-quick-card__label">{mostrarTemas ? 'Tema con más dispersión' : 'Actividad con más dispersión'}</span>
                      <span className="analisis-quick-card__value">{topDesviacion ? topDesviacion.nombre : 'Sin datos'}</span>
                      <span className="analisis-quick-card__meta">{topDesviacion ? `σ ${topDesviacion.sd.toFixed(2)}` : 'No hay dispersión calculable'}</span>
                    </div>
                    <div className="analisis-quick-card analisis-quick-card--warn">
                      <span className="analisis-quick-card__label">Actividad con más abandono</span>
                      <span className="analisis-quick-card__value">{topAbandono ? topAbandono.nombre : 'Sin datos'}</span>
                      <span className="analisis-quick-card__meta">{topAbandono ? `${topAbandono.abandonoPct.toFixed(1)}% abandono` : 'No hay abandono registrado'}</span>
                    </div>
                    <div className="analisis-quick-card analisis-quick-card--neutral">
                      <span className="analisis-quick-card__label">Actividad con más repeticiones</span>
                      <span className="analisis-quick-card__value">{topRepeticiones ? topRepeticiones.nombre : 'Sin datos'}</span>
                      <span className="analisis-quick-card__meta">{topRepeticiones ? `Reps ${topRepeticiones.repMedia.toFixed(2)}` : 'No hay repeticiones registradas'}</span>
                    </div>
                  </div>
                </div>

                <div className="analisis-inline-filters">
                  <label htmlFor="analisis-kpi-tema">Tema</label>
                  <select
                    id="analisis-kpi-tema"
                    className="stats-sidebar-select"
                    value={analisisKpiTema === 'all' ? '__all__' : String(analisisKpiTema)}
                    onChange={(e) => {
                      const v = e.target.value;
                      const temaVal = v === '__all__' ? 'all' : Number(v);
                      setAnalisisKpiTema(temaVal);
                    }}
                  >
                    <option value="__all__">Todos los temas</option>
                    {analisisTree.map(t => (
                      <option key={t.id} value={t.id}>{t.titulo}</option>
                    ))}
                  </select>
                </div>

                <div className="analisis-inline-filters">
                  <label htmlFor="analisis-metric-orden">Ordenar por</label>
                  <select
                    id="analisis-metric-orden"
                    className="stats-sidebar-select"
                    value={analisisMetricOrden}
                    onChange={(e) => setAnalisisMetricOrden(e.target.value as typeof analisisMetricOrden)}
                  >
                    <option value="suspensos">% suspensos</option>
                    <option value="desviacion">Desviación</option>
                    <option value="abandono" disabled={mostrarTemas}>% abandono</option>
                    <option value="repeticiones" disabled={mostrarTemas}>Nº repeticiones</option>
                  </select>
                  <label htmlFor="analisis-metric-sentido">Sentido</label>
                  <select
                    id="analisis-metric-sentido"
                    className="stats-sidebar-select"
                    value={analisisMetricSentido}
                    onChange={(e) => setAnalisisMetricSentido(e.target.value as typeof analisisMetricSentido)}
                  >
                    <option value="desc">Mayor a menor</option>
                    <option value="asc">Menor a mayor</option>
                  </select>
                </div>

                <div className="analisis-metric-group">
                  <h4 className="analisis-metric-heading">{mostrarTemas ? 'Temas: suspensos y dispersión' : 'Actividades: suspensos, dispersión, abandono y repeticiones'}</h4>
                  <div className="analisis-metric-list">
                    {metricItems.map((x) => {
                      return (
                        <div key={`${x.kind}-${x.id}`} className="analisis-metric-item analisis-metric-item--combined">
                          <div className="analisis-metric-main analisis-metric-main--stacked">
                            <span className="analisis-metric-name">{x.nombre}</span>
                            <span className="analisis-metric-sub">{mostrarTemas ? 'Tema' : 'Actividad'}</span>
                          </div>
                          <div className="analisis-metric-stats-grid">
                            <span className="analisis-metric-badge analisis-metric-badge--danger">{x.pct.toFixed(1)}% susp.</span>
                            <span className="analisis-metric-badge analisis-metric-badge--info">σ {x.sd.toFixed(2)}</span>
                            {mostrarTemas ? null : (
                              <>
                                <span className="analisis-metric-badge analisis-metric-badge--warn">{x.abandonoPct === null ? 'Abandono --' : `Abandono ${x.abandonoPct.toFixed(1)}%`}</span>
                                <span className="analisis-metric-badge analisis-metric-badge--neutral">{x.repMedia === null ? 'Reps --' : `Reps ${x.repMedia.toFixed(2)}`}</span>
                              </>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </section>

              <section className="analisis-panel">
                <h3 className="analisis-panel-title">Semáforo de alumnos</h3>
                <p className="analisis-panel-help">Filtra y ordena a los alumnos según su estado actual.</p>
                <div className="stats-alerts-row">
                  <button
                    type="button"
                    className={`stats-alert-card stats-alert-card--warn${analisisSemaforoFiltro === 'riesgo' ? ' stats-alert-card--active' : ''}`}
                    onClick={() => setAnalisisSemaforoFiltro(prev => (prev === 'riesgo' ? 'all' : 'riesgo'))}
                  >
                    <span className="stats-alert-card__label">Riesgo (&lt;5)</span>
                    <span className="stats-alert-card__value">{analisisInsights.semaforoCount.riesgo}</span>
                  </button>
                  <button
                    type="button"
                    className={`stats-alert-card stats-alert-card--info${analisisSemaforoFiltro === 'atencion' ? ' stats-alert-card--active' : ''}`}
                    onClick={() => setAnalisisSemaforoFiltro(prev => (prev === 'atencion' ? 'all' : 'atencion'))}
                  >
                    <span className="stats-alert-card__label">Atención (5-7)</span>
                    <span className="stats-alert-card__value">{analisisInsights.semaforoCount.atencion}</span>
                  </button>
                  <button
                    type="button"
                    className={`stats-alert-card stats-alert-card--good${analisisSemaforoFiltro === 'bien' ? ' stats-alert-card--active' : ''}`}
                    onClick={() => setAnalisisSemaforoFiltro(prev => (prev === 'bien' ? 'all' : 'bien'))}
                  >
                    <span className="stats-alert-card__label">Bien (&ge;7)</span>
                    <span className="stats-alert-card__value">{analisisInsights.semaforoCount.bien}</span>
                  </button>
                </div>
                <div className="table-scroll-container">
                  <table className="pixel-table">
                    <thead>
                      <tr>
                        <th>
                          <button type="button" className={`analisis-semaforo-sort-btn${analisisSemaforoOrden === 'alumno' ? ' analisis-semaforo-sort-btn--active' : ''}`} onClick={() => handleSemaforoSort('alumno')}>
                            Alumno{analisisSemaforoOrden === 'alumno' ? (analisisSemaforoSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-semaforo-sort-btn${analisisSemaforoOrden === 'notaActual' ? ' analisis-semaforo-sort-btn--active' : ''}`} onClick={() => handleSemaforoSort('notaActual')}>
                            Nota actual{analisisSemaforoOrden === 'notaActual' ? (analisisSemaforoSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                        <th className="text-center">
                          <button type="button" className={`analisis-semaforo-sort-btn${analisisSemaforoOrden === 'estado' ? ' analisis-semaforo-sort-btn--active' : ''}`} onClick={() => handleSemaforoSort('estado')}>
                            Estado{analisisSemaforoOrden === 'estado' ? (analisisSemaforoSentido === 'asc' ? ' ↑' : ' ↓') : ''}
                          </button>
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {semaforoOrdenado.map((s, i) => (
                        <tr key={`sem-${i}`}>
                          <td>{s.alumno}</td>
                          <td className="text-center font-bold">{s.notaActual.toFixed(2)}</td>
                          <td className={`text-center font-bold analisis-semaforo-cell analisis-semaforo-cell--${s.estado}`}>
                            {s.estado === 'riesgo' ? 'Riesgo' : s.estado === 'atencion' ? 'Atención' : 'Bien'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            </div>
          </div>
        );
      }
      case "analisisTablas":
        return (
          <div className="analisis-layout">
            <div className="analisis-header">
              <h2 className="analisis-title">Análisis en tablas</h2>
              <p className="analisis-subtitle">Resumen de temas, actividades y tiempos de realización.</p>
            </div>

            <section className="analisis-panel analisis-panel--full">
              <h3 className="analisis-panel-title">Medias del tema seleccionado</h3>
              <MediasCurso cursoIdProp={id} embedded temaIdSeleccionado={analisisTemaId} />
            </section>

            <div className="analisis-grid-two">
              <section className="analisis-panel">
                <h3 className="analisis-panel-title">Rendimiento por temas</h3>
                <EstadisticasTemas cursoIdProp={id} embedded />
              </section>

              <section className="analisis-panel">
                <h3 className="analisis-panel-title">Detalle de actividades del tema</h3>
                <EstadisticasActividades cursoIdProp={id} embedded temaIdSeleccionado={analisisTemaId} />
              </section>
            </div>

            <section className="analisis-panel analisis-panel--full">
              <h3 className="analisis-panel-title">Tiempos de la actividad seleccionada</h3>
              {analisisActividadId ? (
                <EstadisticasActividad actividadIdProp={String(analisisActividadId)} embedded />
              ) : (
                <div className="stats-placeholder">Selecciona una actividad para ver tiempos.</div>
              )}
            </section>
          </div>
        );
      case "analisisGraficas":
        return (
          <div className="analisis-layout">
            <div className="analisis-header">
              <h2 className="analisis-title">Análisis en gráficas</h2>
              <p className="analisis-subtitle">Comparación visual de temas y actividades.</p>
            </div>

            <div className="analisis-grid-two">
              <section className="analisis-panel">
                <h3 className="analisis-panel-title">Gráficas por temas</h3>
                <GraficasTemas cursoIdProp={id} embedded />
              </section>

              <section className="analisis-panel">
                <h3 className="analisis-panel-title">Gráficas por actividades del tema</h3>
                <GraficasActividades cursoIdProp={id} embedded temaIdSeleccionado={analisisTemaId} />
              </section>
            </div>
          </div>
        );
      case "medias":
        return <MediasCurso
          cursoIdProp={id}
          embedded
          temaIdSeleccionado={'temaId' in statsView ? statsView.temaId : undefined}
        />;
      case "tiemposActividad":
        if ('actividadId' in statsView && statsView.actividadId) {
          return <EstadisticasActividad actividadIdProp={String(statsView.actividadId)} embedded />;
        }
        return <div className="stats-placeholder">Selecciona una actividad de la lista</div>;
      case "alumnos":
      case "alumnoDetalle":
        return renderAlumnosContent();
      case "desgloseActividades":
        if ('temaId' in statsView && statsView.temaId) {
          return <EstadisticasActividades cursoIdProp={id} embedded temaIdSeleccionado={statsView.temaId} />;
        }
        return <div className="stats-placeholder">Selecciona un tema de la lista</div>;
      case "desgloseTemas":
        return <EstadisticasTemas cursoIdProp={id} embedded />;
      case "graficasActividades":
        if ('temaId' in statsView && statsView.temaId) {
          return <GraficasActividades cursoIdProp={id} embedded temaIdSeleccionado={statsView.temaId} />;
        }
        return <div className="stats-placeholder">Selecciona un tema de la lista</div>;
      case "graficasTemas":
        return <GraficasTemas cursoIdProp={id} embedded />;
      case "tendencias":
        return (
          <div className="graficas-embedded-main">
            <div className="chart-header">
              <h2 className="chart-main-title">Tendencias de clase</h2>
              <p className="chart-subtitle">Distribución, dispersión y variabilidad por tema.</p>
            </div>
            <div className="charts-grid">
              <HistogramChart title="Distribución por actividades completadas" data={tendenciasData} />
              <ScatterTiempoPuntosChart data={estadisticas} />
              {loadingTendencias ? (
                <p className="stats-info-msg">Cargando heatmap y boxplot...</p>
              ) : errorTendencias ? (
                <p className="stats-error-msg">{errorTendencias}</p>
              ) : tendenciasAvanzadas ? (
                <>
                  <HeatmapTemaAlumnoChart temas={tendenciasAvanzadas.temas} rows={tendenciasAvanzadas.rows} />
                  <BoxplotTemasChart temas={tendenciasAvanzadas.temas} series={tendenciasAvanzadas.series} />
                </>
              ) : (
                <p className="stats-info-msg">No hay suficientes datos para tendencias avanzadas.</p>
              )}
            </div>
          </div>
        );
      default:
        return (
          <>
            {cursoIndicadoresContent}
            <div className="estadisticas-yellow-card">
              {estadisticasContent}
            </div>
          </>
        );
    }
  };

return (
    <div className={embedded ? 'estadisticas-embedded' : 'estadisticas-page'}>
      {!embedded && <NavbarMisCursos />}
      
      <main className="estadisticas-main">
        {!embedded && (
          <>
            <div style={{ width: '100%', display: 'flex', marginBottom: '10px' }}>
              <button className="btn-volver-pixel" onClick={() => navigate(-1)}>
                 Volver
              </button>
            </div>
            <h1 className="estadisticas-titulo-curso">Estadísticas del Curso</h1>
          </>
        )}

        <div className="stats-layout">
          <div className="stats-sidebar">
            {renderSidebar()}
          </div>
          <div className={`stats-content-area${isAlumnosMode ? ' stats-content-area--alumno' : ''}`}>
            {renderStatsContent()}
          </div>
        </div>
      </main>
    </div>
  );
}

function HeatmapTemaAlumnoChart({ temas, rows }: { temas: string[]; rows: HeatmapRow[] }) {
  if (temas.length === 0 || rows.length === 0) {
    return <p className="stats-info-msg">No hay datos suficientes para el heatmap alumno-tema.</p>;
  }

  const cellColor = (nota: number | null): string => {
    if (nota === null || !Number.isFinite(nota)) return '#f3f4f6';
    if (nota < 5) return '#fecaca';
    if (nota < 7) return '#fde68a';
    if (nota < 9) return '#bfdbfe';
    return '#86efac';
  };

  return (
    <div className="chart-block">
      <h3 className="chart-title">Heatmap alumno x tema (nota actual)</h3>
      <div className="chart-scroll-container">
        <table className="stats-heatmap-table" aria-label="Heatmap de nota actual por alumno y tema">
          <thead>
            <tr>
              <th>Alumno</th>
              {temas.map((t, idx) => (
                <th key={`${t}-${idx}`}>{t}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, ridx) => (
              <tr key={`${row.alumno}-${ridx}`}>
                <td className="stats-heatmap-student">{row.alumno}</td>
                {row.notas.map((n, nidx) => (
                  <td
                    key={`${row.alumno}-${nidx}`}
                    className="stats-heatmap-cell"
                    style={{ backgroundColor: cellColor(n) }}
                    title={n === null ? 'Sin datos' : `${n.toFixed(2)}/10`}
                  >
                    {n === null ? '—' : n.toFixed(1)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function BoxplotTemasChart({
  temas,
  series,
}: {
  temas: string[];
  series: number[][];
}) {
  const valid = temas
    .map((tema, idx) => ({ tema, values: series[idx] ?? [] }))
    .filter(item => item.values.length > 0);

  if (valid.length === 0) {
    return <p className="stats-info-msg">No hay datos suficientes para boxplot por tema.</p>;
  }

  const height = 300;
  const margin = { top: 20, right: 16, bottom: 58, left: 28 };
  const chartHeight = height - margin.top - margin.bottom;
  const boxWidth = 42;
  const gap = 22;
  const width = Math.max(620, margin.left + margin.right + valid.length * boxWidth + Math.max(0, valid.length - 1) * gap);
  const yScaleMax = 10;

  const yPos = (v: number) => margin.top + (chartHeight - (Math.max(0, Math.min(yScaleMax, v)) / yScaleMax) * chartHeight);

  return (
    <div className="chart-block">
      <h3 className="chart-title">Boxplot de notas por tema</h3>
      <div className="chart-scroll-container" role="img" aria-label="Boxplot de notas actuales por tema">
        <svg width={width} height={height}>
          {[0, 5, 10].map(tick => (
            <line key={tick} x1={margin.left - 4} y1={yPos(tick)} x2={width - margin.right + 4} y2={yPos(tick)} stroke="#e5e7eb" strokeWidth={1} />
          ))}

          {valid.map((item, idx) => {
            const values = [...item.values].sort((a, b) => a - b);
            const min = values[0];
            const q1 = quantile(values, 0.25);
            const med = quantile(values, 0.5);
            const q3 = quantile(values, 0.75);
            const max = values[values.length - 1];
            const x = margin.left + idx * (boxWidth + gap);
            const center = x + boxWidth / 2;

            return (
              <g key={`${item.tema}-${idx}`}>
                <line x1={center} y1={yPos(min)} x2={center} y2={yPos(max)} stroke="#64748b" strokeWidth={1.2} />
                <rect x={x} y={yPos(q3)} width={boxWidth} height={Math.max(2, yPos(q1) - yPos(q3))} fill="#bfdbfe" stroke="#2563eb" strokeWidth={1.2} rx={5} />
                <line x1={x} y1={yPos(med)} x2={x + boxWidth} y2={yPos(med)} stroke="#1d4ed8" strokeWidth={2} />
                <line x1={x + 6} y1={yPos(min)} x2={x + boxWidth - 6} y2={yPos(min)} stroke="#64748b" strokeWidth={1.2} />
                <line x1={x + 6} y1={yPos(max)} x2={x + boxWidth - 6} y2={yPos(max)} stroke="#64748b" strokeWidth={1.2} />
                <title>{`${item.tema}: min ${min.toFixed(2)}, q1 ${q1.toFixed(2)}, med ${med.toFixed(2)}, q3 ${q3.toFixed(2)}, max ${max.toFixed(2)}`}</title>
                <text x={center} y={height - 14} textAnchor="middle" fontSize="10" fill="#475569" fontWeight="700" fontFamily="'Oxygen', sans-serif">
                  {item.tema}
                </text>
              </g>
            );
          })}

          <line x1={margin.left - 4} y1={margin.top + chartHeight} x2={width - margin.right + 4} y2={margin.top + chartHeight} stroke="#94a3b8" strokeWidth={1.2} />
        </svg>
      </div>
    </div>
  );
}